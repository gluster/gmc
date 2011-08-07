/*******************************************************************************
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
 *******************************************************************************/
package com.gluster.storage.management.console.actions;

import java.net.URI;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.TasksClient;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.Volume;

public class RebalanceVolumeAction extends AbstractActionDelegate {
	private Volume volume;
	private GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();
		try {
			URI uri = new VolumesClient().rebalanceStart(volume.getName(), false, false, false);
			// Add the task to model
			TasksClient taskClient = new TasksClient();
			TaskInfo taskInfo = taskClient.getTaskInfo(uri);
			if (taskInfo != null && taskInfo instanceof TaskInfo) {
				GlusterDataModelManager.getInstance().getModel().getCluster().addTaskInfo(taskInfo);
			}
			showInfoDialog(actionDesc, "Volume [" + volume.getName() + "] rebalance started successfully!");
		} catch (Exception e) {
			showErrorDialog(actionDesc, "Volume rebalance could not be started on [" + volume.getName() + "]! Error: ["
					+ e.getMessage() + "]");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		Volume selectedVolume = guiHelper.getSelectedEntity(getWindow(), Volume.class);
		if (selectedVolume != null) {
			volume = (Volume) selectedVolume;
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}
}