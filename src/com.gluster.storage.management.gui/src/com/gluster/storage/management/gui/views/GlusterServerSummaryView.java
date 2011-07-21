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
package com.gluster.storage.management.gui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.chart.util.CDateTime;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.NetworkInterfaceTableLabelProvider;
import com.gluster.storage.management.gui.toolbar.GlusterToolbarManager;
import com.gluster.storage.management.gui.utils.ChartViewerComposite;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.ibm.icu.util.Calendar;
import com.richclientgui.toolbox.gauges.CoolGauge;

public class GlusterServerSummaryView extends ViewPart {
	public static final String ID = GlusterServerSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private GlusterServer server;
	private ClusterListener serverChangedListener;
	private static final int CHART_WIDTH = 350;

	public enum NETWORK_INTERFACE_TABLE_COLUMN_INDICES {
		INTERFACE, MODEL, SPEED, IP_ADDRESS, NETMASK, GATEWAY
	};

	private static final String[] NETWORK_INTERFACE_TABLE_COLUMN_NAMES = { "Interface", "Model", "Speed", "IP Address",
			"Netmask", "Gateway" };
	private CoolGauge cpuGauge;

	@Override
	public void createPartControl(Composite parent) {
		if (server == null) {
			server = (GlusterServer) guiHelper.getSelectedEntity(getSite(), GlusterServer.class);
		}
		setPartName("Summary");
		createSections(parent);
		
		final GlusterToolbarManager toolbarManager = new GlusterToolbarManager(getSite().getWorkbenchWindow());
		// Refresh the navigation tree whenever there is a change to the data model
		serverChangedListener = new DefaultClusterListener() {
			@Override
			public void serverChanged(GlusterServer server, Event event) {
				updateServerDetails();
				toolbarManager.updateToolbar(server);
			}
		};
		GlusterDataModelManager.getInstance().addClusterListener(serverChangedListener);
	}
	
	private void updateServerDetails() {
	}
	
	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(serverChangedListener);
	}

	private void createLineChart(Composite section, Calendar timestamps[], Double values[], String unit) {
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, timestamps, values, unit);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = CHART_WIDTH;
		data.heightHint = 250;
//		data.verticalAlignment = SWT.CENTER;
//		data.grabExcessVerticalSpace = false;
//		data.horizontalSpan = 5;
//		data.verticalIndent = 0;
//		data.verticalSpan = 1;
		chartViewerComposite.setLayoutData(data);
	}

	private void createMemoryUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Memory Usage", null, 1, false);
		Calendar[] timestamps = new Calendar[] { new CDateTime(1000l*1310468100), new CDateTime(1000l*1310468400), new CDateTime(1000l*1310468700),
				new CDateTime(1000l*1310469000), new CDateTime(1000l*1310469300), new CDateTime(1000l*1310469600), new CDateTime(1000l*1310469900),
				new CDateTime(1000l*1310470200), new CDateTime(1000l*1310470500), new CDateTime(1000l*1310470800), new CDateTime(1000l*1310471100),
				new CDateTime(1000l*1310471400), new CDateTime(1000l*1310471700), new CDateTime(1000l*1310472000), new CDateTime(1000l*1310472300),
				new CDateTime(1000l*1310472600), new CDateTime(1000l*1310472900), new CDateTime(1000l*1310473200), new CDateTime(1000l*1310473500),
				new CDateTime(1000l*1310473800) };
		//Double[] values = new Double[] { 10d, 11.23d, 17.92d, 18.69d, 78.62d, 89.11d, 92.43d, 20.31d, 19.63d, 18.46d, 10.44d, 16.28d, 13.51d, 17.53d, 12.21, 20d, 40d, 10d, 90d, 40d };
		Double[] values = new Double[] { 35d, 34.23d, 37.92d, 28.69d, 38.62d, 39.11d, 38.46d, 30.44d, 36.28d, 72.43d, 79.31d, 77.39d, 33.51d, 37.53d, 32.21, 30d, 31.43d, 36.45d, 34.86d, 35.27d };
		createLineChart(section, timestamps, values, "%");
		createChartLinks(section, 4);
	}
	
	private void createNetworkUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Network Usage", null, 1, false);

		Calendar[] timestamps = new Calendar[] { new CDateTime(1000l*1310468100), new CDateTime(1000l*1310468400), new CDateTime(1000l*1310468700),
				new CDateTime(1000l*1310469000), new CDateTime(1000l*1310469300), new CDateTime(1000l*1310469600), new CDateTime(1000l*1310469900),
				new CDateTime(1000l*1310470200), new CDateTime(1000l*1310470500), new CDateTime(1000l*1310470800), new CDateTime(1000l*1310471100),
				new CDateTime(1000l*1310471400), new CDateTime(1000l*1310471700), new CDateTime(1000l*1310472000), new CDateTime(1000l*1310472300),
				new CDateTime(1000l*1310472600), new CDateTime(1000l*1310472900), new CDateTime(1000l*1310473200), new CDateTime(1000l*1310473500),
				new CDateTime(1000l*1310473800) };
		Double[] values = new Double[] { 32d, 31.23d, 27.92d, 48.69d, 58.62d, 49.11d, 72.43d, 69.31d, 87.39d, 78.46d, 60.44d, 56.28d, 33.51d, 27.53d, 12.21, 10d, 21.43d, 36.45d, 34.86d, 35.27d };

		createLineChart(section, timestamps, values, "Kib/s");

		Composite graphComposite = createChartLinks(section, 5);
		
		CCombo interfaceCombo = new CCombo(graphComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER | SWT.FLAT);
		interfaceCombo.setItems(new String[] {"eth0"});
		interfaceCombo.select(0);
	}

	
	private void createCPUUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "CPU Usage", null, 1, false);
		//toolkit.createLabel(section, "Historical CPU Usage graph aggregated across\nall servers will be displayed here.");

		Calendar[] timestamps = new Calendar[] { new CDateTime(1000l*1310468100), new CDateTime(1000l*1310468400), new CDateTime(1000l*1310468700),
				new CDateTime(1000l*1310469000), new CDateTime(1000l*1310469300), new CDateTime(1000l*1310469600), new CDateTime(1000l*1310469900),
				new CDateTime(1000l*1310470200), new CDateTime(1000l*1310470500), new CDateTime(1000l*1310470800), new CDateTime(1000l*1310471100),
				new CDateTime(1000l*1310471400), new CDateTime(1000l*1310471700), new CDateTime(1000l*1310472000), new CDateTime(1000l*1310472300),
				new CDateTime(1000l*1310472600), new CDateTime(1000l*1310472900), new CDateTime(1000l*1310473200), new CDateTime(1000l*1310473500),
				new CDateTime(1000l*1310473800) };
		//Double[] values = new Double[] { 10d, 11.23d, 17.92d, 18.69d, 78.62d, 89.11d, 92.43d, 20.31d, 19.63d, 18.46d, 10.44d, 16.28d, 13.51d, 17.53d, 12.21, 20d, 40d, 10d, 90d, 40d };
		Double[] values = new Double[] { 10d, 11.23d, 17.92d, 18.69d, 78.62d, 89.11d, 92.43d, 89.31d, 57.39d, 18.46d, 10.44d, 16.28d, 13.51d, 17.53d, 12.21, 20d, 21.43d, 16.45d, 14.86d, 15.27d };
		createLineChart(section, timestamps, values, "%");
		
//		ServerStats stats = new GlusterServersClient().getAggregatedCPUStats();
//		List<Calendar> timestamps = new ArrayList<Calendar>();
//		List<Double> data = new ArrayList<Double>();
//		for(ServerStatsRow row : stats.getRows()) {
//			timestamps.add(new CDateTime(row.getTimestamp() * 1000));
//			// in case of CPU usage, there are three elements in usage data: user, system and total. we use total.
//			data.add(row.getUsageData().get(2));
//		}
//		
//		createLineChart(section, timestamps.toArray(new Calendar[0]), data.toArray(new Double[0]));
		createChartLinks(section, 4);
	}
	
	private Composite createChartLinks(Composite section, int columnCount) {
		GridLayout layout = new org.eclipse.swt.layout.GridLayout(columnCount, false);
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginLeft = (CHART_WIDTH - (50*columnCount)) / 2;
		Composite graphComposite = toolkit.createComposite(section, SWT.NONE);
		graphComposite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = CHART_WIDTH;
		graphComposite.setLayoutData(data);
		
		Label label1 = toolkit.createLabel(graphComposite, "1 day");
		Hyperlink link1 = toolkit.createHyperlink(graphComposite, "1 week", SWT.NONE);
		Hyperlink link2 = toolkit.createHyperlink(graphComposite, "1 month", SWT.NONE);
		Hyperlink link3 = toolkit.createHyperlink(graphComposite, "1 year", SWT.NONE);
		
		return graphComposite;
	}

	private void createSections(Composite parent) {
		String serverName = server.getName();
		form = guiHelper.setupForm(parent, toolkit, "Server Summary [" + serverName + "]");
		createServerSummarySection(server, toolkit, form);

		if (server.getStatus() == SERVER_STATUS.ONLINE) {
			createMemoryUsageSection();
			createNetworkUsageSection();
			createCPUUsageSection();
			createNetworkInterfacesSection(server, toolkit, form);
		}

		parent.layout(); // IMP: lays out the form properly
	}

	private void createServerSummarySection(GlusterServer server, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Summary", null, 2, false);

		// toolkit.createLabel(section, "Preferred Network: ", SWT.NONE);
		// toolkit.createLabel(section, server.getPreferredNetworkInterface().getName(), SWT.NONE);

		boolean online = server.getStatus() == SERVER_STATUS.ONLINE;

		if (online) {
			toolkit.createLabel(section, "Number of CPUs: ", SWT.NONE);
			toolkit.createLabel(section, "" + server.getNumOfCPUs(), SWT.NONE);

			// toolkit.createLabel(section, "CPU Usage (%): ", SWT.NONE);
			// toolkit.createLabel(section, online ? "" + server.getCpuUsage() : "NA", SWT.NONE);

			toolkit.createLabel(section, "% CPU Usage (avg): ", SWT.NONE);
			cpuGauge = new CoolGauge(section, guiHelper.getImage(IImageKeys.GAUGE_SMALL));
			cpuGauge.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			cpuGauge.setGaugeNeedleColour(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			cpuGauge.setGaugeNeedleWidth(2);
			cpuGauge.setGaugeNeedlePivot(new Point(66, 65));

			cpuGauge.setPoints(getPnts());
			cpuGauge.setLevel(server.getCpuUsage() / 100);
			cpuGauge.setToolTipText(server.getCpuUsage() + "%");

			toolkit.createLabel(section, "Memory Usage: ", SWT.NONE);
			ProgressBar memoryUsageBar = new ProgressBar(section, SWT.SMOOTH);
			memoryUsageBar.setMinimum(0);
			memoryUsageBar.setMaximum((int) Math.round(server.getTotalMemory()));
			memoryUsageBar.setSelection((int) Math.round(server.getMemoryInUse()));
			memoryUsageBar.setToolTipText("Total: " + NumberUtil.formatNumber((server.getTotalMemory()/1024)) + "GB, In Use: "
					+ NumberUtil.formatNumber((server.getMemoryInUse()/1024)) + "GB");

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

			toolkit.createLabel(section, "Disk Usage: ", SWT.NONE);
			ProgressBar diskUsageBar = new ProgressBar(section, SWT.SMOOTH);
			diskUsageBar.setMinimum(0);
			diskUsageBar.setMaximum((int) Math.round(server.getTotalDiskSpace()));
			diskUsageBar.setSelection((int) Math.round(server.getDiskSpaceInUse()));
			diskUsageBar.setToolTipText("Total: " + NumberUtil.formatNumber((server.getTotalDiskSpace()/1024))
					+ "GB, In Use: " + NumberUtil.formatNumber((server.getDiskSpaceInUse()/1024)) + "GB");
		}

		toolkit.createLabel(section, "Status: ", SWT.NONE);
		CLabel lblStatusValue = new CLabel(section, SWT.NONE);
		lblStatusValue.setText(server.getStatusStr());
		lblStatusValue.setImage(server.getStatus() == GlusterServer.SERVER_STATUS.ONLINE ? guiHelper
				.getImage(IImageKeys.STATUS_ONLINE) : guiHelper.getImage(IImageKeys.STATUS_OFFLINE));
		toolkit.adapt(lblStatusValue, true, true);
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
