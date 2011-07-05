package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.TasksClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskStatus;

public class StopTaskAction extends AbstractActionDelegate {
	private TaskInfo taskInfo;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();

		try {
			new TasksClient().resumeTask(taskInfo.getName());
			taskInfo.setStatus(new TaskStatus(new Status(Status.STATUS_CODE_SUCCESS, "Stopped")));
			modelManager.updateTask(taskInfo);
		} catch (Exception e) {
			showErrorDialog(actionDesc,
					"Task [" + taskInfo.getDescription() + "] could not be Stopped! Error: [" + e.getMessage() + "]");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		if (selectedEntity instanceof TaskInfo) {
			taskInfo = (TaskInfo) selectedEntity;
			action.setEnabled(taskInfo.getStopSupported()
					&& (taskInfo.getStatus().getCode() == Status.STATUS_CODE_PAUSE 
							|| taskInfo.getStatus().getCode() == Status.STATUS_CODE_RUNNING));
		}
	}

	@Override
	public void dispose() {

	}

}
