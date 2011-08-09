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

public class DeleteVolumeAction extends AbstractActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private List<Volume> selectedVolumes = new ArrayList<Volume>();
	private List<String> selectedVolumeNames = new ArrayList<String>();
	private List<String> onlineVolumeNames = new ArrayList<String>();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();
		VolumesClient vc = new VolumesClient();

		collectVolumeNames();
		String warningMessage;
		if (onlineVolumeNames.size() > 0) { // Getting confirmation for stop and delete
			warningMessage = "Following volume(s) " + onlineVolumeNames + " are online, " + CoreConstants.NEWLINE
					+ "Are you sure to continue?" + CoreConstants.NEWLINE + selectedVolumeNames;
		} else {
			warningMessage = "Are you sure to delete the volumes " + selectedVolumeNames + " ?";
		}

		Integer deleteOption = new MessageDialog(getShell(), "Delete Volume", GUIHelper.getInstance().getImage(
				IImageKeys.VOLUME_16x16), warningMessage, MessageDialog.QUESTION, new String[] { "Cancel",
				"Delete volume and data", "Delete volume, keep data" }, -1).open();
		if (deleteOption <= 0) { // By Cancel button(0) or Escape key(-1)
			return;
		}

		boolean confirmDelete = (deleteOption == 1) ? true : false;
		List<String> deletedVolumeNames = new ArrayList<String>();
		List<String> failedVolumes = new ArrayList<String>();
		String errorMessage = "";

		for (Volume volume : selectedVolumes.toArray(new Volume[0])) {
			try {
				if (volume.getStatus() == VOLUME_STATUS.ONLINE) { // stop if online volume
					vc.stopVolume(volume.getName());
				}
				vc.deleteVolume(volume.getName(), confirmDelete);
				modelManager.deleteVolume(volume);
				deletedVolumeNames.add(volume.getName());
			} catch (Exception e) {
				// there is a possibility that the error was in post-delete operation, which means
				// volume was deleted, but some other error happened. check if this is the case.
				if (vc.volumeExists(volume.getName())) {
					errorMessage += CoreConstants.NEWLINE + "[" + volume.getName() + "] : [" + e.getMessage() + "]";
					failedVolumes.add(volume.getName());
				} else {
					errorMessage += CoreConstants.NEWLINE + "Volume deleted, but following error occured: ["
							+ e.getMessage() + "]";
					modelManager.deleteVolume(volume);
					deletedVolumeNames.add(volume.getName());
				}
			}
		}

		// Display the success or failure info
		if (deletedVolumeNames.size() == 0) { // No volume(s) deleted successfully
			showErrorDialog(actionDesc, "volumes " + failedVolumes + " could not be delete! " + CoreConstants.NEWLINE
					+ "Error: [" + errorMessage + "]");
		} else {
			String info = "Volumes " + deletedVolumeNames + " deleted successfully!";
			if (errorMessage != "") {
				info += CoreConstants.NEWLINE + CoreConstants.NEWLINE + "Volumes "
						+ failedVolumes + " could not be deleted! ["
						+ errorMessage + "]";
			}
			showInfoDialog(actionDesc, info);
		}
	}

	private void collectVolumeNames() {
		selectedVolumeNames.clear();
		onlineVolumeNames.clear();
		for (Volume volume : selectedVolumes) {
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
		Set<Volume> selectedVolumeSet = GUIHelper.getInstance().getSelectedEntities(getWindow(), Volume.class);
		selectedVolumes.clear();
		if (selectedVolumeSet == null || selectedVolumeSet.isEmpty()) {
			super.selectionChanged(action, selection);
			if (selectedEntity instanceof Volume) {
				selectedVolumes.add((Volume) selectedEntity);
			}
		} else {
			selectedVolumes.addAll(selectedVolumeSet); //TODO reverse the collection to maintain the selected order
		}

		action.setEnabled( (selectedVolumes.size() > 0) );
	}
}
