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
import com.richclientgui.toolbox.duallists.DualListComposite.ListContentChangedListener;
import com.richclientgui.toolbox.duallists.IRemovableContentProvider;

/**
 * @author root
 * 
 */
public class AddDiskPage extends WizardPage {
	private List<Disk> availableDisks = new ArrayList<Disk>();
	private List<Disk> selectedDisks = new ArrayList<Disk>();
	private Volume volume = null;
	private DisksSelectionPage page = null;


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
		
		setPageComplete(false);
		setErrorMessage("Please select disks to be added to the volume.");
	}

	
	private boolean  isDiskUsed(Volume volume, Disk disk){
		for (String volumeDisk : volume.getDisks()) { // expected form of volumeDisk is "server:diskName"
			if ( disk.getQualifiedName().equals(volumeDisk)) {
				return true;
			}
		}
		return false;
	}
	
	protected List<Disk> getAvailableDisks(Volume volume) {
		List<Disk> availableDisks = new ArrayList<Disk>();
		for (Disk disk : GlusterDataModelManager.getInstance().getReadyDisksOfAllServers()) {
			if ( ! isDiskUsed(volume, disk) ) {
				availableDisks.add(disk);
			}
		}
		return availableDisks;
	}


	public List<Disk> getChosenDisks( ) {
		return page.getChosenDisks();
	}
	
	private boolean isValidDiskSelection(int diskCount) {
		if ( diskCount == 0) {
			return false;
		}
		switch (volume.getVolumeType()) {
		case DISTRIBUTED_MIRROR:
			return (diskCount % volume.getReplicaCount() == 0);
		case DISTRIBUTED_STRIPE:
			return (diskCount % volume.getStripeCount() == 0);
		}
		return true;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		getShell().setText("Add Disk");
		List<Disk> chosenDisks = new ArrayList<Disk>(); // or volume.getDisks();
		
		page = new DisksSelectionPage(parent, SWT.NONE, availableDisks, chosenDisks);
		page.addDiskSelectionListener(new ListContentChangedListener<Disk>() {
			@Override
			public void listContentChanged(IRemovableContentProvider<Disk> contentProvider) {
				List<Disk> newChosenDisks = page.getChosenDisks();
				
				// validate chosen disks
				if(isValidDiskSelection(newChosenDisks.size())) {
					clearError();
				} else {
					setError();
				}
			}
		});
		setControl(page);
	}

	private void setError() {
		String errorMessage = null;
		if ( volume.getVolumeType() == VOLUME_TYPE.PLAIN_DISTRIBUTE) {
			errorMessage = "Please select at least one disk!";
		} else if( volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			errorMessage = "Please select disks in multiples of " + volume.getReplicaCount();
		} else {
			errorMessage = "Please select disks in multiples of " + volume.getStripeCount();
		}

		setPageComplete(false);
		setErrorMessage(errorMessage);
	}

	private void clearError() {
		setErrorMessage(null);
		setPageComplete(true);
	}

	public DisksSelectionPage getDialogPage() {
		return this.page;
	}
	
	public void setPageComplete() {
		
	}

}
