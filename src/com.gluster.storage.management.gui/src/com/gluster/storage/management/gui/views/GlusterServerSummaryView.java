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

import org.eclipse.jface.layout.TableColumnLayout;
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
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.NetworkInterfaceTableLabelProvider;
import com.gluster.storage.management.gui.toolbar.GlusterToolbarManager;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.richclientgui.toolbox.gauges.CoolGauge;

public class GlusterServerSummaryView extends ViewPart {
	public static final String ID = GlusterServerSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private GlusterServer server;
	private ClusterListener serverChangedListener;

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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(serverChangedListener);
	}

	private void createSections(Composite parent) {
		String serverName = server.getName();
		form = guiHelper.setupForm(parent, toolkit, "Server Summary [" + serverName + "]");
		createServerSummarySection(server, toolkit, form);

		if (server.getStatus() == SERVER_STATUS.ONLINE) {
			Composite section = createNetworkInterfacesSection(server, toolkit, form);
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
