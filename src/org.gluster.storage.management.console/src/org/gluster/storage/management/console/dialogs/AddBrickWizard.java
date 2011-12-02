/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.utils.StringUtil;


/**
 *
 */
public class AddBrickWizard extends Wizard {
	private AddBrickPage page;
	private Volume volume;

	public AddBrickWizard(Volume volume) {
		setWindowTitle("Gluster Management Console - Add Brick");
		setHelpAvailable(false); // TODO: Introduce wizard help
		this.volume = volume;
	}

	public void addPages() {
		page = new AddBrickPage(volume);
		addPage(page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		Set<Brick> bricks = page.getChosenBricks(volume.getName());
		VolumesClient volumeClient = new VolumesClient();
		try {
			Set<String> brickList = getBrickList(bricks);
			
			volumeClient.addBricks(volume.getName(), brickList);

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

	private Set<String> getBrickList(Set<Brick> bricks) {
		Set<String> brickList = new HashSet<String>();
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
