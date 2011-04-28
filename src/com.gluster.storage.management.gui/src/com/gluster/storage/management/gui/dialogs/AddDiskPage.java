/**
 * AddDiskPage.java
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
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;

/**
 * @author root
 * 
 */
public class AddDiskPage extends WizardPage {
	private List<Disk> availableDisks = new ArrayList<Disk>();
	private List<Disk> selectedDisks = new ArrayList<Disk>();
	private Volume volume = null;
	private CreateVolumeDisksPage page = null;

	public static final String PAGE_NAME = "add.disk.volume.page";

	/**
	 * @param pageName
	 */
	protected AddDiskPage(Volume volume) {
		super(PAGE_NAME);
		this.volume = volume;
		setTitle("Add Disk");

		String description = "Add disks to the Volume by choosing disks from the cluster servers.\n";
		if ( volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			description += "(Disk selection should be multiples of " + volume.getReplicaCount() + ")";
		} else if (volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			description += "(Disk selection should be multiples of " + volume.getStripeCount() + ")";
		}
		setDescription(description);
		
		availableDisks = getAvailableDisks(volume);
	}

	protected List<Disk> getAvailableDisks(Volume volume) {
		List<Disk> availableDisks = new ArrayList<Disk>();
		boolean isDiskAlreadyUsedInTheVolume = false;
		for (Disk disk : GlusterDataModelManager.getInstance().getReadyDisksOfAllServers()) {
			isDiskAlreadyUsedInTheVolume = false;
			for (String volumeDisk : volume.getDisks()) { // volumeDisk of the form "server:diskName"
				if (disk.getServerName().equals(volumeDisk.split(":")[0])
						&& disk.getName().equals(volumeDisk.split(":")[1])) {
					isDiskAlreadyUsedInTheVolume = true;
					break;
				}
			}
			if (!isDiskAlreadyUsedInTheVolume) {
				availableDisks.add(disk);
			}
		}
		return availableDisks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		getShell().setText("Add Disk");
		List<Disk> configuredDisks = new ArrayList<Disk>(); // or volume.getDisks();
		page = new CreateVolumeDisksPage(parent, SWT.NONE, availableDisks, configuredDisks);
		setControl(page);
	}

	public CreateVolumeDisksPage getDialogPage() {
		return this.page;
	}
}
