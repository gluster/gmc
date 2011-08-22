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
package com.gluster.storage.management.console.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.IImageKeys;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.utils.GlusterLogger;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;

public class CreateVolumeWizard extends Wizard {
	private static final String title = "Gluster Management Console - Create Volume";
	private CreateVolumePage1 page;
	
	public CreateVolumeWizard() {
		setWindowTitle(title);
		setHelpAvailable(false); // TODO: Introduce wizard help
	}

	@Override
	public void addPages() {
		page = new CreateVolumePage1();
		addPage(page);
	}
	
	@Override
	public boolean performFinish() {
		Volume newVolume = page.getVolume();
		VolumesClient volumesClient = new VolumesClient();
		
		try {
			List<String> servers = GlusterDataModelManager.getInstance().getOfflineServers();
			// One or more servers are offline, Show warning if cifs is enabled
			if (newVolume.isCifsEnable() && servers != null && servers.size() > 0) {
				Integer userAction = new MessageDialog(getShell(), "Create Volume", GUIHelper.getInstance().getImage(
						IImageKeys.VOLUME_16x16),
						"Performing CIFS updates when one or more servers are offline can trigger "
								+ "inconsistent behavior for CIFS accesses in the cluster." + CoreConstants.NEWLINE
								+ "Are you sure you want to continue?", MessageDialog.QUESTION, new String[] { "No",
								"Yes" }, -1).open();
				if (userAction != 1) {
					return false; // To stay on the create dialog
				}
			}
			
			volumesClient.createVolume(newVolume);

			// Set proper volume type before assign to model
			VOLUME_TYPE volumetype = newVolume.getVolumeType();
			if (volumetype == VOLUME_TYPE.REPLICATE && newVolume.getBricks().size() > newVolume.getReplicaCount()) {
				newVolume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_REPLICATE);
			} else if (volumetype == VOLUME_TYPE.STRIPE && newVolume.getBricks().size() > newVolume.getStripeCount()) {
				newVolume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_STRIPE);
			}

			handleSuccess(newVolume, volumesClient);
		} catch (Exception e) {
			String errMsg = e.getMessage();
			// the error could be in to post-volume-create processing. check if this is the case.
			if (volumesClient.volumeExists(newVolume.getName())) {
				handlePartSuccess(newVolume, volumesClient, errMsg);
			} else {
				MessageDialog.openError(getShell(), title, "Volume creation failed! Error: " + errMsg);
				return false;
			}
		}

		return true;
	}

	public void handleSuccess(Volume newVolume, VolumesClient volumesClient) {
		String message = "Volume created successfully!";
		newVolume.setStatus(VOLUME_STATUS.OFFLINE);
		boolean warning = false;
		if (page.startVolumeAfterCreation()) {
			try {
				volumesClient.startVolume(newVolume.getName());
				newVolume.setStatus(VOLUME_STATUS.ONLINE);
				message = "Volume created and started successfully!";
			} catch(Exception e) {
				message = "Volume created successfuly, but couldn't be started. Error: " + e.getMessage();
				warning = true;
			}
		}
		
		// update the model
		GlusterDataModelManager.getInstance().addVolume(newVolume);
		if (warning) {
			MessageDialog.openWarning(getShell(), title, message);
		} else {
			MessageDialog.openInformation(getShell(), title, message);
		}
	}

	public void handlePartSuccess(Volume newVolume, VolumesClient volumesClient, String errMsg) {
		// volume exists. error was in post-volume-create
		newVolume.setStatus(VOLUME_STATUS.OFFLINE);
		boolean error = false;
		String message1 = null;
		if (page.startVolumeAfterCreation()) {
			if (MessageDialog.openConfirm(getShell(), title, "Volume created, but following error(s) occured: "
					+ errMsg + CoreConstants.NEWLINE + CoreConstants.NEWLINE
					+ "Do you still want to start the volume [" + newVolume.getName() + "]?")) {
				try {
					volumesClient.startVolume(newVolume.getName());
					newVolume.setStatus(VOLUME_STATUS.ONLINE);
					message1 = "Volume [" + newVolume.getName() + "] started successfully!"; // Only start operation
				} catch(Exception e1) {
					message1 = "Volume couldn't be started. Error: " + e1.getMessage();
					error = true;
				}
			}
			
			if (error) {
				MessageDialog.openWarning(getShell(), title, message1);
			} else if (message1.trim().length() > 0) {
				MessageDialog.openInformation(getShell(), title, message1);
			}
		} else { // Start volume is not checked
			MessageDialog.openWarning(getShell(), title,
					"Volume created, but following error(s) occured: " + errMsg);
		}
		
		// Fetching actual volume info (because of partial success)
		Volume volume = newVolume; 
		try {
			volume = volumesClient.getVolume(newVolume.getName());
		}catch (Exception e) {
			GlusterLogger.getInstance().error("Fetching volume details failed:" + e.getMessage());
		}
		GlusterDataModelManager.getInstance().addVolume(volume);
	}
}
