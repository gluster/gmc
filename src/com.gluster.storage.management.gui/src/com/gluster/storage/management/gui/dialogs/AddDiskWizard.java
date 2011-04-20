/**
 * AddDiskWizard.java
 *
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
 */
package com.gluster.storage.management.gui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.StringUtil;

/**
 *
 */
public class AddDiskWizard extends Wizard {
	private AddDiskPage page;
	private Volume volume;

	public AddDiskWizard() {
		setWindowTitle("Gluster Management Console - Add disk");
		setHelpAvailable(false); // TODO: Introduce wizard help
	}

	public void addPages(Volume volume) {
		this.volume = volume;
		page = new AddDiskPage(volume);
		addPage(page);
	}

	private String getDiskNames(List<Disk> disks) {
		List<String> diskNames = new ArrayList<String>();
		for (Disk disk : disks) {
			diskNames.add(disk.getServerName() + ":" + disk.getName());
		}
		return StringUtil.ListToString(diskNames, ",");
	}

	private boolean isValidDiskSelection(int diskCount) {
		switch (volume.getVolumeType()) {
		case PLAIN_DISTRIBUTE:
			return (diskCount > 0);
		case DISTRIBUTED_MIRROR:
			return (diskCount % volume.getReplicaCount() == 0 && diskCount > 0);
		case DISTRIBUTED_STRIPE:
			return (diskCount % volume.getStripeCount() == 0 && diskCount > 0);
		}
		return false;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		List<Disk> disks = page.getDialogPage().getChosenDisks();
		String selectedDisks = getDiskNames(disks);
		List<String> diskList = Arrays.asList(selectedDisks.split(":"));
		if (isValidDiskSelection(disks.size())) {
			VolumesClient volumeClient = new VolumesClient(GlusterDataModelManager.getInstance().getSecurityToken());
			try {
				Status status = volumeClient.addDisks(volume.getName(), getDiskNames(disks));
				if (!status.isSuccess()) {
					MessageDialog.openError(getShell(), "Add disk failure", status.getMessage());
					return status.isSuccess();
				} else {
					volume.addDisks(diskList);
					MessageDialog.openInformation(getShell(), "Add Disk", status.getMessage());
					return status.isSuccess();
				}
			} catch (Exception e) {
				MessageDialog.openError(getShell(), "Add disk failure", e.getMessage());
				return false;
			}
		} else {
			String errorMessage = "";
			if ( volume.getVolumeType() == VOLUME_TYPE.PLAIN_DISTRIBUTE) {
				errorMessage = "Atleast one disk required to add";
			} else if( volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
				errorMessage = "Multiples of " + volume.getReplicaCount() + " disks are required to add";
			} else {
				errorMessage = "Multiples of " + volume.getStripeCount() + " disks are required to add";
			}
			MessageDialog.openError(getShell(), "Add disk failure", errorMessage);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		// TODO: Add logic to return true only when all validations are successful
		return super.canFinish();
	}
}
