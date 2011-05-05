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
package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class DeleteVolumeAction extends AbstractActionDelegate {
	private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(IAction action) {
		final String actionDesc = action.getDescription();

		String warningMessage;
		if (volume.getStatus() == VOLUME_STATUS.OFFLINE) {
			warningMessage = "Are you sure to delete the Volume[" + volume.getName() + "] ?";
		} else {
			warningMessage = "Volume [" + volume.getName() + "] is online, \nAre you sure to continue?";
		}

		Integer deleteOption = new MessageDialog(getShell(), "Delete Volume", GUIHelper.getInstance().getImage(
				IImageKeys.VOLUME), warningMessage, MessageDialog.QUESTION, new String[] { "Cancel",
				"Delete volume and it's data", "Delete volume, keep back-up of data" }, 2).open();
		if (deleteOption <= 0) { // By Cancel button(0) or Escape key(-1)
			return;
		}

		VolumesClient client = new VolumesClient(modelManager.getSecurityToken());

		Status status;
		if (volume.getStatus() == VOLUME_STATUS.ONLINE) { // To stop the volume service, if running
			status = client.stopVolume(volume.getName());
			if (!status.isSuccess()) {
				showErrorDialog(actionDesc, "Volume [" + volume.getName() + "] could not be stopped! Error: [" + status
						+ "]");
				return;
			}
		}
		String confirmDelete = "";
		if (deleteOption == 1) {
			confirmDelete = "-d";
		}

		status = client.deleteVolume(volume, confirmDelete);
		if (status.isSuccess()) {
			showInfoDialog(actionDesc, "Volume [" + volume.getName() + "] deleted successfully!");
			modelManager.deleteVolume(volume);
		} else {
			if (status.isPartSuccess()) {
				showWarningDialog(actionDesc, "Volume deleted, but following error(s) occured: " + status);
				modelManager.deleteVolume(volume);
			} else {
				showErrorDialog(actionDesc,
						"Volume [" + volume.getName() + "] could not be deleted! Error: [" + status + "]");
			}
		}
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (selectedEntity instanceof Volume) {
			volume = (Volume) selectedEntity;
		}
	}
}
