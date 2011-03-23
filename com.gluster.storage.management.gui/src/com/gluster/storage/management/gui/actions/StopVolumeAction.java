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
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;

public class StopVolumeAction extends AbstractActionDelegate {
	private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	
	@Override
	public void run(IAction action) {
		if(volume.getStatus() == VOLUME_STATUS.OFFLINE) {
			return; // Volume already offline. Don't do anything.
		}
		
		VolumesClient client = new VolumesClient(modelManager.getSecurityToken());
		Status status = client.stopVolume(volume.getName());
		if (status.isSuccess()) {
			new MessageDialog(Display.getDefault().getActiveShell(), action.getDescription(), null, "Volume ["
					+ volume.getName() + "] stopped successfully!", MessageDialog.INFORMATION, new String[] { "OK" }, 0)
					.open();
			modelManager.updateVolumeStatus(volume, VOLUME_STATUS.OFFLINE);
		} else {
			new MessageDialog(Display.getDefault().getActiveShell(), action.getDescription(), null, "Volume ["
					+ volume.getName() + "] could not be stopped! Error: [" + status + "]", MessageDialog.ERROR,
					new String[] { "OK" }, 0).open();
		}
	}

	@Override
	public void dispose() {
	}
	
	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.actions.AbstractActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
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
