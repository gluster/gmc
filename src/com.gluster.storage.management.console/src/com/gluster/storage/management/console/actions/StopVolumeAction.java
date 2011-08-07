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

public class StopVolumeAction extends AbstractActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private List<Volume> volumes = new ArrayList<Volume>();
	private List<String> selectedVolumeNames = new ArrayList<String>();
	private List<String> onlineVolumeNames = new ArrayList<String>();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();
		VolumesClient vc = new VolumesClient();

		collectVolumeNames();

		if (onlineVolumeNames.size() == 0) {
			String errorMessage;
			if (selectedVolumeNames.size() == 1) {
				errorMessage = "Volume [" + StringUtil.collectionToString(selectedVolumeNames, ", ")
						+ "] is already offline!";
			} else {
				errorMessage = "Volumes [" + StringUtil.collectionToString(selectedVolumeNames, ", ")
						+ "] are already offline!";
			}
			showWarningDialog(actionDesc, errorMessage);
			return; // Volume already offline. Don't do anything.
		}

		Integer userAction = new MessageDialog(getShell(), "Stop Volume", GUIHelper.getInstance().getImage(
				IImageKeys.VOLUME_16x16), "Are you sure you want to stop the following volume(s)?"
				+ CoreConstants.NEWLINE + "[" + StringUtil.collectionToString(onlineVolumeNames, ", ") + "]",
				MessageDialog.QUESTION, new String[] { "No", "Yes" }, -1).open();

		if (userAction <= 0) { // user select cancel or pressed escape key
			return;
		}

		List<String> stoppedVolumes = new ArrayList<String>();
		List<String> failedVolumes = new ArrayList<String>();
		String errorMessage = "";

		for (Volume volume : volumes) {
			if (volume.getStatus() == VOLUME_STATUS.OFFLINE) {
				continue; // skip if offline volume
			}
			try {
				vc.stopVolume(volume.getName());
				modelManager.updateVolumeStatus(volume, VOLUME_STATUS.OFFLINE);
				stoppedVolumes.add(volume.getName());
			} catch (Exception e) {
				failedVolumes.add(volume.getName());
				// If any post volume stop activity failed, update the volume status
				if (vc.getVolume(volume.getName()).getStatus() == VOLUME_STATUS.OFFLINE) {
					modelManager.updateVolumeStatus(volume, VOLUME_STATUS.OFFLINE);
				}
				errorMessage += e.getMessage();
			}
		}

		// Display the success or failure info
		if (stoppedVolumes.size() == 0) { // No volume(s) stopped successfully
			showErrorDialog(actionDesc, "Following volume(s) [" + StringUtil.collectionToString(failedVolumes, ", ")
					+ "] could not be stopped! " + CoreConstants.NEWLINE + "Error: [" + errorMessage + "]");
		} else {
			String info = "Following volume(s) [" + StringUtil.collectionToString(stoppedVolumes, ", ")
					+ "] are stopped successfully!";
			if (errorMessage != "") {
				info += CoreConstants.NEWLINE + CoreConstants.NEWLINE + "Following volume(s) ["
						+ StringUtil.collectionToString(failedVolumes, ", ") + "] are failed to stop! [" + errorMessage
						+ "]";
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gluster.storage.management.console.actions.AbstractActionDelegate#selectionChanged(org.eclipse.jface.action.IAction
	 * , org.eclipse.jface.viewers.ISelection)
	 */
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

		action.setEnabled(false);
		// To enable the action
		for (Volume volume : volumes) {
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
				action.setEnabled(true);
				break;// If find an online volume, enable the action
			}
		}
	}
}
