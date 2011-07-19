/**
 * DiscoveredServerView.java
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
import java.util.Date;
import java.util.List;

import org.eclipse.birt.chart.util.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.actions.IActionConstants;
import com.gluster.storage.management.gui.utils.ChartViewerComposite;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.ibm.icu.util.Calendar;

/**
 * @author root
 * 
 */
public class ClusterSummaryView extends ViewPart {
	public static final String ID = ClusterSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private Cluster cluster;
	private GlusterDataModel model = GlusterDataModelManager.getInstance().getModel();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (cluster == null) {
			cluster = model.getCluster();
		}
		setPartName("Summary");
		createSections(parent);
	}

	private int getServerCountByStatus(Cluster cluster, SERVER_STATUS status) {
		int count = 0;
		for (GlusterServer server : cluster.getServers()) {
			if (server.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createServersSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Servers", null, 2, false);

		int onlineServerCount = getServerCountByStatus(cluster, SERVER_STATUS.ONLINE);
		int offlineServerCount = getServerCountByStatus(cluster, SERVER_STATUS.OFFLINE);
		
		toolkit.createLabel(section, "Online : ");
		Label label = toolkit.createLabel(section, "" + onlineServerCount);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
		
		toolkit.createLabel(section, "Offline : ");
		label = toolkit.createLabel(section, "" + offlineServerCount);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
	}
	
	private void createDiskSpaceSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Disk Space", null, 3, false);
		if (cluster.getServers().size() == 0) {
			toolkit.createLabel(section, "This section will be populated after at least\none server is added to the storage cloud.");
			return;
		}
		
		double totalDiskSpace = cluster.getTotalDiskSpace();
		double diskSpaceInUse = cluster.getDiskSpaceInUse();
		Double[] values = new Double[] { diskSpaceInUse, totalDiskSpace - diskSpaceInUse };
		createDiskSpaceChart(section, values);
	}

	private void createDiskSpaceChart(Composite section, Double[] values) {
		String[] categories = new String[] { "Used Space: " + NumberUtil.formatNumber((values[0] / 1024)) + " GB",
				"Free Space: " + NumberUtil.formatNumber((values[1] / 1024)) + " GB" };
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, categories, values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 400;
		data.heightHint = 150;
		data.verticalAlignment = SWT.CENTER;
		chartViewerComposite.setLayoutData(data);
	}
	
	private void createLineChart(Composite section, Calendar timestamps[], Double values[]) {
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, timestamps, values);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 400;
		data.heightHint = 300;
		data.verticalAlignment = SWT.CENTER;
		chartViewerComposite.setLayoutData(data);
	}

	private void createAlertsSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Alerts", null, 1, false);
		List<Alert> alerts = GlusterDataModelManager.getInstance().getModel().getCluster().getAlerts();

		for (Alert alert : alerts) {
				addAlertLabel(section, alert);
		}
	}
	
	private void addAlertLabel(Composite section, Alert alert) {
		CLabel lblAlert = new CLabel(section, SWT.FLAT);
		Image alertImage = null;
		switch (alert.getType()) {
		case OFFLINE_VOLUME_BRICKS_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.DISK_OFFLINE);
			break;
		case DISK_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.LOW_DISK_SPACE);
			break;
		case OFFLINE_SERVERS_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			break;
		case MEMORY_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.MEMORY_USAGE_ALERT);
			break;
		case CPU_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.SERVER_WARNING);
			break;
		}
		lblAlert.setImage(alertImage);
		lblAlert.setText(alert.getMessage());
		lblAlert.redraw();
	}

	private void createActionsSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Actions", null, 1, false);

		ImageHyperlink imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Create Volume");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.CREATE_VOLUME_BIG));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				IHandlerService hs = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					hs.executeCommand(IActionConstants.COMMAND_CREATE_VOLUME, null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Add Server(s)");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.ADD_SERVER_BIG));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				// Open the "discovered servers" view by selecting the corresponding entity in the navigation view
				EntityGroup<Server> autoDiscoveredServersEntityGroup = GlusterDataModelManager.getInstance().getModel()
						.getCluster().getEntityGroup(Server.class);

				NavigationView navigationView = (NavigationView) guiHelper.getView(NavigationView.ID);
				navigationView.selectEntity(autoDiscoveredServersEntityGroup);
			}
		});
	}

	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, "Cluster Summary");

		createServersSection();
		createDiskSpaceSection();
		createCPUUsageSection();
		createNetworkUsageSection();
		//createMemoryUsageSection();
		createActionsSection();
		createAlertsSection();
		createRunningTasksSection();

		parent.layout(); // IMP: lays out the form properly
	}

	private void createCPUUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "CPU Usage (aggregated)", null, 1, false);
		if (cluster.getServers().size() == 0) {
			toolkit.createLabel(section, "This section will be populated after at least\none server is added to the storage cloud.");
			return;
		}
		
		ServerStats stats = new GlusterServersClient().getAggregatedCPUStats();
		List<Calendar> timestamps = new ArrayList<Calendar>();
		List<Double> data = new ArrayList<Double>();
		for(ServerStatsRow row : stats.getRows()) {
			timestamps.add(new CDateTime(row.getTimestamp() * 1000));
			// in case of CPU usage, there are three elements in usage data: user, system and total. we use total.
			data.add(row.getUsageData().get(2));
		}

//		Calendar[] timestamps = new Calendar[] { new CDateTime(1000l*1310468100), new CDateTime(1000l*1310468400), new CDateTime(1000l*1310468700),
//				new CDateTime(1000l*1310469000), new CDateTime(1000l*1310469300), new CDateTime(1000l*1310469600), new CDateTime(1000l*1310469900),
//				new CDateTime(1000l*1310470200), new CDateTime(1000l*1310470500), new CDateTime(1000l*1310470800), new CDateTime(1000l*1310471100),
//				new CDateTime(1000l*1310471400), new CDateTime(1000l*1310471700), new CDateTime(1000l*1310472000), new CDateTime(1000l*1310472300),
//				new CDateTime(1000l*1310472600), new CDateTime(1000l*1310472900), new CDateTime(1000l*1310473200), new CDateTime(1000l*1310473500),
//				new CDateTime(1000l*1310473800) };
		
		//String[] timestampsarr = new String[] {"t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10", "t11", "t12", "t13", "t14", "t15", "t16", "t17", "t18", "t19", "t20"};
		//Double[] values = new Double[] { 10d, 11.23d, 17.92d, 18.69d, 78.62d, 89.11d, 92.43d, 89.31d, 57.39d, 18.46d, 10.44d, 16.28d, 13.51d, 17.53d, 12.21, 20d, 21.43d, 16.45d, 14.86d, 15.27d };
		createLineChart(section, timestamps.toArray(new Calendar[0]), data.toArray(new Double[0]));
	}

	private void createNetworkUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Network Usage", null, 1, false);
		//toolkit.createLabel(section, "Historical Network Usage graph will be displayed here.");
		
		Calendar[] timestamps = new Calendar[] { new CDateTime(1000l*1310468100), new CDateTime(1000l*1310468400), new CDateTime(1000l*1310468700),
				new CDateTime(1000l*1310469000), new CDateTime(1000l*1310469300), new CDateTime(1000l*1310469600), new CDateTime(1000l*1310469900),
				new CDateTime(1000l*1310470200), new CDateTime(1000l*1310470500), new CDateTime(1000l*1310470800), new CDateTime(1000l*1310471100),
				new CDateTime(1000l*1310471400), new CDateTime(1000l*1310471700), new CDateTime(1000l*1310472000), new CDateTime(1000l*1310472300),
				new CDateTime(1000l*1310472600), new CDateTime(1000l*1310472900), new CDateTime(1000l*1310473200), new CDateTime(1000l*1310473500),
				new CDateTime(1000l*1310473800) };
//		Date[] timestamps = new Date[] { new Date(1310468100), new Date(1310468400), new Date(1310468700),
//				new Date(1310469000), new Date(1310469300), new Date(1310469600), new Date(1310469900),
//				new Date(1310470200), new Date(1310470500), new Date(1310470800), new Date(1310471100),
//				new Date(1310471400), new Date(1310471700), new Date(1310472000), new Date(1310472300),
//				new Date(1310472600), new Date(1310472900), new Date(1310473200), new Date(1310473500),
//				new Date(1310473800) };
		String[] timestampsarr = new String[] {"t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9", "t10", "t11", "t12", "t13", "t14", "t15", "t16", "t17", "t18", "t19", "t20"};
		Double[] values = new Double[] { 32d, 31.23d, 27.92d, 48.69d, 58.62d, 49.11d, 72.43d, 69.31d, 87.39d, 78.46d, 60.44d, 56.28d, 33.51d, 27.53d, 12.21, 10d, 21.43d, 36.45d, 34.86d, 35.27d };
		createLineChart(section, timestamps, values);
	}

	private void createRunningTasksSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Running Tasks", null, 1, false);

		for (TaskInfo taskInfo : cluster.getTaskInfoList()) {
			addTaskLabel(section, taskInfo);
		}
	}

	private void addTaskLabel(Composite section, TaskInfo taskInfo) {
		//TODO: create link and open the task progress view
		CLabel lblAlert = new CLabel(section, SWT.NONE);
		lblAlert.setText(taskInfo.getDescription());
		
		Image taskImage = null;
		switch(taskInfo.getType()) {
		case DISK_FORMAT:
			taskImage = guiHelper.getImage(IImageKeys.DISK);
			break;
		case BRICK_MIGRATE:
			taskImage = guiHelper.getImage(IImageKeys.BRICK_MIGRATE);
			break;
		case VOLUME_REBALANCE:
			taskImage = guiHelper.getImage(IImageKeys.VOLUME_REBALANCE);
			break;
		}
		lblAlert.setImage(taskImage);
		lblAlert.redraw();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (form != null) {
			form.setFocus();
		}
	}
}
