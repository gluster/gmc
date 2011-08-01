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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Device;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.richclientgui.toolbox.duallists.DualListComposite.ListContentChangedListener;
import com.richclientgui.toolbox.duallists.IRemovableContentProvider;

/**
 * @author root
 * 
 */
public class AddBrickPage extends WizardPage {
	private List<Device> availableDevices = new ArrayList<Device>();
	private List<Device> selectedDevices = new ArrayList<Device>();
	private Volume volume = null;
	private BricksSelectionPage page = null;


	public static final String PAGE_NAME = "add.disk.volume.page";

	/**
	 * @param pageName
	 */
	protected AddBrickPage(Volume volume) {
		super(PAGE_NAME);
		this.volume = volume;
		setTitle("Add Brick");

		String description = "Add bricks to [" + volume.getName() + "] ";
		if ( volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			description += "(in multiples of " + volume.getReplicaCount() + ")";
		} else if (volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			description += "(in multiples of " + volume.getStripeCount() + ")";
		}
		setDescription(description);

		availableDevices = getAvailableDevices(volume);
		
		setPageComplete(false);
		setErrorMessage("Please select bricks to be added to the volume [" + volume.getName()  +"]");
	}

	
	private boolean  isDeviceUsed(Volume volume, Device device){
		for (Brick volumeBrick : volume.getBricks()) {
			if ( device.getQualifiedBrickName(volume.getName()).equals(volumeBrick.getQualifiedName())) {
				return true;
			}
		}
		return false;
	}
	
	protected List<Device> getAvailableDevices(Volume volume) {
		List<Device> availableDevices = new ArrayList<Device>();
		for (Device device : GlusterDataModelManager.getInstance().getReadyDevicesOfAllServers()) {
			if ( ! isDeviceUsed(volume, device) ) {
				availableDevices.add(device);
			}
		}
		return availableDevices;
	}

	public Set<Device> getChosenDevices() {
		return new HashSet<Device>(page.getChosenDevices());
	}
	
	public Set<Brick> getChosenBricks( String volumeName ) {
		return page.getChosenBricks(volumeName);
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
		getShell().setText("Add Brick");
		List<Device> chosenDevices = new ArrayList<Device>(); // or volume.getDisks();
		
		page = new BricksSelectionPage(parent, SWT.NONE, availableDevices, chosenDevices, volume.getName());
		page.addDiskSelectionListener(new ListContentChangedListener<Device>() {
			@Override
			public void listContentChanged(IRemovableContentProvider<Device> contentProvider) {
				List<Device> newChosenDevices = page.getChosenDevices();
				
				// validate chosen disks
				if(isValidDiskSelection(newChosenDevices.size())) {
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
			errorMessage = "Please select at least one brick!";
		} else if( volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			errorMessage = "Please select bricks in multiples of " + volume.getReplicaCount();
		} else {
			errorMessage = "Please select bricks in multiples of " + volume.getStripeCount();
		}

		setPageComplete(false);
		setErrorMessage(errorMessage);
	}

	private void clearError() {
		setErrorMessage(null);
		setPageComplete(true);
	}

	public BricksSelectionPage getDialogPage() {
		return this.page;
	}
	
	public void setPageComplete() {
		
	}

}
