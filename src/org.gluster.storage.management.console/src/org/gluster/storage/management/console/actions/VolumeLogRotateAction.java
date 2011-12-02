/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.actions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.VolumeBricksView;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.utils.GlusterCoreUtil;



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
