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
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.StringUtil;

/**
 *
 */
public class AddDiskWizard extends Wizard {
	private AddDiskPage page;
	private Volume volume;

	public AddDiskWizard(Volume volume) {
		setWindowTitle("Gluster Management Console - Add Brick");
		setHelpAvailable(false); // TODO: Introduce wizard help
		this.volume = volume;
	}

	public void addPages() {
		page = new AddDiskPage(volume);
		addPage(page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		List<Brick> bricks = page.getChosenBricks(volume.getName());
		VolumesClient volumeClient = new VolumesClient();
		try {
			List<String> brickList = getBrickList(bricks);
			
			volumeClient.addBricks(volume.getName(), brickList);
			List<Disk> disks = page.getChosenDisks();
			volume.addDisks(GlusterCoreUtil.getQualifiedDiskNames(disks));
			volume.addBricks(bricks);

			// Update model with new bricks in the volume
			GlusterDataModelManager.getInstance().addBricks(volume, bricks);

			MessageDialog.openInformation(getShell(), "Add brick(s) to Volume", "Volume [" + volume.getName()
					+ "] is expanded with bricks [" + StringUtil.collectionToString(brickList, ", ") + "]");
			return true;
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Add brick(s) to Volume", e.getMessage());
			return false;
		}
	}

	private List<String> getBrickList(List<Brick> bricks) {
		List<String> brickList = new ArrayList<String>();
		for(Brick brick : bricks) {
			brickList.add(brick.getServerName() + ":" + brick.getBrickDirectory());
		}
		return brickList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		return super.canFinish() && page.isPageComplete();
	}
}
