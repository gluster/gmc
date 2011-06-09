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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class AddServerAction extends AbstractActionDelegate {
	@Override
	protected void performAction(final IAction action) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
				GlusterServersClient glusterServersClient = new GlusterServersClient();

				Set<Server> selectedServers = GUIHelper.getInstance().getSelectedEntities(getWindow(), Server.class);
				Set<Server> successServers = new HashSet<Server>();
				Set<Server> partSuccessServers = new HashSet<Server>();
				String errMsg = "";
				String partErrMsg = "";
				for (Server server : selectedServers) {
					GlusterServerResponse response = glusterServersClient.addServer(server);
					Status status = response.getStatus();
					if (status.isSuccess()) {
						modelManager.removeDiscoveredServer(server);
						modelManager.addGlusterServer(response.getGlusterServer());
						successServers.add(server);
					} else if (status.isPartSuccess()) {
						modelManager.removeDiscoveredServer(server);
						modelManager.addGlusterServer(response.getGlusterServer());
						partSuccessServers.add(server);
						partErrMsg += "[" + server.getName() + "] : " + status;
					} else {
						errMsg += "[" + server.getName() + "] : " + status;
					}
				}

				showStatusMessage(action.getDescription(), selectedServers, successServers, errMsg, partErrMsg);
			}
		});
	}

	private void showStatusMessage(String dialogTitle, Set<Server> selectedServers, Set<Server> successServers,
			String errMsg, String partErrMsg) {
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
		if (successServers.size() == 0) {
			finalMsg = "Server Addition Failed! Error(s):" + CoreConstants.NEWLINE + errMsg;
		} else {
			finalMsg = "Following servers added successfully : "
					+ CoreConstants.NEWLINE
					+ successServers
					+ (partErrMsg.isEmpty() ? "" : CoreConstants.NEWLINE
							+ "Following servers were added to cluster, but with some errors: " + CoreConstants.NEWLINE
							+ partErrMsg)
					+ (errMsg.isEmpty() ? "" : CoreConstants.NEWLINE
							+ "Following errors occurred on other selected servers: " + CoreConstants.NEWLINE + errMsg);
		}
		showErrorDialog(dialogTitle, finalMsg);
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		Set<Server> selectedServers = GUIHelper.getInstance().getSelectedEntities(getWindow(), Server.class);
		if (selectedServers == null || selectedServers.isEmpty()) {
			action.setEnabled(false);
		} else {
			action.setEnabled(true);
		}
	}
}
