package com.gluster.storage.management.gui.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.utils.StringUtil;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.VolumeBricksView;

public class RemoveBrickAction extends AbstractActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private Set<Brick> bricks;
	private Volume volume;
	boolean confirmDelete = false;

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();
		List<String> brickList = getBrickList(bricks);
		Integer deleteOption = new MessageDialog(getShell(), "Remove Bricks(s)", GUIHelper.getInstance().getImage(
				IImageKeys.VOLUME_16x16), "Are you sure you want to remove following bricks from volume [" + volume.getName()
				+ "] ? " + CoreConstants.NEWLINE + StringUtil.collectionToString(brickList, ", "), MessageDialog.QUESTION, new String[] {
				"Cancel", "Remove bricks, delete data", "Remove bricks, keep data" }, -1).open();
		if (deleteOption <= 0) { // By Cancel button(0) or Escape key(-1)
			return;
		}

		if (deleteOption == 1) {
			confirmDelete = true;
		}
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				VolumesClient client = new VolumesClient();
				try {
					client.removeBricks(volume.getName(), bricks, confirmDelete);
					// Update model with removed bricks in the volume
					modelManager.removeBricks(volume, bricks);

					showInfoDialog(actionDesc, "Volume [" + volume.getName() + "] bricks(s) removed successfully!");
				} catch (Exception e) {
					showErrorDialog(actionDesc, "Volume [" + volume.getName()
							+ "] bricks(s) could not be removed! Error: [" + e.getMessage() + "]");
				}
			}
		});
	}

	@Override
	public void dispose() {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		action.setEnabled(false);
		volume = (Volume) guiHelper.getSelectedEntity(window, Volume.class);
		if (volume != null) {
			// a volume is selected on navigation tree. Let's check if the currently open view is volume disks view
			IWorkbenchPart view = guiHelper.getActiveView();
			if (view instanceof VolumeBricksView) {
				// volume disks view is open. check if any brick is selected
				bricks = GUIHelper.getInstance().getSelectedEntities(getWindow(), Brick.class);
				action.setEnabled(bricks.size() > 0);
			}
		}
	}

	private List<String> getBrickList(Set<Brick> bricks) {
		List<String> brickList = new ArrayList<String>();
		for (Brick brick : bricks) {
			brickList.add(brick.getServerName() + ":" + brick.getBrickDirectory());
		}
		return brickList;
	}
}