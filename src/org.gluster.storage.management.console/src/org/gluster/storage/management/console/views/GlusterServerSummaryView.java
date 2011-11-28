/**
 * GlusterServerSummaryView.java
 *
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.gluster.storage.management.console.views;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.gluster.storage.management.client.GlusterServersClient;
import org.gluster.storage.management.console.Activator;
import org.gluster.storage.management.console.ConsoleConstants;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.IImageKeys;
import org.gluster.storage.management.console.NetworkInterfaceTableLabelProvider;
import org.gluster.storage.management.console.preferences.PreferenceConstants;
import org.gluster.storage.management.console.toolbar.GlusterToolbarManager;
import org.gluster.storage.management.console.utils.ChartUtil;
import org.gluster.storage.management.console.utils.ChartUtil.ChartPeriodLinkListener;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.utils.GlusterLogger;
import org.gluster.storage.management.core.model.ClusterListener;
import org.gluster.storage.management.core.model.DefaultClusterListener;
import org.gluster.storage.management.core.model.Event;
import org.gluster.storage.management.core.model.Event.EVENT_TYPE;
import org.gluster.storage.management.core.model.GlusterServer;
import org.gluster.storage.management.core.model.Server.SERVER_STATUS;
import org.gluster.storage.management.core.model.ServerStats;
import org.gluster.storage.management.core.utils.NumberUtil;

import com.richclientgui.toolbox.gauges.CoolGauge;

public class GlusterServerSummaryView extends ViewPart {
	public static final String ID = GlusterServerSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private GlusterServer server;
	private ClusterListener clusterListener;
	private static final GlusterLogger logger = GlusterLogger.getInstance();
	private static final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

	public enum NETWORK_INTERFACE_TABLE_COLUMN_INDICES {
		INTERFACE, MODEL, SPEED, IP_ADDRESS, NETMASK, GATEWAY
	};

	private static final String[] NETWORK_INTERFACE_TABLE_COLUMN_NAMES = { "Interface", "Model", "Speed", "IP Address",
			"Netmask", "Gateway" };
	private CoolGauge cpuGauge;
	private IPropertyChangeListener propertyChangeListener;
	private Composite cpuUsageSection;
	private Composite networkUsageSection;
	private Composite memoryUsageSection;
	private static final ChartUtil chartUtil = ChartUtil.getInstance();
	private Composite serverSummarySection;
	private Label numCpus;
	private ProgressBar memoryUsageBar;
	private ProgressBar diskUsageBar;
	private CLabel lblServerStatus;

	@Override
	public void createPartControl(Composite parent) {
		if (server == null) {
			server = guiHelper.getSelectedEntity(getSite(), GlusterServer.class);
		}
		setPartName("Summary");
		createSections(parent);
		
		createListeners();
	}

	private void createListeners() {
		// Refresh the server details whenever the server has changed
		createClusterListener();
		GlusterDataModelManager.getInstance().addClusterListener(clusterListener);
		
		createPropertyChangeListener();
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
	}

	private void createPropertyChangeListener() {
		propertyChangeListener = new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				String propertyName = event.getProperty();
				if(propertyName.equals(PreferenceConstants.P_CPU_CHART_PERIOD)) {
					refreshCpuChart();
				} else if(propertyName.equals(PreferenceConstants.P_MEM_CHART_PERIOD)) {
					refreshMemoryChart();
				} else if (propertyName.equals(PreferenceConstants.P_NETWORK_CHART_PERIOD)
						|| propertyName.equals(PreferenceConstants.P_DEFAULT_NETWORK_INTERFACE_PFX + server.getName())) {
					refreshNetworkChart();
				}
			}
		};
	}

	private void createClusterListener() {
		final GlusterToolbarManager toolbarManager = new GlusterToolbarManager(getSite().getWorkbenchWindow());
		final GlusterServer thisServer = server;
		clusterListener = new DefaultClusterListener() {
			
			@Override
			public void serverChanged(GlusterServer server, Event event) {
				if (event.getEventType() == EVENT_TYPE.GLUSTER_SERVER_CHANGED && server == thisServer) {
					updateServerDetails();
					toolbarManager.updateToolbar(server);
					refreshCharts();
				}
			}
		};
	}
	
	private void refreshCharts() {
		refreshCpuChart();
		refreshMemoryChart();
		refreshNetworkChart();
	}

	private void refreshNetworkChart() {
		guiHelper.clearSection(networkUsageSection);
		String statsPeriod = preferenceStore.getString(PreferenceConstants.P_NETWORK_CHART_PERIOD);
		String networkInterface = preferenceStore.getString(PreferenceConstants.P_DEFAULT_NETWORK_INTERFACE_PFX + server.getName());
		if(networkInterface == null || networkInterface.isEmpty()) {
			networkInterface = server.getNetworkInterfaces().get(0).getName();
		}
		ServerStats stats = new GlusterServersClient().getNetworkStats(server.getName(), networkInterface, statsPeriod);
		chartUtil.refreshChartSection(toolkit, networkUsageSection, stats, statsPeriod, "KiB/s", -1, 5, chartUtil.new NetworkChartPeriodLinkListener(server, statsPeriod, toolkit), 2);
	}

	private void refreshMemoryChart() {
		guiHelper.clearSection(memoryUsageSection);
		String statsPeriod = preferenceStore.getString(PreferenceConstants.P_MEM_CHART_PERIOD);
		ServerStats stats = new GlusterServersClient().getMemoryStats(server.getName(), statsPeriod);
		chartUtil.refreshChartSection(toolkit, memoryUsageSection, stats, statsPeriod, "%", 100, 4, chartUtil.new MemoryChartPeriodLinkListener(server.getName(), statsPeriod, toolkit), 0);
	}

	private void refreshCpuChart() {
		guiHelper.clearSection(cpuUsageSection);
		String statsPeriod = preferenceStore.getString(PreferenceConstants.P_CPU_CHART_PERIOD);
		ServerStats stats = new GlusterServersClient().getCpuStats(server.getName(), statsPeriod);
		chartUtil.refreshChartSection(toolkit, cpuUsageSection, stats, statsPeriod, "%", 100, 4,
				chartUtil.new CpuChartPeriodLinkListener(server.getName(), statsPeriod, toolkit), 2);
	}

	private void updateServerDetails() {
		// TODO: Update the server details (cpu usage, memory usage)
		populateServerSummarySection(server);
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(clusterListener);
		preferenceStore.removePropertyChangeListener(propertyChangeListener);
	}

	private void createMemoryUsageSection() {
		String memStatsPeriod = preferenceStore.getString(PreferenceConstants.P_MEM_CHART_PERIOD);
		memoryUsageSection = guiHelper.createSection(form, toolkit, "Memory Usage", null, 1, false);
		
		ServerStats stats;
		try {
			stats = new GlusterServersClient().getMemoryStats(server.getName(), memStatsPeriod);
		} catch(Exception e) {
			logger.error("Couldn't fetch memory usage statistics for server [" + server.getName() + "]", e);
			toolkit.createLabel(memoryUsageSection, "Couldn't fetch memory usage statistics for server [" + server.getName() + "]! Error: [" + e.getMessage() + "]");
			return;
		}
		
		// in case of memory usage, there are four elements in usage data: user, free, cache, buffer and total. we use "user".
		ChartUtil chartUtil = ChartUtil.getInstance();
		chartUtil.createAreaChart(toolkit, memoryUsageSection, stats, 0, "%", chartUtil
				.getTimestampFormatForPeriod(memStatsPeriod),
				chartUtil.new MemoryChartPeriodLinkListener(server.getName(), memStatsPeriod, toolkit), 100, 4);
	}
	
	private void createCPUUsageSection() {
		String cpuStatsPeriod = preferenceStore.getString(PreferenceConstants.P_CPU_CHART_PERIOD);
		cpuUsageSection = guiHelper.createSection(form, toolkit, "CPU Usage", null, 1, false);
		
		ServerStats stats;
		try {
			stats = new GlusterServersClient().getCpuStats(server.getName(), cpuStatsPeriod);
		} catch(Exception e) {
			logger.error("Couldn't fetch CPU usage statistics for server [" + server.getName() + "]", e);
			toolkit.createLabel(cpuUsageSection, "Couldn't fetch CPU usage statistics for server [" + server.getName() + "]! Error: [" + e.getMessage() + "]");
			return;
		}

		// in case of CPU usage, there are three elements in usage data: user, system and total. we use total.
		chartUtil.createAreaChart(toolkit, cpuUsageSection, stats, 2, "%", chartUtil
				.getTimestampFormatForPeriod(cpuStatsPeriod),
				chartUtil.new CpuChartPeriodLinkListener(server.getName(), cpuStatsPeriod, toolkit), 100, 4);
	}
	
	private void createNetworkUsageSection() {
		final String networkStatsPeriod = preferenceStore.getString(PreferenceConstants.P_NETWORK_CHART_PERIOD);
		networkUsageSection = guiHelper.createSection(form, toolkit, "Network Usage", null, 1, false);

		String networkInterface = server.getNetworkInterfaces().get(0).getName();
		ServerStats stats;
		try {
			stats = new GlusterServersClient().getNetworkStats(server.getName(), networkInterface, networkStatsPeriod);
		} catch(Exception e) {
			logger.error("Couldn't fetch Network usage statistics for server [" + server.getName() + "] network interface [" + networkInterface + "]", e);
			toolkit.createLabel(networkUsageSection, "Couldn't fetch CPU usage statistics for server [" + server.getName() + "]! Error: [" + e.getMessage() + "]");
			return;
		}

		// in case of network usage, there are three elements in usage data: received, transmitted and total. we use total.
		final ChartUtil chartUtil = ChartUtil.getInstance();
		final ChartPeriodLinkListener networkChartPeriodLinkListener = chartUtil.new NetworkChartPeriodLinkListener(server, networkStatsPeriod, toolkit);
		chartUtil.createAreaChart(toolkit, networkUsageSection, stats, 2, "KiB/s", chartUtil
				.getTimestampFormatForPeriod(networkStatsPeriod),
				networkChartPeriodLinkListener , -1, 5);
	}
	
	private void createSections(Composite parent) {
		String serverName = server.getName();
		form = guiHelper.setupForm(parent, toolkit, "Server Summary [" + serverName + "]");
		createServerSummarySection(server, toolkit, form);

		if (server.getStatus() == SERVER_STATUS.ONLINE) {
			try {
				new ProgressMonitorDialog(getSite().getShell()).run(false, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Creating Server Summary View", 4);
						monitor.setTaskName("Creating Memory Usage Section");
						createMemoryUsageSection();
						monitor.worked(1);

						monitor.setTaskName("Creating Network Usage Section");
						createNetworkUsageSection();
						monitor.worked(1);
						
						monitor.setTaskName("Creating CPU Usage Section");
						createCPUUsageSection();
						monitor.worked(1);
						
						monitor.setTaskName("Creating Network Interfaces Section");
						createNetworkInterfacesSection(server, toolkit, form);
						monitor.worked(1);
						monitor.done();
					}
				});
			} catch (Exception e) {
				String errMsg = "Exception while creating the Gluster Server Summary View : [" + e.getMessage() + "]";
				logger.error(errMsg, e);
				MessageDialog.openError(getSite().getShell(), ConsoleConstants.CONSOLE_TITLE, errMsg);
			}
		}

		parent.layout(); // IMP: lays out the form properly
	}

	private void createServerSummarySection(GlusterServer server, FormToolkit toolkit, final ScrolledForm form) {
		serverSummarySection = guiHelper.createSection(form, toolkit, "Summary", null, 2, false);
		// toolkit.createLabel(section, "Preferred Network: ", SWT.NONE);
		// toolkit.createLabel(section, server.getPreferredNetworkInterface().getName(), SWT.NONE);
		
		if (server.isOnline()) {
			toolkit.createLabel(serverSummarySection, "Number of CPUs: ", SWT.NONE);
			numCpus = toolkit.createLabel(serverSummarySection, "" + server.getNumOfCPUs(), SWT.NONE);

			toolkit.createLabel(serverSummarySection, "% CPU Usage (avg): ", SWT.NONE);
			cpuGauge = new CoolGauge(serverSummarySection, guiHelper.getImage(IImageKeys.GAUGE_SMALL));

			toolkit.createLabel(serverSummarySection, "Memory Usage: ", SWT.NONE);
			memoryUsageBar = new ProgressBar(serverSummarySection, SWT.SMOOTH);

			// toolkit.createLabel(section, "Memory Usage: ", SWT.NONE);
			// final CoolProgressBar bar = new CoolProgressBar(section,SWT.HORIZONTAL,
			// guiHelper.getImage(IImageKeys.PROGRESS_BAR_LEFT),
			// guiHelper.getImage(IImageKeys.PROGRESS_BAR_FILLED),
			// guiHelper.getImage(IImageKeys.PROGRESS_BAR_EMPTY),
			// guiHelper.getImage(IImageKeys.PROGRESS_BAR_RIGHT));
			// bar.updateProgress(server.getMemoryInUse() / server.getTotalMemory());

			// toolkit.createLabel(section, "Total Disk Space (GB): ", SWT.NONE);
			// toolkit.createLabel(section, online ? "" + server.getTotalDiskSpace() : "NA", SWT.NONE);
			//
			// toolkit.createLabel(section, "Disk Space in Use (GB): ", SWT.NONE);
			// toolkit.createLabel(section, online ? "" + server.getDiskSpaceInUse() : "NA", SWT.NONE);

			toolkit.createLabel(serverSummarySection, "Disk Usage: ", SWT.NONE);
			diskUsageBar = new ProgressBar(serverSummarySection, SWT.SMOOTH);
		}

		toolkit.createLabel(serverSummarySection, "Status: ", SWT.NONE);
		lblServerStatus = new CLabel(serverSummarySection, SWT.NONE);
		populateServerSummarySection(server);
	}
	
	private void populateServerSummarySection(GlusterServer server) {
		if (server.isOnline()) {
			numCpus.setText("" + server.getNumOfCPUs());
			numCpus.redraw();

			cpuGauge.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			cpuGauge.setGaugeNeedleColour(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			cpuGauge.setGaugeNeedleWidth(2);
			cpuGauge.setGaugeNeedlePivot(new Point(66, 65));

			cpuGauge.setPoints(getPnts());
			cpuGauge.setLevel(server.getCpuUsage() / 100);
			cpuGauge.setToolTipText(server.getCpuUsage() + "%");
			cpuGauge.redraw();

			memoryUsageBar.setMinimum(0);
			memoryUsageBar.setMaximum((int) Math.round(server.getTotalMemory()));
			memoryUsageBar.setSelection((int) Math.round(server.getMemoryInUse()));
			memoryUsageBar.setToolTipText("Total: " + NumberUtil.formatNumber((server.getTotalMemory() / 1024))
					+ "GB, In Use: " + NumberUtil.formatNumber((server.getMemoryInUse() / 1024)) + "GB");

			diskUsageBar.setMinimum(0);
			diskUsageBar.setMaximum((int) Math.round(server.getTotalDiskSpace()));
			diskUsageBar.setSelection((int) Math.round(server.getDiskSpaceInUse()));
			diskUsageBar.setToolTipText("Total: " + NumberUtil.formatNumber((server.getTotalDiskSpace() / 1024))
					+ "GB, In Use: " + NumberUtil.formatNumber((server.getDiskSpaceInUse() / 1024)) + "GB");

		}
		lblServerStatus.setText(server.getStatusStr());
		lblServerStatus.setImage(server.getStatus() == GlusterServer.SERVER_STATUS.ONLINE ? guiHelper
				.getImage(IImageKeys.STATUS_ONLINE_16x16) : guiHelper.getImage(IImageKeys.STATUS_OFFLINE_16x16));
		toolkit.adapt(lblServerStatus, true, true);

		serverSummarySection.layout();
		form.reflow(true);
	}

	private List<Point> getPnts() {
		final List<Point> pnts = new ArrayList<Point>();
		pnts.add(new Point(47, 98));
		pnts.add(new Point(34, 84));
		pnts.add(new Point(29, 65));
		pnts.add(new Point(33, 48));
		pnts.add(new Point(48, 33));
		pnts.add(new Point(66, 28));
		pnts.add(new Point(83, 32));
		pnts.add(new Point(98, 47));
		pnts.add(new Point(103, 65));
		pnts.add(new Point(98, 83));
		pnts.add(new Point(84, 98));
		return pnts;
	}

	private Composite createNetworkInterfacesSection(GlusterServer server, FormToolkit toolkit, ScrolledForm form) {
		final Composite section = guiHelper.createSection(form, toolkit, "Network Interfaces", null, 1, false);
		createNetworkInterfacesTableViewer(createTableViewerComposite(section), server);
		// Hyperlink changePreferredNetworkLink = toolkit.createHyperlink(section, "Change Preferred Network",
		// SWT.NONE);
		// changePreferredNetworkLink.addHyperlinkListener(new HyperlinkAdapter() {
		//
		// @Override
		// public void linkActivated(HyperlinkEvent e) {
		// new MessageDialog(
		// section.getShell(),
		// "Gluster Storage Platform",
		// guiHelper.getImage(IImageKeys.SERVER),
		// "This will show additional controls to help user choose a new network interface. TO BE IMPLEMENTED.",
		// MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();
		// }
		// });
		return section;
	}

	private TableViewer createNetworkInterfacesTableViewer(final Composite parent, GlusterServer server) {
		TableViewer tableViewer = new TableViewer(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		// TableViewer tableViewer = new TableViewer(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setLabelProvider(new NetworkInterfaceTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		setupNetworkInterfaceTable(parent, tableViewer.getTable());
		tableViewer.setInput(server.getNetworkInterfaces().toArray());

		return tableViewer;
	}

	private void setupNetworkInterfaceTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table,
				NETWORK_INTERFACE_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table, NETWORK_INTERFACE_TABLE_COLUMN_INDICES.INTERFACE, SWT.CENTER, 70);
		setColumnProperties(table, NETWORK_INTERFACE_TABLE_COLUMN_INDICES.MODEL, SWT.CENTER, 70);
		setColumnProperties(table, NETWORK_INTERFACE_TABLE_COLUMN_INDICES.SPEED, SWT.CENTER, 70);
		setColumnProperties(table, NETWORK_INTERFACE_TABLE_COLUMN_INDICES.IP_ADDRESS, SWT.CENTER, 100);
		setColumnProperties(table, NETWORK_INTERFACE_TABLE_COLUMN_INDICES.NETMASK, SWT.CENTER, 70);
		setColumnProperties(table, NETWORK_INTERFACE_TABLE_COLUMN_INDICES.GATEWAY, SWT.CENTER, 70);
	}

	private Composite createTableViewerComposite(Composite parent) {
		Composite tableViewerComposite = new Composite(parent, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		tableLayoutData.widthHint = 400;
		tableLayoutData.minimumWidth = 400;
		// tableLayoutData.grabExcessHorizontalSpace = true;
		tableViewerComposite.setLayoutData(tableLayoutData);
		return tableViewerComposite;
	}

	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 */
	public void setColumnProperties(Table table, NETWORK_INTERFACE_TABLE_COLUMN_INDICES columnIndex, int alignment,
			int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}
}
