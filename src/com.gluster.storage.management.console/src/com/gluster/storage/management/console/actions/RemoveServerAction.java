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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.GlusterServer;

public class RemoveServerAction extends AbstractMonitoredActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	protected void performAction(final IAction action, IProgressMonitor monitor) {
		final String actionDesc = action.getDescription();

		Set<GlusterServer> selectedServers = guiHelper.getSelectedEntities(getWindow(), GlusterServer.class);

		if (!validate(action, selectedServers)) {
			return;
		}

		boolean confirmed = showConfirmDialog(actionDesc, "Are you sure you want to remove the server(s) "
				+ selectedServers + " ?");
		if (!confirmed) {
			return;
		}

		Set<GlusterServer> successServers = new HashSet<GlusterServer>();
		String errMsg = "";
		monitor.beginTask("Removing Selected Servers...", selectedServers.size());
		for (GlusterServer server : selectedServers) {
			monitor.setTaskName("Removing server [" + server.getName() + "]...");

			GlusterServersClient client = new GlusterServersClient();
			try {
				client.removeServer(server.getName());
				GlusterServer glusterServer = (GlusterServer) server;
				modelManager.removeGlusterServer(glusterServer);
				successServers.add(server);
			} catch (Exception e) {
				if (!serverExists(server.getName())) {
					modelManager.removeGlusterServer((GlusterServer) server);
				}
				errMsg += "[" + server.getName() + "] : " + e.getMessage() + CoreConstants.NEWLINE;
			}
			monitor.worked(1);
		}
		monitor.done();
		showStatusMessage(action.getDescription(), selectedServers, successServers, errMsg);
	}

	private Boolean serverExists(String serverName) {
		try {
			GlusterServersClient client = new GlusterServersClient();
			GlusterServer server = client.getGlusterServer(serverName);
			if (server != null && server.getName().length() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	private void showStatusMessage(String dialogTitle, Set<GlusterServer> selectedServers, Set<GlusterServer> successServers,
			String errMsg) {
		if (successServers.size() == selectedServers.size()) {
			if(selectedServers.size() == 1) {
				showInfoDialog(dialogTitle, "Server [" + selectedServers.iterator().next() + "] removed successfully!");
			} else {
				showInfoDialog(dialogTitle, "Following servers removed successfully: " + CoreConstants.NEWLINE
						+ selectedServers + CoreConstants.NEWLINE + errMsg);
			}
			return;
		}
		
		if (successServers.size() == 0) {
			errMsg = "Server Removal Failed! Error(s):" + CoreConstants.NEWLINE + errMsg;
		} else {
			errMsg = "Following servers removed successfully : " + CoreConstants.NEWLINE + successServers
					+ CoreConstants.NEWLINE + "Following errors occurred on other selected servers: "
					+ CoreConstants.NEWLINE + errMsg;
		}
		showErrorDialog(dialogTitle, errMsg);
	}

	private boolean validate(IAction action, Set<GlusterServer> selectedServers) {
		Map<GlusterServer, List<String>> usedServers = new HashMap<GlusterServer, List<String>>();
		for (GlusterServer server : selectedServers) {
			List<String> configuredVolumes = modelManager.getVolumesOfServer(server.getName());

			if (configuredVolumes.size() > 0) {
				usedServers.put(server, configuredVolumes);
			}
		}

		if (usedServers.size() > 0) {
			if (usedServers.size() == 1) {
				showErrorDialog(action.getDescription(), "Server [" + usedServers.keySet().iterator().next()
						+ "] cannot be removed as it is being used by volume(s): " + CoreConstants.NEWLINE
						+ usedServers.values().iterator().next() );
			} else {
				String serverList = "";
				for (Entry<GlusterServer, List<String>> entry : usedServers.entrySet()) {
					serverList += entry.getKey() + " -> " + entry.getValue() + CoreConstants.NEWLINE;
				}
				showErrorDialog(action.getDescription(),
						"Following servers cannot be removed as they are being used by volume(s): "
								+ CoreConstants.NEWLINE + serverList );
			}
			return false;
		}
		return true;
	}

	public void dispose() {
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		Set<GlusterServer> selectedServers = GUIHelper.getInstance().getSelectedEntities(getWindow(),
				GlusterServer.class);
		if(selectedServers == null || selectedServers.isEmpty()) {
			action.setEnabled(false);
		} else {
			action.setEnabled(true);
		}
	}
}
