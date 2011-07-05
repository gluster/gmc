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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class StopVolumeAction extends AbstractActionDelegate {
	private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(final IAction action) {
		final String actionDesc = action.getDescription();
		if (volume.getStatus() == VOLUME_STATUS.OFFLINE) {
			showWarningDialog(actionDesc, "Volume [" + volume.getName() + "] is already offline!");
			return; // Volume already offline. Don't do anything.
		}

		Integer deleteOption = new MessageDialog(getShell(), "Stop Volume", GUIHelper.getInstance().getImage(
				IImageKeys.VOLUME), "Are you sure you want to stop the volume [" + volume.getName() + "] ?",
				MessageDialog.QUESTION, new String[] { "No", "Yes" }, -1).open();

		if (deleteOption <= 0) {
			return;
		}

		try {
			new VolumesClient().stopVolume(volume.getName());
			showInfoDialog(actionDesc, "Volume [" + volume.getName() + "] stopped successfully!");
			modelManager.updateVolumeStatus(volume, VOLUME_STATUS.OFFLINE);
		} catch (Exception e) {
			showErrorDialog(actionDesc,
					"Volume [" + volume.getName() + "] could not be stopped! Error: [" + e.getMessage() + "]");
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
			action.setEnabled(volume.getStatus() == VOLUME_STATUS.ONLINE);
		}
	}
}
