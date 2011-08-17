/**
 * GlusterViewsManager.java
 *
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
 */
package com.gluster.storage.management.console.views;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.gluster.storage.management.console.ConsoleConstants;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Volume;

/**
 * @see ViewsManager
 */
public class GlusterViewsManager implements ViewsManager {
	private IWorkbenchPage page;

	public GlusterViewsManager(IWorkbenchPage page) {
		this.page = page;
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.console.views.ViewsManager#updateViews(com.gluster.storage.management.core.model.Entity)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void updateViews(Entity entity) {
		closeAllViews();

		try {
			if (entity instanceof EntityGroup) {
				showViewsForEntityGroup((EntityGroup)entity);
			} else if (entity.getClass() == Server.class) {
				showViewsForDiscoveredServer((Server)entity);
			} else if (entity.getClass() == GlusterServer.class) {
				showViewsForGlusterServer((GlusterServer)entity);
			} else if (entity instanceof Volume) {
				showViewsForVolume((Volume)entity);
			} else if (entity instanceof Cluster) {
				showViewsForCluster((Cluster)entity);
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private void closeAllViews() {
		IViewReference[] viewReferences = page.getViewReferences();
		for (final IViewReference viewReference : viewReferences) {
			if (!(viewReference.getId().equals(NavigationView.ID) || viewReference.getId().equals(
					ConsoleConstants.TERMINAL_VIEW_ID))) {
				page.hideView(viewReference);
			}
		}
	}

	private void showViewsForCluster(Cluster cluster) throws PartInitException {
		page.showView(ClusterSummaryView.ID);
		page.showView(TasksView.ID, null, IWorkbenchPage.VIEW_CREATE);
	}

	private void showViewsForVolume(Volume volume) throws PartInitException {
		page.showView(VolumeSummaryView.ID);
		page.showView(VolumeBricksView.ID, null, IWorkbenchPage.VIEW_CREATE);
		page.showView(VolumeOptionsView.ID, null, IWorkbenchPage.VIEW_CREATE);
		page.showView(VolumeLogsView.ID, null, IWorkbenchPage.VIEW_CREATE);
	}

	private void showViewsForGlusterServer(GlusterServer server) throws PartInitException {
		page.showView(GlusterServerSummaryView.ID);
		if (server.getStatus() == GlusterServer.SERVER_STATUS.ONLINE) {
			page.showView(GlusterServerDisksView.ID, null, IWorkbenchPage.VIEW_CREATE);
			//page.showView(GlusterServerLogsView.ID, null, IWorkbenchPage.VIEW_CREATE);
		}
	}

	private void showViewsForDiscoveredServer(Server server) throws PartInitException {
		page.showView(DiscoveredServerView.ID);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void showViewsForEntityGroup(EntityGroup entityGroup) throws PartInitException {
		Class entityType = entityGroup.getEntityType();
		if (entityType == Server.class) {
			showViewForServers(entityGroup);
		} else if (entityType == Volume.class) {
			showViewsForVolumes(entityGroup);
		} else if (entityType == GlusterServer.class) {
			showViewsForGlusterServers(entityGroup);
		}
	}

	private void showViewsForGlusterServers(EntityGroup<GlusterServer> server) throws PartInitException {
		page.showView(GlusterServersSummaryView.ID);
		page.showView(GlusterServersView.ID, null, IWorkbenchPage.VIEW_CREATE);
		page.showView(DisksView.ID, null, IWorkbenchPage.VIEW_CREATE);
	}

	private void showViewsForVolumes(EntityGroup<Volume> volumes) throws PartInitException {
		page.showView(VolumesSummaryView.ID);
		page.showView(VolumesView.ID, null, IWorkbenchPage.VIEW_CREATE);
	}

	private void showViewForServers(EntityGroup<Server> servers) throws PartInitException {
		page.showView(DiscoveredServersView.ID);
	}
}