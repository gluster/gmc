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
package com.gluster.storage.management.gui.views.navigator;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IImageKeys;

public class RootAdapter implements IWorkbenchAdapter {
	public enum NODE_TYPE {
		CLUSTER, SERVERS, VOLUMES, GSN
	};

	private Cluster cluster;
	private static final String LABEL_SERVERS = "Servers";
	private static final String LABEL_VOLUMES = "Volumes";
	private static final String LABEL_GSN = "GSN";

	public RootAdapter(Cluster cluster) {
		this.cluster = cluster;
	}

	@Override
	public Object getParent(Object o) {
		return cluster;
	}

	@Override
	public String getLabel(Object o) {
		switch (getNodeType(o)) {
		case CLUSTER:
			return cluster.getName();
		case VOLUMES:
			return LABEL_VOLUMES;
		case SERVERS:
			return LABEL_SERVERS;
		case GSN:
			return LABEL_GSN;
		default:
			return null;
		}
	}

	private NODE_TYPE getNodeType(Object obj) {
		if(obj instanceof Cluster) {
			return NODE_TYPE.CLUSTER;
		}
		
		if (obj instanceof String) {
			return NODE_TYPE.GSN;
		}

		if (obj instanceof List) {
			List<? extends Entity> list = (List<? extends Entity>) obj;
			if (list.size() == 0) {
				return null;
			}
			Entity firstEntity = list.get(0);
			return ((firstEntity instanceof Volume) ? NODE_TYPE.VOLUMES
					: NODE_TYPE.SERVERS);
		}

		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		String iconPath = null;
		switch (getNodeType(object)) {
		case CLUSTER:
			iconPath = IImageKeys.CLUSTER;
		case VOLUMES:
			iconPath = IImageKeys.VOLUMES;
		case SERVERS:
			iconPath = IImageKeys.SERVERS;
		case GSN:
			iconPath = IImageKeys.GSN;
		}
		return AbstractUIPlugin.imageDescriptorFromPlugin(
				Application.PLUGIN_ID, iconPath);
	}

	@Override
	public Object[] getChildren(Object o) {
		/*switch (getNodeType(o)) {
		case CLUSTER:
			return new Object[] {cluster.getVolumes(), cluster.getServers(), "GSN"};
		case VOLUMES:
			return cluster.getVolumes().toArray();
		case GLUSTER_SERVERS:
			return cluster.getServers().toArray();
		default:
			return null;
		}*/
		return null;
	}
}
