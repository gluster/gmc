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
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.pages.ServerDisksPage.SERVER_DISK_TABLE_COLUMN_INDICES;

public class ServerDiskTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private GlusterDataModelManager glusterDataModelManager = GlusterDataModelManager.getInstance();
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof Disk)) {
			return null;
		}
		
		Disk disk = (Disk) element;
		if (columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			DISK_STATUS status = disk.getStatus();
			switch (status) {
			case READY:
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
			return NumberUtil.formatNumber(disk.getFreeSpace());
		}
	}
	
	private String getTotalDiskSpace(Disk disk) {
		if (disk.hasErrors() || disk.isUninitialized()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber(disk.getSpace());
		}
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Disk)) {
			return null;
		}

		Disk disk = (Disk) element;
		String columnText = (columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.DISK.ordinal() ? disk.getName()
			: columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.SPACE.ordinal() ? getDiskFreeSpace(disk) 
			: columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.SPACE_IN_USE.ordinal() ? getTotalDiskSpace(disk)
			: columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.STATUS.ordinal() ? glusterDataModelManager.getDiskStatus(disk) // disk.getStatusStr()
			: "Invalid");
		return ((columnText == null || columnText.trim().equals("")) ? CoreConstants.NA : columnText); 
	}
}
