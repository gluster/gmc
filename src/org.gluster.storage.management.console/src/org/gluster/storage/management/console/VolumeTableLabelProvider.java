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
package org.gluster.storage.management.console;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.VolumesPage.VOLUME_TABLE_COLUMN_INDICES;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.Volume.VOLUME_STATUS;


public class VolumeTableLabelProvider implements ITableLabelProvider {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
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
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof Volume)) {
			return null;
		}
		
		Volume volume = (Volume) element;
		if(columnIndex == VOLUME_TABLE_COLUMN_INDICES.VOLUME_STATUS.ordinal()) {
			VOLUME_STATUS status = volume.getStatus();
			if(status == VOLUME_STATUS.ONLINE) {
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE_16x16);
			} else {
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE_16x16);
			}
		}
		
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Volume)) {
			return null;
		}

		Volume volume = (Volume) element;
		return (columnIndex == VOLUME_TABLE_COLUMN_INDICES.NAME.ordinal() ? volume.getName()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.VOLUME_TYPE.ordinal() ? volume.getVolumeTypeStr()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.TRANSPORT_TYPE.ordinal() ? volume.getTransportTypeStr()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.NUM_OF_BRICKS.ordinal() ? "" + volume.getNumOfBricks()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.VOLUME_STATUS.ordinal() ? volume.getStatusStr() : "Invalid");
	}
}
