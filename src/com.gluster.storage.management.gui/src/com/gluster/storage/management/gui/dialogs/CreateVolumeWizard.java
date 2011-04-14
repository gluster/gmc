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
package com.gluster.storage.management.gui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;

public class CreateVolumeWizard extends Wizard {

	public CreateVolumeWizard() {
		setWindowTitle("Gluster Management Console - Create Volume");
		setHelpAvailable(false); // TODO: Introduce wizard help
	}

	@Override
	public void addPages() {
		addPage(new CreateVolumePage1());
	}
	
	@Override
	public boolean performFinish() {
		String dialogTitle = "Create Volume";
		CreateVolumePage1 page = (CreateVolumePage1) getPage(CreateVolumePage1.PAGE_NAME);

		Volume newVolume = page.getVolume();
		GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
		VolumesClient volumesClient = new VolumesClient(modelManager.getSecurityToken());
		Status status = volumesClient.createVolume(newVolume);

		if (status.isSuccess()) {
			String message = "Volume created successfully!";
			boolean warning = false;
			newVolume.setStatus(VOLUME_STATUS.OFFLINE);
			if (page.startVolumeAfterCreation()) {
				Status volumeStartStatus = volumesClient.startVolume(newVolume.getName());
				if (volumeStartStatus.isSuccess()) {
					newVolume.setStatus(VOLUME_STATUS.ONLINE);
					message = "Volume created and started successfully!";
				} else {
					message = "Volume created successfuly, but couldn't be started. Error: " + volumeStartStatus;
					warning = true;
				}
			}
			
			// update the model
			modelManager.addVolume(newVolume);
			if (warning) {
				MessageDialog.openWarning(getShell(), dialogTitle, message);
			} else {
				MessageDialog.openInformation(getShell(), dialogTitle, message);
			}
		} else {
			if (status.isPartSuccess()) {
				MessageDialog.openWarning(getShell(), dialogTitle, "Volume created, but following error(s) occured: "
						+ status);
			} else {
				MessageDialog.openError(getShell(), dialogTitle, "Volume creation failed! " + status);
			}
		}

		return true;
	}
}
