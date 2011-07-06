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
package com.gluster.storage.management.gui.toolbar;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.actions.IActionConstants;

public class GlusterToolbarManager implements ToolbarManager {
	private enum ENTITY_TYPE {
		CLUSTER, VOLUMES, VOLUME, GLUSTER_SERVERS, GLUSTER_SERVER, DISCOVERED_SERVERS, DISCOVERED_SERVER, TASK
	};

	private IWorkbenchWindow window;
	private final Map<Class<? extends Entity>, ENTITY_TYPE> entityTypeMap = createEntityTypeMap();
	private final Map<ENTITY_TYPE, String> actionSetMap = createActionSetMap();

	private Map<Class<? extends Entity>, ENTITY_TYPE> createEntityTypeMap() {
		Map<Class<? extends Entity>, ENTITY_TYPE> entityTypeMap = new HashMap<Class<? extends Entity>, GlusterToolbarManager.ENTITY_TYPE>();
		entityTypeMap.put(Cluster.class, ENTITY_TYPE.CLUSTER);
		entityTypeMap.put(Volume.class, ENTITY_TYPE.VOLUME);
		entityTypeMap.put(Server.class, ENTITY_TYPE.DISCOVERED_SERVER);
		entityTypeMap.put(GlusterServer.class, ENTITY_TYPE.GLUSTER_SERVER);
		entityTypeMap.put(TaskInfo.class, ENTITY_TYPE.TASK);

		return entityTypeMap;
	}

	private Map<ENTITY_TYPE, String> createActionSetMap() {
		Map<ENTITY_TYPE, String> actionSetMap = new HashMap<GlusterToolbarManager.ENTITY_TYPE, String>();
		actionSetMap.put(ENTITY_TYPE.CLUSTER, IActionConstants.ACTION_SET_CLUSTER);
		actionSetMap.put(ENTITY_TYPE.VOLUMES, IActionConstants.ACTION_SET_VOLUMES);
		actionSetMap.put(ENTITY_TYPE.VOLUME, IActionConstants.ACTION_SET_VOLUME);
		actionSetMap.put(ENTITY_TYPE.GLUSTER_SERVERS, IActionConstants.ACTION_SET_GLUSTER_SERVERS);
		actionSetMap.put(ENTITY_TYPE.GLUSTER_SERVER, IActionConstants.ACTION_SET_GLUSTER_SERVER);
		actionSetMap.put(ENTITY_TYPE.DISCOVERED_SERVERS, IActionConstants.ACTION_SET_DISCOVERED_SERVERS);
		actionSetMap.put(ENTITY_TYPE.DISCOVERED_SERVER, IActionConstants.ACTION_SET_DISCOVERED_SERVER);
		actionSetMap.put(ENTITY_TYPE.TASK, IActionConstants.ACTION_SET_TASK);

		return actionSetMap;
	}

	public GlusterToolbarManager(IWorkbenchWindow window) {
		this.window = window;
	}

	@SuppressWarnings("rawtypes")
	private ENTITY_TYPE getEntityType(Entity entity) {
		if (entity instanceof EntityGroup) {
			EntityGroup entityGroup = (EntityGroup) entity;
			if (entityGroup.getEntityType() == Volume.class) {
				return ENTITY_TYPE.VOLUMES;
			} else if (entityGroup.getEntityType() == GlusterServer.class) {
				return ENTITY_TYPE.GLUSTER_SERVERS;
			} else {
				return ENTITY_TYPE.DISCOVERED_SERVERS;
			}
		}

		return entityTypeMap.get(entity.getClass());
	}

	@Override
	public void updateToolbar(Entity entity) {
		ENTITY_TYPE entityType = getEntityType(entity);
		IWorkbenchPage page = window.getActivePage();
		
		for (ENTITY_TYPE targetEntityType : actionSetMap.keySet()) {
			String actionSetId = actionSetMap.get(targetEntityType);
			if (entityType == targetEntityType) {
				// show only the action set mapped to given entity
				page.showActionSet(actionSetId);
			} else {
				page.hideActionSet(actionSetId);
			}
		}
	}
}
