package org.gluster.storage.management.console.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.gluster.storage.management.client.TasksClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;


public class StopTaskAction extends AbstractActionDelegate {
	private TaskInfo taskInfo;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();

		try {
			new TasksClient().stopTask(taskInfo.getName());
			// On successful stop clear from the task list
			modelManager.removeTask(taskInfo);
			action.setEnabled(false); // TODO disable other task buttons
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
