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
