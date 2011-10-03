package com.gluster.storage.management.console.actions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.views.VolumeBricksView;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;


public class VolumeLogRotateAction extends AbstractActionDelegate {

	private Volume volume;
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private Set<Brick> bricks;
	
	@Override
	public void dispose() {
	}

	@Override
	protected void performAction(IAction action) {
		final String actionDesc = action.getDescription();
		List<String> selectedBricks = new ArrayList<String>();
		boolean confirmed = showConfirmDialog(actionDesc,
				"Are you sure you want to Rotate logs for volume [" + volume.getName() + "] ? ");
		if (!confirmed) {
			return;
		}

		if (bricks != null) {
			selectedBricks = GlusterCoreUtil.getQualifiedBrickList(bricks);
		}
		try {
			new VolumesClient().volumeLogRotate(volume.getName(), selectedBricks);
			showInfoDialog(actionDesc, "Volume logs for [" + volume.getName() + "] rotated successfully!");
		} catch (Exception e) {
			showErrorDialog(actionDesc, "Volume [" + volume.getName() + "] log rotation failed!  Error: [" + e.getMessage() + "]");
		}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		volume = guiHelper.getSelectedEntity(window, Volume.class);

		if (volume != null) {
			// a volume is selected on navigation tree. Let's check if the currently open view is volume bricks view
			IWorkbenchPart view = guiHelper.getActiveView();
			if (view instanceof VolumeBricksView) {
				// volume bricks view is open. check if any brick is selected
				bricks = GUIHelper.getInstance().getSelectedEntities(getWindow(), Brick.class);
			}
		}
	}

}
