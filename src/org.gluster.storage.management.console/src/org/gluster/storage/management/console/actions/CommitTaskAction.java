package org.gluster.storage.management.console.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.gluster.storage.management.client.TasksClient;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;
import org.gluster.storage.management.core.model.TaskStatus;
import org.gluster.storage.management.core.model.Volume;


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
			showInfoDialog(actionDesc, "Commit successful");
		} catch (Exception e) {
			showErrorDialog(actionDesc,
					"Task [" + taskInfo.getName() + "] could not be Committed! Error: [" + e.getMessage() + "]");
			return; // Prevent to update model
		}
		
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			@Override
			public void run() {
				try {
					String volumeName = taskInfo.getReference().split("#")[0]; //Extract volume name from reference
					Volume oldVolume = modelManager.getModel().getCluster().getVolume(volumeName);
					Volume newVolume = (new VolumesClient()).getVolume(volumeName);
					
					modelManager.volumeChanged(oldVolume, newVolume);
				} catch (Exception e) {
					String errMsg = "Volume brick update failed! [" + e.getMessage() + "]";
					logger.error(errMsg, e);
					showInfoDialog(actionDesc, errMsg);
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
			action.setEnabled(taskInfo.getCommitSupported()
					&& taskInfo.getStatus().getCode() == Status.STATUS_CODE_COMMIT_PENDING);
		}
	}
	
	public void updateVolume(String volumeName) {
		
	}

	@Override
	public void dispose() {

	}
}
