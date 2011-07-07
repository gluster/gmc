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

import java.util.List;

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
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.actions.IActionConstants;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.utils.PieChartViewerComposite;

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

	private int getVolumeCountByStatus(Cluster cluster, VOLUME_STATUS status) {
		int count = 0;
		for (Volume volume : cluster.getVolumes()) {
			if (volume.getStatus() == status) {
				count++;
			}
		}
		return count;
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

	private void createVolumesSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Volumes", null, 1, false);

		Double[] values = new Double[] { Double.valueOf(getVolumeCountByStatus(cluster, VOLUME_STATUS.ONLINE)),
				Double.valueOf(getVolumeCountByStatus(cluster, VOLUME_STATUS.OFFLINE)) };
		createDiskSpaceChart(toolkit, section, values);
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
		createDiskSpaceChart(toolkit, section, values);
	}

	private int getDiskCountByStatus(Cluster cluster, DEVICE_STATUS status) {
		int diskCount = 0;
		for(GlusterServer server : cluster.getServers()) {
			for(Disk disk : server.getDisks()) {
				if(disk.getStatus() == status) {
					diskCount++;
				}
			}
		}
		return diskCount;
	}

	private int getDiskCount(Cluster cluster) {
		int diskCount = 0;
		for(GlusterServer server : cluster.getServers()) {
			diskCount += server.getDisks().size();
		}
		return diskCount;
	}

	private void createDiskSpaceChart(FormToolkit toolkit, Composite section, Double[] values) {
		String[] categories = new String[] { "Used Space: " + NumberUtil.formatNumber((values[0] / 1024)) + " GB",
				"Free Space: " + NumberUtil.formatNumber((values[1] / 1024)) + " GB"};
		PieChartViewerComposite chartViewerComposite = new PieChartViewerComposite(section, SWT.NONE, categories,
				values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 400;
		data.heightHint = 150;
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
			alertImage = guiHelper.getImage(IImageKeys.DISK);
			break;
		case OFFLINE_SERVERS_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			break;
		case MEMORY_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.MEMORY);
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
						.getCluster().getAutoDiscoveredServersEntityGroup();

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

	private void createMemoryUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Memory Usage (aggregated)", null, 1, false);
		toolkit.createLabel(section, "Historical Memory Usage graph aggregated across all servers will be displayed here.");
	}

	private void createCPUUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "CPU Usage (aggregated)", null, 1, false);
		if (cluster.getServers().size() == 0) {
			toolkit.createLabel(section, "This section will be populated after at least\none server is added to the storage cloud.");
			return;
		}
		toolkit.createLabel(section, "Historical CPU Usage graph aggregated across\nall servers will be displayed here.");
	}

	private void createNetworkUsageSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Network Usage", null, 1, false);
		toolkit.createLabel(section, "Historical Network Usage graph will be displayed here.");
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
