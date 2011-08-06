/*******************************************************************************
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.gluster.storage.management.console.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.IImageKeys;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.utils.StringUtil;

public class DeleteVolumeAction extends AbstractActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private List<Volume> volumes = new ArrayList<Volume>();
	private List<String> selectedVolumeNames = new ArrayList<String>();
	private List<String> onlineVolumeNames = new ArrayList<String>();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();
		VolumesClient vc = new VolumesClient();

		collectVolumeNames();
		String warningMessage;
		if (onlineVolumeNames.size() > 0) { // There are some online volumes, get confirmation to stop and delete all
											// the volumes
			warningMessage = "Following volume(s) [" + StringUtil.collectionToString(onlineVolumeNames, ", ")
					+ "] are online, " + CoreConstants.NEWLINE + "Are you sure to continue?";
		} else {
			warningMessage = "Are you sure to delete the following volume(s) ["
					+ StringUtil.collectionToString(selectedVolumeNames, ", ") + "] ?";
		}

		Integer deleteOption = new MessageDialog(getShell(), "Delete Volume", GUIHelper.getInstance().getImage(
				IImageKeys.VOLUME_16x16), warningMessage, MessageDialog.QUESTION, new String[] { "Cancel",
				"Delete volume and data", "Delete volume, keep data" }, -1).open();
		if (deleteOption <= 0) { // By Cancel button(0) or Escape key(-1)
			return;
		}

		boolean confirmDelete = (deleteOption == 1) ? true : false;
		List<String> deletedVolumes = new ArrayList<String>();
		List<String> failedVolumes = new ArrayList<String>();
		String errorMessage = "";

		for (Volume volume : volumes) {
			try {
				if (volume.getStatus() == VOLUME_STATUS.ONLINE) { // stop if online volume
					vc.stopVolume(volume.getName());
				}
				vc.deleteVolume(volume, confirmDelete);
				modelManager.deleteVolume(volume);
				deletedVolumes.add(volume.getName());
			} catch (Exception e) {
				// there is a possibility that the error was in post-delete operation, which means
				// volume was deleted, but some other error happened. check if this is the case.
				if (vc.volumeExists(volume.getName())) {
					errorMessage += CoreConstants.NEWLINE + "Volume [" + volume.getName()
							+ "] could not be deleted! Error: [" + e.getMessage() + "]";
					failedVolumes.add(volume.getName());
				} else {
					errorMessage += CoreConstants.NEWLINE + "Volume deleted, but following error(s) occured: ["
							+ e.getMessage() + "]";
					modelManager.deleteVolume(volume);
					deletedVolumes.add(volume.getName());
				}
			}
		}

		// Display the success or failure info
		if (deletedVolumes.size() == 0) { // No volume(s) deleted successfully
			showErrorDialog(actionDesc, "Following volume(s) [" + StringUtil.collectionToString(failedVolumes, ", ")
					+ "] could not be delete! " + CoreConstants.NEWLINE + "Error: [" + errorMessage + "]");
		} else {
			String info = "Following volumes [" + StringUtil.collectionToString(deletedVolumes, ", ")
					+ "] are deleted successfully!";
			if (errorMessage != "") {
				info += CoreConstants.NEWLINE + CoreConstants.NEWLINE + "Following volumes ["
						+ StringUtil.collectionToString(failedVolumes, ", ") + "] are failed to delete! ["
						+ errorMessage + "]";
			}
			showInfoDialog(actionDesc, info);
		}
	}

	private void collectVolumeNames() {
		selectedVolumeNames.clear();
		onlineVolumeNames.clear();
		for (Volume volume : volumes) {
			selectedVolumeNames.add(volume.getName());
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
				onlineVolumeNames.add(volume.getName());
			}
		}
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		Set<Volume> selectedVolumes = GUIHelper.getInstance().getSelectedEntities(getWindow(), Volume.class);
		volumes.clear();
		if (selectedVolumes == null || selectedVolumes.isEmpty()) {
			super.selectionChanged(action, selection);
			if (selectedEntity instanceof Volume) {
				volumes.add((Volume) selectedEntity);
			}
		} else {
			volumes.addAll(selectedVolumes); //TODO reverse the collection to maintain the selected order
		}

		action.setEnabled( (volumes.size() > 0) );
	}
}
