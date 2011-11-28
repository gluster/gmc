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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.gluster.storage.management.client.GlusterServersClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.dialogs.ServerAdditionDialog;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.model.Server;


public class AddServerAction extends AbstractMonitoredActionDelegate {
	
	@Override
	protected void performAction(final IAction action, IProgressMonitor monitor) {
		GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
		GlusterServersClient glusterServersClient = new GlusterServersClient();

		Set<Server> selectedServers = GUIHelper.getInstance().getSelectedEntities(getWindow(), Server.class);
		Set<Server> successServers = new HashSet<Server>();
		Set<Server> partSuccessServers = new HashSet<Server>();
		String errMsg = "";
		String partErrMsg = "";

		if (selectedServers.isEmpty()) {
			monitor.beginTask("Starting Manual Server Addition", 1);
			addServerManually();
			monitor.worked(1);
			monitor.done();
			return;
		}
		
		monitor.beginTask("Adding Selected Servers...", selectedServers.size());
		for (Server server : selectedServers) {
			if(monitor.isCanceled()) {
				break;
			}
			
			monitor.setTaskName("Adding server [" + server.getName() + "]...");

			try {
				URI newServerURI = glusterServersClient.addServer(server.getName());
				modelManager.addGlusterServer(glusterServersClient.getGlusterServer(newServerURI));
				successServers.add(server);
			} catch (Exception e) {
				if (!errMsg.isEmpty()) {
					errMsg += CoreConstants.NEWLINE;
				}
				errMsg += "Server " + server.getName() + ". Error: [" + e.getMessage() + "]";
			}
			monitor.worked(1);
		}
		monitor.done();

		showStatusMessage(action.getDescription(), selectedServers, successServers, partSuccessServers, errMsg,
				partErrMsg);
	}

	private void addServerManually() {
		try {
			// To open a dialog for server addition
			ServerAdditionDialog dialog = new ServerAdditionDialog(getShell());
			dialog.open();
		} catch (Exception e) {
			logger.error("Error in Manual server addition", e);
			showErrorDialog("Add server", "Add server failed! [" + e.getMessage() + "]");
		}
	}

	private void showStatusMessage(String dialogTitle, Set<Server> selectedServers, Set<Server> successServers,
			Set<Server> partSuccessServers, String errMsg, String partErrMsg) {
		if (successServers.size() == selectedServers.size()) {
			if (selectedServers.size() == 1) {
				showInfoDialog(dialogTitle, "Server [" + selectedServers.iterator().next() + "] added successfully!");
			} else {
				showInfoDialog(dialogTitle, "Following servers added successfully!" + CoreConstants.NEWLINE
						+ selectedServers);
			}
			return;
		}

		String finalMsg = "";
		if (successServers.size() == 0 && partSuccessServers.size() == 0) {
			finalMsg = "Server Addition Failed! Error(s):" + CoreConstants.NEWLINE + errMsg;
		} else {
			finalMsg = (successServers.isEmpty() ? "" : "Following servers added successfully : "
					+ CoreConstants.NEWLINE + successServers + CoreConstants.NEWLINE)
					+ (partSuccessServers.isEmpty() ? "" : "Following servers were added to cluster, but with some errors: "
							+ CoreConstants.NEWLINE + partErrMsg + CoreConstants.NEWLINE)
					+ (errMsg.isEmpty() ? "" : CoreConstants.NEWLINE
							+ "Following errors occurred on other selected servers: " + CoreConstants.NEWLINE + errMsg);
		}
		showErrorDialog(dialogTitle, finalMsg);
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}
}
