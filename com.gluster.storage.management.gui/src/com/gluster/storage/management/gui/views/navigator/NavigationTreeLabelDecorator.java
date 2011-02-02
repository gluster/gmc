package com.gluster.storage.management.gui.views.navigator;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IImageKeys;

public class NavigationTreeLabelDecorator implements ILightweightLabelDecorator {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof Volume) {
			Volume volume = (Volume) element;
			if (volume.getStatus() == Volume.VOLUME_STATUS.OFFLINE) {
				decoration.addOverlay(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
						IImageKeys.OVERLAY_OFFLINE));
			} else {
				decoration.addOverlay(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
						IImageKeys.OVERLAY_ONLINE));
			}
		}

		if (element instanceof GlusterServer) {
			GlusterServer server = (GlusterServer) element;
			if (server.getStatus() == GlusterServer.SERVER_STATUS.OFFLINE) {
				decoration.addOverlay(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
						IImageKeys.OVERLAY_OFFLINE));
			} else {
				decoration.addOverlay(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
						IImageKeys.OVERLAY_ONLINE));
			}
		}

		if (element instanceof Server) {
			decoration.addOverlay(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
					IImageKeys.OVERLAY_STAR));
		}
		
		if(element instanceof EntityGroup && ((EntityGroup)element).getEntityType() == Server.class) {
			decoration.addOverlay(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
					IImageKeys.OVERLAY_STAR));
		}
	}
}
