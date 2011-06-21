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
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;

public class StartVolumeAction extends AbstractActionDelegate {
	private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(IAction action) {
		if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
			return; // Volume already online. Don't do anything.
		}

		VolumesClient client = new VolumesClient();
		final String actionDesc = action.getDescription();
		try {
			client.startVolume(volume.getName());
			showInfoDialog(actionDesc, "Volume [" + volume.getName() + "] started successfully!");
			modelManager.updateVolumeStatus(volume, VOLUME_STATUS.ONLINE);
		} catch (Exception e) {
			showErrorDialog(actionDesc,
					"Volume [" + volume.getName() + "] could not be started! Error: [" + e.getMessage() + "]");
		}
	}

	@Override
	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gluster.storage.management.gui.actions.AbstractActionDelegate#selectionChanged(org.eclipse.jface.action.IAction
	 * , org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		if (selectedEntity instanceof Volume) {
			volume = (Volume) selectedEntity;
			action.setEnabled(volume.getStatus() == VOLUME_STATUS.OFFLINE);
		}
	}
}
