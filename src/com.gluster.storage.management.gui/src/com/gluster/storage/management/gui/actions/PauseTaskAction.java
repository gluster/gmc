/**
 * PauseTaskAction.java
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
package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.TasksClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskStatus;


public class PauseTaskAction extends AbstractActionDelegate {
	private TaskInfo taskInfo;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	
	@Override
	protected void performAction(final IAction action) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				final String actionDesc = action.getDescription();

				try {
					new TasksClient().pauseTask(taskInfo.getName());
					taskInfo.setStatus(new TaskStatus(new Status(Status.STATUS_CODE_PAUSE, taskInfo.getName()
							+ " is Paused")));
					modelManager.updateTask(taskInfo);
				} catch (Exception e) {
					showErrorDialog(actionDesc,
							"Task [" + taskInfo.getName() + "] could not be Paused! Error: [" + e.getMessage() + "]");
				}
			}
		});
	}
	
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		if (selectedEntity instanceof TaskInfo) {
			taskInfo = (TaskInfo) selectedEntity;
			action.setEnabled(taskInfo.canPause() && taskInfo.getStatus().getCode() == Status.STATUS_CODE_RUNNING);
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
}
