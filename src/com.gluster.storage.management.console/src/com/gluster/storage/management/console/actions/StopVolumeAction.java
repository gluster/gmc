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
package com.gluster.storage.management.console.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.IImageKeys;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;

public class StopVolumeAction extends AbstractMonitoredActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private List<Volume> selectedVolumes = new ArrayList<Volume>();
	private List<String> selectedVolumeNames = new ArrayList<String>();
	private List<String> onlineVolumeNames = new ArrayList<String>();
	private List<String> stoppedVolumes  = new ArrayList<String>();
	private List<Volume> failedVolumes = new ArrayList<Volume>();
	private String errorMessage = null;
	
	@Override
	protected void performAction(final IAction action, IProgressMonitor monitor) {
		collectVolumeNames();

		if (onlineVolumeNames.size() == 0) {
			showWarningDialog(action.getDescription(), "Volumes " + selectedVolumeNames + " already stopped!");
			return; // Volumes already stopped, Don't do anything.
		}

		Integer userAction = new MessageDialog(getShell(), "Stop Volume", GUIHelper.getInstance().getImage(
				IImageKeys.VOLUME_16x16), "Are you sure you want to stop the following volumes?"
				+ CoreConstants.NEWLINE + onlineVolumeNames, MessageDialog.QUESTION,
				new String[] { "No", "Yes" }, -1).open();

		if (userAction <= 0) { // user select cancel or pressed escape key
			return;
		}

		List<String> cifsVolumes = GlusterDataModelManager.getInstance().getCifsEnabledVolumeNames(selectedVolumes);
		List<String> offlineServers = GlusterDataModelManager.getInstance().getOfflineServers();
		// One or more servers are offline, Show warning if cifs is enabled
		if (cifsVolumes != null && cifsVolumes.size() > 0 && offlineServers != null && offlineServers.size() > 0) {
			userAction = new MessageDialog(getShell(), "CIFS configuration", GUIHelper.getInstance().getImage(
					IImageKeys.VOLUME_16x16),
					"Performing CIFS updates when one or more servers are offline can trigger "
							+ "inconsistent behavior for CIFS accesses in the cluster." + CoreConstants.NEWLINE
							+ "Are you sure you want to continue?", MessageDialog.QUESTION,
					new String[] { "No", "Yes" }, -1).open();
			if (userAction != 1) {
				return; // Do not stop volume services
			}
		}
		
		stopVolumes(selectedVolumes, false, monitor);
		
		// Check for errors, trying to force stop in case of errors
		checkForErrors(action, monitor, true);
	}

	private void checkForErrors(final IAction action, IProgressMonitor monitor, boolean tryForceStop) {
		String message = null;
		if (stoppedVolumes.size() == 0) { // No volume(s) stopped successfully
			message = "Volume(s) " + failedVolumes + " could not be stopped! " + CoreConstants.NEWLINE
					+ "Error: [" + errorMessage + "]";
			if(tryForceStop) {
				forceStopVolumes(action.getDescription(), message + CoreConstants.NEWLINE
						+ "Do you want to stop forcefully?", monitor);
				// check for errors without trying to force stop in case of errors
				checkForErrors(action, monitor, false);
				return;
			} else {
				showErrorDialog(action.getDescription(), message);
				return;
			}
		} else {
			message = "Volume(s) " + stoppedVolumes + " stopped successfully!";
			if (!errorMessage.isEmpty()) {
				if (failedVolumes.size() > 0) { 
					message = message + CoreConstants.NEWLINE + CoreConstants.NEWLINE + "Volume(s) "
							+ failedVolumes + " could not be stopped! [" + errorMessage + "]";
					if(tryForceStop) {
						forceStopVolumes(action.getDescription(), message + CoreConstants.NEWLINE
								+ "Do you want to stop forcefully?", monitor);
						// check for errors without trying to force stop in case of errors
						checkForErrors(action, monitor, false);
						return;
					}
				} else { // Stop volume success, but post stop volume fails, append the error message
					message += CoreConstants.NEWLINE + CoreConstants.NEWLINE + errorMessage;
				}
			}
			
			if (errorMessage.isEmpty()) {
				showInfoDialog(action.getDescription(), message);
			} else {
				showWarningDialog(action.getDescription(), message);
			}
		}
	}
	
	private void forceStopVolumes(String actionDesc,  String message, IProgressMonitor monitor) {
		boolean forceStop = showConfirmDialog(actionDesc, message);
		if (!forceStop) {
			return;
		}
		stopVolumes(failedVolumes, true, monitor);
	}

	private void stopVolumes(List<Volume> volumes, Boolean force, IProgressMonitor monitor) {
		VolumesClient vc = new VolumesClient();
		Volume newVolume = new Volume();
		stoppedVolumes.clear();
		failedVolumes.clear();
		errorMessage = "";

		monitor.beginTask("Stopping Selected Volumes...", volumes.size());
		// Stopping of a volume results in changes to the model, and ultimately updates the "selectedVolumes" list,
		// over which we are iterating, thus resulting in ConcurrentModificationException. To avoid this, we iterate
		// over an array obtained from the list.
		for (Volume volume : volumes.toArray(new Volume[0])) {
			if(monitor.isCanceled()) {
				break;
			}
			
			if (volume.getStatus() == VOLUME_STATUS.OFFLINE) {
				monitor.worked(1);
				continue; // skip if already stopped
			}
			try {
				monitor.setTaskName("Stopping volume [" + volume.getName() + "]");
				vc.stopVolume(volume.getName(), force);
				stoppedVolumes.add(volume.getName());
				modelManager.updateVolumeStatus(volume, VOLUME_STATUS.OFFLINE);
			} catch (Exception e) {
				// If any post volume stop activity failed, update the volume status
				if (vc.getVolume(volume.getName()).getStatus() == VOLUME_STATUS.OFFLINE) {
					// stop volume succeed, so add it to stoppedVolumes
					stoppedVolumes.add(volume.getName());
					modelManager.updateVolumeStatus(volume, VOLUME_STATUS.OFFLINE);
					errorMessage += "Volume [" + volume.getName() + "] stopped, but following error occured: ["
							+ e.getMessage() + "]";
				} else {
					failedVolumes.add(volume);
					errorMessage += "[" + volume.getName() + "] : " + e.getMessage() + CoreConstants.NEWLINE;
				}
			}

			// Update the model by fetching latest volume info (NOT JUST STATUS)
			try {
				modelManager.refreshVolumeData(volume);
			} catch (Exception e) {
				errorMessage += "Failed to update volume info on UI. [" + e.getMessage() + "]";
			}
			monitor.worked(1);
		}
		monitor.done();
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gluster.storage.management.console.actions.AbstractActionDelegate#selectionChanged(org.eclipse.jface.action.IAction
	 * , org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		Set<Volume> selectedVolumeNames = GUIHelper.getInstance().getSelectedEntities(getWindow(), Volume.class);
		selectedVolumes.clear();
		if (selectedVolumeNames == null || selectedVolumeNames.isEmpty()) {
			super.selectionChanged(action, selection);
			if (selectedEntity instanceof Volume) {
				selectedVolumes.add((Volume) selectedEntity);
			}
		} else {
			selectedVolumes.addAll(selectedVolumeNames); //TODO reverse the collection to maintain the selected order
		}

		action.setEnabled(false);
		// To enable the action
		for (Volume volume : selectedVolumes) {
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
				action.setEnabled(true);
				break; // If find an online volume, enable the action
			}
		}
	}
}
