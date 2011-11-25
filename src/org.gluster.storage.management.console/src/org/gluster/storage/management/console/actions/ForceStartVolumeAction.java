package org.gluster.storage.management.console.actions;

import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.VolumeBricksView;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import org.gluster.storage.management.core.utils.StringUtil;


public class ForceStartVolumeAction extends AbstractActionDelegate {

	private Volume volume;
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private Set<Brick> bricks;
	
	@Override
	public void dispose() {
		
	}

	@Override
	protected void performAction(IAction action) {
		// volume brick service will be started, do you want to continue?
		final String actionDesc = action.getDescription();
		boolean confirmed = showConfirmDialog(
				actionDesc,
				"The offline Bricks [" + StringUtil.collectionToString(bricks, ", ") + "] of Volume ["
						+ volume.getName() + "] will be started. Are you sure you want to continue?");
		if (!confirmed) {
			return;
		}
		try {
			new VolumesClient().startVolume(volume.getName(), true);
			showInfoDialog(actionDesc, "Offline Bricks of Volume [" + volume.getName() + "] started successfully!");
		} catch (Exception e) {
			showErrorDialog(actionDesc, e.getMessage());
		}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		action.setEnabled(false);
		volume = guiHelper.getSelectedEntity(window, Volume.class);
		if (volume != null) {
			// a volume is selected on navigation tree. Let's check if the currently open view is volume bricks view
			IWorkbenchPart view = guiHelper.getActiveView();
			if (view instanceof VolumeBricksView) {
				// volume bricks view is open. check if any offline brick is selected
				bricks = GUIHelper.getInstance().getSelectedEntities(getWindow(), Brick.class);
				for (Brick brick : bricks) {
					if (brick.getStatus() == BRICK_STATUS.OFFLINE) {
						action.setEnabled(true);
					} else {
						// if any one of the selected brick is online, the disable the button
						action.setEnabled(false);
						break;
					}
				}
			}
		}
	}

}
