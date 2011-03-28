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
import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServerResponse;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Volume;

public class AddServerAction extends AbstractActionDelegate {
	@Override
	public void run(IAction action) {
		GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
		GlusterServersClient glusterServersClient = new GlusterServersClient(modelManager.getSecurityToken());
		Server server = (Server) selectedEntity;
		GlusterServerResponse response = glusterServersClient.addServer(server);
		if (response.getStatus().isSuccess()) {
			modelManager.removeDiscoveredServer(server);
			modelManager.addGlusterServer(response.getGlusterServer());
			new MessageDialog(Display.getDefault().getActiveShell(), "Add Server", null, "Server [" + server.getName()
					+ "] added successfully!", MessageDialog.INFORMATION, new String[] { "OK" }, 0).open();
		} else {
			new MessageDialog(Display.getDefault().getActiveShell(), "Add Server", null, "Server [" + server.getName()
					+ " could not be added to cluster! Error: [" + response.getStatus().getMessage() + "]",
					MessageDialog.ERROR, new String[] { "OK" }, 0).open();
		}
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		if (selectedEntity != null && selectedEntity instanceof Entity) {
			action.setEnabled(true);
			if (selectedEntity instanceof EntityGroup && ((EntityGroup) selectedEntity).getEntityType() == Volume.class) {
				action.setEnabled(false);
			}
		}
	}
}
