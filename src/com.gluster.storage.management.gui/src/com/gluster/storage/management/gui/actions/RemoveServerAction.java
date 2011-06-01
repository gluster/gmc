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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.utils.StringUtil;

public class RemoveServerAction extends AbstractActionDelegate {

	private GlusterServer server;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	protected void performAction(IAction action) {
		final String actionDesc = action.getDescription();

		List<String> configuredVolumes = getServerVolumeNames(server.getName());

		if (configuredVolumes.size() > 0) {
			String volumes = StringUtil.ListToString(configuredVolumes, ", ");
			showErrorDialog(actionDesc, "Server cannot be removed as it is being used by following volumes: ["
					+ volumes + "]");
			return;
		}

		boolean confirmed = showConfirmDialog(actionDesc,
				"Are you sure you want to remove this server [" + server.getName() + "] ?");
		if (!confirmed) {
			return;
		}

		GlusterServersClient client = new GlusterServersClient();
		Status status = client.removeServer(server.getName());

		if (status.isSuccess()) {
			showInfoDialog(actionDesc, "Server removed successfully");
			GlusterServer glusterServer = (GlusterServer) server;
			GlusterDataModelManager.getInstance().removeGlusterServer(glusterServer);
		} else {
			showErrorDialog(actionDesc, "Server could not be removed. Error: [" + status + "]");
		}
	}

	private List<String> getServerVolumeNames(String serverName) {
		Cluster cluster = GlusterDataModelManager.getInstance().getModel().getCluster();
		List<String> volumeNames = new ArrayList<String>();
		for (Volume volume : cluster.getVolumes()) {
			for (String brick : volume.getDisks()) {
				if (serverName.equals(brick.split(":")[0])) {
					volumeNames.add(volume.getName());
					break;
				}
			}
		}
		return volumeNames;
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		if (selectedEntity instanceof GlusterServer) {
			server = (GlusterServer) selectedEntity;
		}
	}
}
