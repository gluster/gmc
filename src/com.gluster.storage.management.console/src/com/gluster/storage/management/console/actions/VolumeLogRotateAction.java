package com.gluster.storage.management.console.actions;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.console.actions.AbstractActionDelegate;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.views.VolumeBricksView;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.StringUtil;


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
		String brick;
		boolean confirmed = showConfirmDialog(actionDesc,
				"Are you sure you want to rotate log for volume [" + volume.getName() + "] ? ");
		if (!confirmed) {
			return;
		}

		if (bricks == null) {
			brick = "";
		} else {
			brick = StringUtil.collectionToString(GlusterCoreUtil.getQualifiedBrickList(bricks), ",");
		}
		try {
			new VolumesClient().volumeLogRotate(volume.getName(), brick);
			showInfoDialog(actionDesc, "Volume log for [" + volume.getName() + "] rotated successfully!");
		} catch (Exception e) {
			showErrorDialog(actionDesc, "Volume log rotate for [" + volume.getName()
					+ "] could not be rotate! Error: [" + e.getMessage() + "]");
		}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		volume = (Volume) guiHelper.getSelectedEntity(window, Volume.class);

		if (volume != null) {
			// a volume is selected on navigation tree. Let's check if the currently open view is volume disks view
			IWorkbenchPart view = guiHelper.getActiveView();
			if (view instanceof VolumeBricksView) {
				// volume disks view is open. check if any brick is selected
				bricks = GUIHelper.getInstance().getSelectedEntities(getWindow(), Brick.class);
			}

		}
	}

}
