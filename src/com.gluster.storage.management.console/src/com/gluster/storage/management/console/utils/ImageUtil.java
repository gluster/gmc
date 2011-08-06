/**
 * ImageUtil.java
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
package com.gluster.storage.management.console.utils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.gluster.storage.management.console.Application;
import com.gluster.storage.management.core.utils.LRUCache;

/**
 *
 */
public class ImageUtil {
	private static final LRUCache<String, Image> imageCache = new LRUCache<String, Image>(20);
	
	public ImageDescriptor getImageDescriptor(String imagePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, imagePath);
	}

	public synchronized Image getImage(String imagePath) {
		if(imageCache.containsKey(imagePath)) {
			return imageCache.get(imagePath);
		}
		return createImage(imagePath);
	}
	
	private Image createImage(String imagePath) {
		Image image = getImageDescriptor(imagePath).createImage();
		imageCache.put(imagePath, image);
		return image;
	}
}