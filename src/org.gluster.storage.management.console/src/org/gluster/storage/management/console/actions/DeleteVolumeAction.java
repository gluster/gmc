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
package org.gluster.storage.management.console.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.IImageKeys;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.Volume.VOLUME_STATUS;


public class DeleteVolumeAction extends AbstractMonitoredActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private List<Volume> selectedVolumes = new ArrayList<Volume>();
	private List<String> selectedVolumeNames = new ArrayList<String>();
	private List<String> onlineVolumeNames = new ArrayList<String>();
	private List<String> deletedVolumeNames = new ArrayList<String>();
	private List<Volume> failedVolumes = new ArrayList<Volume>(); 
	
	@Override
	protected void performAction(final IAction action, IProgressMonitor monitor) {
		final String actionDesc = action.getDescription();

		collectVolumeNames();
		String warningMessage;
		List<String> cifsVolumes = GlusterDataModelManager.getInstance().getCifsEnabledVolumeNames(selectedVolumes);
		List<String> offlineServers = GlusterDataModelManager.getInstance().getOfflineServers();
		// One or more servers are offline, Show warning if cifs is enabled
		if (cifsVolumes != null && cifsVolumes.size() > 0 && offlineServers != null && offlineServers.size() > 0) {
			Integer userAction = new MessageDialog(getShell(), "CIFS configuration", GUIHelper.getInstance().getImage(
					IImageKeys.VOLUME_16x16),
					"Performing CIFS updates when one or more servers are offline can trigger "
							+ "inconsistent behavior for CIFS accesses in the cluster." + CoreConstants.NEWLINE
							+ "Are you sure you want to continue?", MessageDialog.QUESTION,
					new String[] { "No", "Yes" }, -1).open();
			if (userAction != 1) {
				return; // Do not delete volume services
			}
		}
		
		if (onlineVolumeNames.size() > 0) { // Getting confirmation for stop and delete
			warningMessage = "Following volume(s) " + onlineVolumeNames + " are online, " + CoreConstants.NEWLINE
					+ "Are you sure to continue?" + CoreConstants.NEWLINE + selectedVolumeNames;
		} else {
			warningMessage = "Are you sure to delete the volumes " + selectedVolumeNames + " ?";
		}

		final Integer deleteOption = new MessageDialog(getShell(), "Delete Volume", GUIHelper.getInstance()
				.getImage(IImageKeys.VOLUME_16x16), warningMessage, MessageDialog.QUESTION, new String[] { "Cancel",
				"Delete volume and data", "Delete volume, keep data" }, -1).open();
		if (deleteOption <= 0) { // By Cancel button(0) or Escape key(-1)
			return;
		}
		
		String errorMessage = deleteVolumes(selectedVolumes, deleteOption, monitor);

		// Display the success or failure info
		if (deletedVolumeNames.size() == 0) { // No volume(s) deleted successfully
			showErrorDialog(actionDesc, "Volume(s) could not be deleted! " + CoreConstants.NEWLINE + errorMessage);
		} else {
			String info = "Volume(s) " + deletedVolumeNames + " deleted successfully!";
			if (!failedVolumes.isEmpty()) {
				info += CoreConstants.NEWLINE + CoreConstants.NEWLINE + "Volumes " + failedVolumes
						+ " could not be deleted!" + CoreConstants.NEWLINE + errorMessage;
			}
			
			if (selectedVolumes.size() == deletedVolumeNames.size()) {
				showInfoDialog(actionDesc, info);
			} else {
				showWarningDialog(actionDesc, info);
			}
		}
	}
	
	private String deleteVolumes(List<Volume> volumes, final Integer deleteOption, IProgressMonitor monitor) {
		deletedVolumeNames.clear();
		failedVolumes.clear();
		VolumesClient vc = new VolumesClient();
		boolean confirmDeleteDir = (deleteOption == 1) ? true : false;

		String errorMessage = "";

		// To calculate the total work we need to sum volumes size + online volumes (because we treat stop and delete as
		// separate steps)
		List<Volume> onlineVolumes = getOnlineVolumes(volumes);
		monitor.beginTask("Deleting Selected Volumes...", volumes.size() + onlineVolumes.size());
		
		// Deletion of a volume results in changes to the model, and ultimately updates the "selectedVolumes" list,
		// over which we are iterating, thus resulting in ConcurrentModificationException. To avoid this, we iterate
		// over an array obtained from the list.
		for (Volume volume : volumes.toArray(new Volume[0])) {
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) { // stop if online volume
				monitor.setTaskName("Stopping volume [" + volume.getName() + "]");
				try {
					vc.stopVolume(volume.getName(), false);
				} catch (Exception e1) {
					// try again with force = true
					try {
						vc.stopVolume(volume.getName(), true);
					} catch(Exception e2) {
						// force stop also failed.
						// Mark as deletion failed, append error message.
						errorMessage += CoreConstants.NEWLINE + "Stop [" + volume.getName() + "] : [" + e2.getMessage()
								+ "]";
						failedVolumes.add(volume);
						// since we are not going to perform delete on this volume, 
						// mark the deletion task as worked
						monitor.worked(1);
						
						// continue to next volume without trying to delete this one
						continue;
					}
				} finally {
					// worked for "stop" operation
					monitor.worked(1);
				}
			}

			monitor.setTaskName("Deleting volume [" + volume.getName() + "]");
			try {
				vc.deleteVolume(volume.getName(), confirmDeleteDir);
				modelManager.deleteVolume(volume);
				deletedVolumeNames.add(volume.getName());
			} catch (Exception e) {
				// Volume delete succeeded and post delete operation (directory cleanup, CIFS etc) may fail
				if (vc.volumeExists(volume.getName())) {
					errorMessage += CoreConstants.NEWLINE + "Delete [" + volume.getName() + "] : [" + e.getMessage() + "]";
					failedVolumes.add(volume);
				} else {
					errorMessage += CoreConstants.NEWLINE + "Volume [" + volume.getName()
							+ "] deleted, but following error occured: [" + e.getMessage() + "]";
					modelManager.deleteVolume(volume);
					deletedVolumeNames.add(volume.getName());
				}
			} finally {			
				monitor.worked(1);
			}
		}
		monitor.done();
		return errorMessage;
	}

	private List<Volume> getOnlineVolumes(List<Volume> volumes) {
		List<Volume> onlineVolumes = new ArrayList<Volume>();
		for (Volume volume : volumes) {
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
				onlineVolumes.add(volume);
			}
		}
		return onlineVolumes;
	}
	
	private void collectVolumeNames() {
		selectedVolumeNames.clear();
		onlineVolumeNames.clear();
		for (Volume volume : selectedVolumes) {
			selectedVolumeNames.add(volume.getName());
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
				onlineVolumeNames.add(volume.getName());
			}
		}
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		Set<Volume> selectedVolumeSet = GUIHelper.getInstance().getSelectedEntities(getWindow(), Volume.class);
		selectedVolumes.clear();
		if (selectedVolumeSet == null || selectedVolumeSet.isEmpty()) {
			super.selectionChanged(action, selection);
			if (selectedEntity instanceof Volume) {
				selectedVolumes.add((Volume) selectedEntity);
			}
		} else {
			selectedVolumes.addAll(selectedVolumeSet); //TODO reverse the collection to maintain the selected order
		}

		action.setEnabled( (selectedVolumes.size() > 0) );
	}
}
