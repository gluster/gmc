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
import org.gluster.storage.management.core.model.Cluster;
import org.gluster.storage.management.core.model.ClusterListener;
import org.gluster.storage.management.core.model.DefaultClusterListener;
import org.gluster.storage.management.core.model.EntityGroup;
import org.gluster.storage.management.core.model.Event;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import org.gluster.storage.management.core.model.Volume.VOLUME_STATUS;


/**
 * 
 */
public class VolumesSummaryView extends ViewPart {
	public static final String ID = VolumesSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private EntityGroup<Volume> volumes;
	private Cluster cluster = GlusterDataModelManager.getInstance().getModel().getCluster();
	private ClusterListener clusterListener;

	private static final String ALERTS = "Alerts";
	private static final String VOLUMES_SUMMARY = "Volumes - Summary";
	private static final String AVAILABILITY = "Availability";
	private Composite alertsSection;
	private Composite tasksSection;
	private Composite summarySection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void createPartControl(Composite parent) {
		if (volumes == null) {
			Object selectedObj = guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
			if (selectedObj != null && ((EntityGroup) selectedObj).getEntityType() == Volume.class) {
				volumes = (EntityGroup<Volume>) selectedObj;
			}
		}
		
		setPartName("Summary");
		createSections(parent);

		clusterListener = new DefaultClusterListener() {
			@Override
			public void volumeCreated(Volume volume) {
				super.volumeCreated(volume);
				updateSummarySection();
			}

			@Override
			public void volumeDeleted(Volume volume) {
				super.volumeDeleted(volume);
				updateSummarySection();
			}

			@Override
			public void volumeChanged(Volume volume, Event event) {
				super.volumeChanged(volume, event);
				updateSummarySection();
			}
			
			private void updateAlertSection() {
				guiHelper.clearSection(alertsSection);
				populateAlertSection();
			}
			
			private void updateSummarySection() {
				guiHelper.clearSection(summarySection);
				populateSummarySection();
				summarySection.layout();
				form.reflow(true);
			}

			@Override
			public void alertsGenerated() {
				super.alertsGenerated();
				guiHelper.clearSection(alertsSection);
				populateAlertSection();
			}
			
			@Override
			public void alertRemoved(Alert alert) {
				super.alertRemoved(alert);
				updateAlertSection();
			}
			
			@Override
			public void alertCreated(Alert alert) {
				super.alertCreated(alert);
				updateAlertSection();
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
				populateTasks();
			}
		};
		GlusterDataModelManager.getInstance().addClusterListener(clusterListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(clusterListener);
	}

	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, VOLUMES_SUMMARY);
		createSummarySection();
		createTasksSection();
		createAlertsSection();

		parent.layout(); // IMP: lays out the form properly
	}

	private void createAlertsSection() {
		alertsSection = guiHelper.createSection(form, toolkit, ALERTS, null, 1, false);
		populateAlertSection();
	}

	private void populateAlertSection() {
		for (Alert alert : cluster.getAlerts()) {
			if (alert.getType() == Alert.ALERT_TYPES.OFFLINE_VOLUME_BRICKS_ALERT
					|| alert.getType() == Alert.ALERT_TYPES.OFFLINE_VOLUME_ALERT) {
				addAlertLabel(alertsSection, alert);
			}
		}
		alertsSection.pack(true);
		form.reflow(true);
	}

	private void addAlertLabel(Composite section, Alert alert) {
		CLabel lblAlert = new CLabel(section, SWT.NONE);
		
		Image alertImage = null;
		switch (alert.getType()) {
			case OFFLINE_VOLUME_BRICKS_ALERT:
				alertImage = guiHelper.getImage(IImageKeys.BRICK_OFFLINE_22x22);
				break;
			case OFFLINE_VOLUME_ALERT:
				alertImage = guiHelper.getImage(IImageKeys.VOLUME_OFFLINE_22x22);
				break;
		}
		lblAlert.setImage(alertImage);
		lblAlert.setText(alert.getMessage());
		lblAlert.redraw();
	}

	private void createTasksSection() {
		tasksSection = guiHelper.createSection(form, toolkit, CoreConstants.RUNNING_TASKS, null, 1, false);
		populateTasks();
	}

	private void populateTasks() {
		for (TaskInfo taskInfo : cluster.getTaskInfoList()) {
			if ((taskInfo.getType() == TASK_TYPE.BRICK_MIGRATE || taskInfo.getType() == TASK_TYPE.VOLUME_REBALANCE)
					&& taskInfo.getStatus().getCode() != Status.STATUS_CODE_SUCCESS)
				addTaskLabel(tasksSection, taskInfo);
		}
		tasksSection.pack(true);
		form.reflow(true);
	}

	private void addTaskLabel(Composite section, TaskInfo taskInfo) {
		// Task related to Volumes context
		if (taskInfo.getStatus().isPercentageSupported()) {
			// TODO Progress bar or link to progress view
		}
		
		CLabel lblTask = new CLabel(section, SWT.NONE);
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
		lblTask.setImage((taskInfo.getType() == TASK_TYPE.BRICK_MIGRATE) ? guiHelper
				.getImage(IImageKeys.BRICK_MIGRATE_32x32) : guiHelper.getImage(IImageKeys.VOLUME_REBALANCE_32x32));
		lblTask.redraw();
	}

	private void createSummarySection() {
		summarySection = guiHelper.createSection(form, toolkit, AVAILABILITY, null, 2, false);
		populateSummarySection();
	}

	private void populateSummarySection() {
		if(volumes.getEntities().size() == 0) {
			toolkit.createLabel(summarySection,
					"This section will be populated after at least" + CoreConstants.NEWLINE +"one volume is created the storage cloud.");
			return;
		}

		Double[] values = new Double[] { Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.ONLINE)),
				Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.OFFLINE)) };
		createStatusChart(toolkit, summarySection, values);
	}

	private int getVolumeCountByStatus(EntityGroup<Volume> volumes, VOLUME_STATUS status) {
		int count = 0;
		for (Volume volume : volumes.getEntities()) {
			if (volume.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createStatusChart(FormToolkit toolkit, Composite section, Double[] values) {
		String[] categories = new String[] { "Online", "Offline" };
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, categories, values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 300;
		data.heightHint = 150;
		chartViewerComposite.setLayoutData(data);
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
