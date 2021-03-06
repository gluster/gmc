/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.views;

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
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.IImageKeys;
import org.gluster.storage.management.console.utils.ChartViewerComposite;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.model.Alert;
import org.gluster.storage.management.core.model.ClusterListener;
import org.gluster.storage.management.core.model.DefaultClusterListener;
import org.gluster.storage.management.core.model.EntityGroup;
import org.gluster.storage.management.core.model.Event;
import org.gluster.storage.management.core.model.GlusterServer;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;
import org.gluster.storage.management.core.model.Alert.ALERT_TYPES;
import org.gluster.storage.management.core.model.Server.SERVER_STATUS;
import org.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;


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
	@Override
	public void createPartControl(Composite parent) {
		if (servers == null) {
			servers = guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
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
			toolkit.createLabel(serversAvailabilitySection, "This section will be populated after at least"
					+ CoreConstants.NEWLINE + "one server is added to the storage cloud.");
			return;
		}

		Double[] values = new Double[] { Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.ONLINE)),
				Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.OFFLINE)) };
		createStatusChart(serversAvailabilitySection, values);
	}	
	
	private int getServerCountByStatus(EntityGroup<GlusterServer> servers, SERVER_STATUS status) {
		int count = 0;
		for (GlusterServer server : servers.getEntities()) {
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
			if (alert.getType() == ALERT_TYPES.DISK_USAGE_ALERT || alert.getType() != ALERT_TYPES.OFFLINE_SERVERS_ALERT
					|| alert.getType() == ALERT_TYPES.MEMORY_USAGE_ALERT
					|| alert.getType() == ALERT_TYPES.CPU_USAGE_ALERT) {
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
		tasksSection = guiHelper.createSection(form, toolkit, CoreConstants.RUNNING_TASKS, null, 1, false);
		populateTasksSection();
	}

	private void populateTasksSection() {
		for (TaskInfo taskInfo : GlusterDataModelManager.getInstance().getModel().getCluster().getTaskInfoList()) {
			// Exclude volume related tasks
			if (taskInfo.getStatus().getCode() != Status.STATUS_CODE_SUCCESS
					&& taskInfo.getType() != TASK_TYPE.VOLUME_REBALANCE
					&& taskInfo.getType() != TASK_TYPE.BRICK_MIGRATE) {
				addTaskLabel(tasksSection, taskInfo);
			}
		}
		tasksSection.layout();
		form.reflow(true);
	}

	private void addTaskLabel(Composite section, TaskInfo taskInfo) {
		CLabel lblTask = new CLabel(section, SWT.NONE);
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
		
		String description = taskInfo.getDescription();
		switch (taskInfo.getStatus().getCode()) {
		case Status.STATUS_CODE_PAUSE:
			description += " (paused)";
			break;
		case Status.STATUS_CODE_COMMIT_PENDING:
			description += " (commit pending)";
			break;
		case Status.STATUS_CODE_FAILURE:
			description += " (failed)";
			break;
		}
		
		lblTask.setText(description);
		lblTask.setImage(taskImage);
		lblTask.redraw();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}
}