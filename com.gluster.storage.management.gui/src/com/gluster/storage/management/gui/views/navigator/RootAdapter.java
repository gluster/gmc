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
