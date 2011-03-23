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

import java.util.HashSet;
import java.util.Set;

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
		CreateVolumePage1 page = (CreateVolumePage1) getPage(CreateVolumePage1.PAGE_NAME);
		Volume newVol = page.getVolume();
		GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
		VolumesClient volumesClient = new VolumesClient(modelManager.getSecurityToken());
		Status status = volumesClient.createVolume(newVol);
		
		if (status.isSuccess()) {
			newVol.setStatus(VOLUME_STATUS.OFFLINE);
			
			if (page.getStartVolumeRequest()) {
				Status volumeStartStatus = volumesClient.startVolume(newVol.getName());
				if (volumeStartStatus.isSuccess()) {
					newVol.setStatus(VOLUME_STATUS.ONLINE);
				}
			}
			
			modelManager.addVolume(newVol);
			MessageDialog.openInformation(getShell(), "Create Volume", "Volume created successfully and configuration added!");
		} else {
			MessageDialog.openError(getShell(), "Create Volume", "Volume creation failed! [" + status.getCode() + "]["
					+ status.getMessage() + "]");
		}

		return true;
	}
}
