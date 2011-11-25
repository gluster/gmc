package org.gluster.storage.management.console.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.gluster.storage.management.client.TasksClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;
import org.gluster.storage.management.core.model.TaskStatus;


public class ResumeTaskAction extends AbstractActionDelegate {
	private TaskInfo taskInfo;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();

		try {
			new TasksClient().resumeTask(taskInfo.getName());
			taskInfo.setStatus(new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, "Resumed")));
			modelManager.updateTask(taskInfo);
		} catch (Exception e) {
			showErrorDialog(actionDesc,
					"Task [" + taskInfo.getDescription() + "] could not be Resumed! Error: [" + e.getMessage() + "]");
		}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		if (selectedEntity instanceof TaskInfo) {
			taskInfo = (TaskInfo) selectedEntity;
			action.setEnabled(taskInfo.getStatus().getCode() == Status.STATUS_CODE_PAUSE);
		}
	}
	
	@Override
	public void dispose() {

	}

}
