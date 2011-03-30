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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.RunningTaskStatus;
import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.RunningTask;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.tabcreators.PieChartViewerComposite;

/**
 * 
 */
public class VolumesSummaryView extends ViewPart {
	public static final String ID = VolumesSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private EntityGroup<Volume> volumes;
	
	private static final String ALERTS = "Alerts";
	private static final String RUNNING_TASKS = "Running Tasks";
	private static final String VOLUMES_SUMMARY = "Volumes - Summary";
	private static final String AVAILABILITY = "Availability";
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (volumes == null) {
			Object selectedObj = guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
			if (selectedObj != null && ((EntityGroup) selectedObj).getEntityType() == Volume.class) {
				volumes = (EntityGroup<Volume>) selectedObj;
			}
		}

		createSections(parent);
	}

	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, VOLUMES_SUMMARY);
		createSummarySection();
		createRunningTasksSection();
		createAlertsSection();

		parent.layout(); // IMP: lays out the form properly
	}

	private void createAlertsSection() {
		Composite section = guiHelper.createSection(form, toolkit, ALERTS, null, 1, false);
		// toolkit.createLabel(section, "Any alerts related to volumes\nwill be displayed here.");

		Cluster cluster = GlusterDataModelManager.getInstance().getModel().getCluster();
		List<Alert> alerts = cluster.getAlerts();
		for (Alert alert : alerts) {
			if (alert.getType() == Alert.ALERT_TYPES.DISK_USAGE_ALERT ||
					alert.getType() == Alert.ALERT_TYPES.OFFLINE_VOLUME_DISKS_ALERT) {
				toolkit.createLabel(section, alert.getMessage());
			}
		}
	}

	private void createRunningTasksSection() {
		Composite section = guiHelper.createSection(form, toolkit, RUNNING_TASKS, null, 1, false);

		List<RunningTask> runningTasks = GlusterDataModelManager.getInstance().getModel().getCluster().getRunningTasks();

		for (RunningTask task : runningTasks) {
			if (task.getType() == RunningTask.TASK_TYPES.MIGRATE_DISK
					|| task.getType() == RunningTask.TASK_TYPES.VOLUME_REBALANCE) {
				if (task.getStatus().isPercentageSupported()) {
					// TODO Progress bar
				}
				toolkit.createLabel(section, task.getTaskInfo());
			}
		}
	}

	private void createSummarySection() {
		Composite section = guiHelper.createSection(form, toolkit, AVAILABILITY, null, 2, false);

		// Cluster cluster = GlusterDataModelManager.getInstance().getModel().getCluster();

		Double[] values = new Double[] { Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.ONLINE)),
				Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.OFFLINE)) };
		createStatusChart(toolkit, section, values);
	}

	@SuppressWarnings("unchecked")
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
		PieChartViewerComposite chartViewerComposite = new PieChartViewerComposite(section, SWT.NONE, categories,
				values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 250;
		data.heightHint = 250;
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
