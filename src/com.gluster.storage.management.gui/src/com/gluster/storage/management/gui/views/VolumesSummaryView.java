/**
 * VolumesSummaryView.java
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.ChartViewerComposite;
import com.gluster.storage.management.gui.utils.GUIHelper;

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
	private static final String RUNNING_TASKS = "Running Tasks";
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
				updateSummarySection() ;
			}

			@Override
			public void volumeChanged(Volume volume, Event event) {
				super.volumeChanged(volume, event);
				updateSummarySection();
			}
			
			private void updateSummarySection() {
				guiHelper.clearSection(summarySection);
				populateSummarySection();
				summarySection.layout();	
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
		createRunningTasksSection();
		createAlertsSection();

		parent.layout(); // IMP: lays out the form properly
	}

	private void createAlertsSection() {
		alertsSection = guiHelper.createSection(form, toolkit, ALERTS, null, 1, false);
		populateAlertSection();
	}

	private void populateAlertSection() {
		for (Alert alert : cluster.getAlerts()) {
			if (alert.getType() == Alert.ALERT_TYPES.OFFLINE_VOLUME_BRICKS_ALERT) {
				addAlertLabel(alertsSection, alert);
			}
		}
		alertsSection.pack(true);
		form.reflow(true);
	}

	private void addAlertLabel(Composite section, Alert alert) {
		CLabel lblAlert = new CLabel(section, SWT.NONE);
		lblAlert.setImage((alert.getType() == Alert.ALERT_TYPES.DISK_USAGE_ALERT) ? guiHelper
				.getImage(IImageKeys.LOW_DISK_SPACE_22x22) : guiHelper.getImage(IImageKeys.BRICK_OFFLINE_22x22));
		lblAlert.setText(alert.getMessage());
		lblAlert.redraw();
	}

	private void createRunningTasksSection() {
		tasksSection = guiHelper.createSection(form, toolkit, RUNNING_TASKS, null, 1, false);
		populateTasks();
	}

	private void populateTasks() {
		for (TaskInfo taskInfo : cluster.getTaskInfoList()) {
			if (taskInfo.getType() == TASK_TYPE.BRICK_MIGRATE || taskInfo.getType() == TASK_TYPE.VOLUME_REBALANCE)
				addTaskLabel(tasksSection, taskInfo);
		}
		tasksSection.pack(true);
		form.reflow(true);
	}

	private void addTaskLabel(Composite section, TaskInfo taskInfo) {
		// Task related to Volumes context
		if (taskInfo.getType() == TASK_TYPE.BRICK_MIGRATE
				|| taskInfo.getType() == TASK_TYPE.VOLUME_REBALANCE) {
			if (taskInfo.getStatus().isPercentageSupported()) {
				// TODO Progress bar or link to progress view
			}
			CLabel lblAlert = new CLabel(section, SWT.NONE);
			lblAlert.setText(taskInfo.getDescription());
			lblAlert.setImage((taskInfo.getType() == TASK_TYPE.BRICK_MIGRATE) ? guiHelper
					.getImage(IImageKeys.BRICK_MIGRATE_32x32) : guiHelper.getImage(IImageKeys.VOLUME_REBALANCE_32x32));
			lblAlert.redraw();
		}
	}

	private void createSummarySection() {
		summarySection = guiHelper.createSection(form, toolkit, AVAILABILITY, null, 2, false);
		populateSummarySection();
	}

	private void populateSummarySection() {
		if(volumes.getEntities().size() == 0) {
			toolkit.createLabel(summarySection,
					"This section will be populated after at least\none volume is created the storage cloud.");
			return;
		}

		Double[] values = new Double[] { Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.ONLINE)),
				Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.OFFLINE)) };
		createStatusChart(toolkit, summarySection, values);
	}

	private int getVolumeCountByStatus(EntityGroup<Volume> volumes, VOLUME_STATUS status) {
		int count = 0;
		for (Volume volume : (List<Volume>) volumes.getEntities()) {
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
