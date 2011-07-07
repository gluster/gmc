package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.TasksClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;

public class ClearTaskAction extends AbstractActionDelegate {
	private TaskInfo taskInfo;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();

		try {
			new TasksClient().deleteTask(taskInfo.getName()); // taskId
			modelManager.removeTask(taskInfo);
			action.setEnabled(false); // TODO disable other task buttons
		} catch (Exception e) {
			showErrorDialog(actionDesc,
					"Task [" + taskInfo.getName() + "] could not be cleared! Error: [" + e.getMessage() + "]");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		if (selectedEntity instanceof TaskInfo) {
			taskInfo = (TaskInfo) selectedEntity;
			action.setEnabled(taskInfo.getStatus().getCode() == Status.STATUS_CODE_SUCCESS 
							|| taskInfo.getStatus().getCode() == Status.STATUS_CODE_FAILURE);
		} else {
			action.setEnabled(false);
		}
	}

	@Override
	public void dispose() {

	}
}
