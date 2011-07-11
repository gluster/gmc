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
package com.gluster.storage.management.gui;

import org.eclipse.swt.graphics.Image;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.pages.DisksPage.DISK_TABLE_COLUMN_INDICES;

public class DiskTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private GlusterDataModelManager glusterDataModelManager = GlusterDataModelManager.getInstance();

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		if (!(element instanceof Disk)) {
			return null;
		}

		// Brick brick = (Brick) element;
		// Disk disk = GlusterDataModelManager.getInstance().getDisk(brick.getDiskName());
		Disk disk = (Disk) element;

		if (columnIndex == DISK_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			DEVICE_STATUS status = disk.getStatus();
			// TODO: Use different images for different statuses
			switch (status) {
			case INITIALIZED:
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE);
			case IO_ERROR:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			case UNINITIALIZED:
				return guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED);
			case INITIALIZING:
				return guiHelper.getImage(IImageKeys.WORK_IN_PROGRESS);
			default:
				throw new GlusterRuntimeException("Invalid disk status [" + status + "]");
			}
		}

		return null;
	}

	private String getDiskFreeSpace(Disk disk) {
		if (disk.hasErrors() || disk.isUninitialized()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber((disk.getFreeSpace() / 1024));
		}
	}

	private String getTotalDiskSpace(Disk disk) {
		if (disk.hasErrors() || disk.isUninitialized()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber((disk.getSpace() / 1024));
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Disk)) {
			return null;
		}

		Disk disk = (Disk) element;
		return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? disk.getServerName()
				: columnIndex == DISK_TABLE_COLUMN_INDICES.DISK.ordinal() ? disk.getName()
				: columnIndex == DISK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal() ? getDiskFreeSpace(disk)
				: columnIndex == DISK_TABLE_COLUMN_INDICES.TOTAL_SPACE.ordinal() ? getTotalDiskSpace(disk)
				: columnIndex == DISK_TABLE_COLUMN_INDICES.STATUS.ordinal() ? glusterDataModelManager.getDiskStatus(disk) : "Invalid");
	}
}
