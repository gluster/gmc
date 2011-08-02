/**
 * GlusterServersSummaryView.java
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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Alert.ALERT_TYPES;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.ChartViewerComposite;
import com.gluster.storage.management.gui.utils.GUIHelper;

/**
 *
 */
public class GlusterServersSummaryView extends ViewPart {
	public static final String ID = GlusterServersSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private ClusterListener clusterListener;
	private EntityGroup<GlusterServer> servers;
	private Composite alertsSection;
	private Composite serversAvailabilitySection;
	private Composite tasksSection;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		if (servers == null) {
			servers = (EntityGroup<GlusterServer>)guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
		}
		setPartName("Summary");
		createSections(parent);
		
		clusterListener = new DefaultClusterListener() {
			@Override
			public void serverAdded(GlusterServer server) {
				super.serverAdded(server);
				updateServerAvailabilitySection();
			}
			
			@Override
			public void serverRemoved(GlusterServer server) {
				super.serverRemoved(server);
				updateServerAvailabilitySection();
			}
			
			@Override
			public void serverChanged(GlusterServer server, Event event) {
				super.serverChanged(server, event);
				updateServerAvailabilitySection();
			}
			
			private void updateServerAvailabilitySection() {
				guiHelper.clearSection(serversAvailabilitySection);
				populateAvailabilitySection();
			}
			
			@Override
			public void alertsGenerated() {
				super.alertsGenerated();
				guiHelper.clearSection(alertsSection);
				populateAlertSection();
			}
			
			@Override
			public void taskAdded(TaskInfo taskInfo) {
				super.taskAdded(taskInfo);
				updateTasksSection();
			}
			
			@Override
			public void taskRemoved(TaskInfo taskInfo) {
				super.taskRemoved(taskInfo);
				updateTasksSection();
			}
			
			@Override
			public void taskUpdated(TaskInfo taskInfo) {
				super.taskUpdated(taskInfo);
				updateTasksSection();
			}
			
			private void updateTasksSection() {
				guiHelper.clearSection(tasksSection);
				populateTasksSection();
			}
		};
		GlusterDataModelManager.getInstance().addClusterListener(clusterListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(clusterListener);
	}

	/**
	 * @param parent
	 */
	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, "Servers - Summary");
		
		createSummarySection();
		createRunningTasksSection();
		createAlertsSection();
		
		parent.layout(); // IMP: lays out the form properly
	}
	
	private void createSummarySection() {
		serversAvailabilitySection = guiHelper.createSection(form, toolkit, "Availability", null, 2, false);
		populateAvailabilitySection();
	}

	private void populateAvailabilitySection() {
		if (servers.getEntities().size() == 0) {
			toolkit.createLabel(serversAvailabilitySection,
					"This section will be populated after at least\none server is added to the storage cloud.");
			return;
		}

		Double[] values = new Double[] { Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.ONLINE)),
				Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.OFFLINE)) };
		createStatusChart(serversAvailabilitySection, values);
	}	
	
	private int getServerCountByStatus(EntityGroup<GlusterServer> servers, SERVER_STATUS status) {
		int count = 0;
		for (GlusterServer server : (List<GlusterServer>)servers.getEntities()) {
			if (server.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createStatusChart(Composite section, Double[] values) {
		String[] categories = new String[] { "Online", "Offline" };
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, categories, values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 300;
		data.heightHint = 150;
		chartViewerComposite.setLayoutData(data);	
	}

	private void createAlertsSection() {
		alertsSection = guiHelper.createSection(form, toolkit, "Alerts", null, 1, false);
		populateAlertSection();
	}
	
	private void populateAlertSection() {
		List<Alert> alerts = GlusterDataModelManager.getInstance().getModel().getCluster().getAlerts();

		for (Alert alert : alerts) {
			if (alert.getType() != ALERT_TYPES.OFFLINE_VOLUME_BRICKS_ALERT) {
				addAlertLabel(alertsSection, alert);
			}
		}
		alertsSection.pack(true);
		form.reflow(true);
	}
	
	private void addAlertLabel(Composite section, Alert alert) {
		CLabel lblAlert = new CLabel(section, SWT.FLAT);
		Image alertImage = null;
		switch (alert.getType()) {
		case OFFLINE_VOLUME_BRICKS_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.BRICK_OFFLINE_22x22);
			break;
		case DISK_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.LOW_DISK_SPACE_22x22);
			break;
		case OFFLINE_SERVERS_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.SERVER_OFFLINE_22x22);
			break;
		case MEMORY_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.MEMORY_USAGE_ALERT_22x22);
			break;
		case CPU_USAGE_ALERT:
			alertImage = guiHelper.getImage(IImageKeys.SERVER_WARNING_22x22);
			break;
		}
		lblAlert.setImage(alertImage);
		lblAlert.setText(alert.getMessage());
		lblAlert.redraw();
	}
	
	private void createRunningTasksSection() {
		tasksSection = guiHelper.createSection(form, toolkit, "Running Tasks", null, 1, false);
		populateTasksSection();

	}

	private void populateTasksSection() {
		for (TaskInfo taskInfo : GlusterDataModelManager.getInstance().getModel().getCluster().getTaskInfoList()) {
			// Exclude volume related tasks
			if (taskInfo.getType() != TASK_TYPE.VOLUME_REBALANCE && taskInfo.getType() != TASK_TYPE.BRICK_MIGRATE) {
				addTaskLabel(tasksSection, taskInfo);
			}
		}
		tasksSection.layout();
		form.reflow(true);
	}

	private void addTaskLabel(Composite section, TaskInfo taskInfo) {
		CLabel lblAlert = new CLabel(section, SWT.NONE);
		lblAlert.setText(taskInfo.getDescription());
		
		Image taskImage = null;
		switch(taskInfo.getType()) {
		case DISK_FORMAT:
			taskImage = guiHelper.getImage(IImageKeys.DISK_INITIALIZING_22x22);
			break;
		case BRICK_MIGRATE:
			taskImage = guiHelper.getImage(IImageKeys.BRICK_MIGRATE_22x22);
			break;
		case VOLUME_REBALANCE:
			taskImage = guiHelper.getImage(IImageKeys.VOLUME_REBALANCE_22x22);
			break;
		}
		lblAlert.setImage(taskImage);
		lblAlert.redraw();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}
}