package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.TasksClient;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.model.Volume;

public class CommitTaskAction extends AbstractActionDelegate {
	private TaskInfo taskInfo;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();
		try {
			new TasksClient().commitTask(taskInfo.getName());
			taskInfo.setStatus(new TaskStatus(new Status(Status.STATUS_CODE_SUCCESS, "Committed")));
			modelManager.removeTask(taskInfo);
			Volume volume = (new VolumesClient()).getVolume(taskInfo.getReference());
			modelManager.updateVolumeBricks(modelManager.getModel().getCluster().getVolume(taskInfo.getReference()),
					volume.getBricks());

			showInfoDialog(actionDesc, "Commit successful");
		} catch (Exception e) {
			showErrorDialog(actionDesc,
					"Task [" + taskInfo.getName() + "] could not be Committed! Error: [" + e.getMessage() + "]");
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		if (selectedEntity instanceof TaskInfo) {
			taskInfo = (TaskInfo) selectedEntity;
			action.setEnabled(taskInfo.getCommitSupported()
					&& taskInfo.getStatus().getCode() == Status.STATUS_CODE_COMMIT_PENDING);
		}
	}

	@Override
	public void dispose() {

	}
}
