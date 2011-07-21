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

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Device;
import com.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Partition;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.DeviceTableLabelProvider.DEVICE_COLUMN_INDICES;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.pages.ServerDisksPage.SERVER_DISK_TABLE_COLUMN_INDICES;

public class ServerDiskTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private GlusterDataModelManager glusterDataModelManager = GlusterDataModelManager.getInstance();

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof Device)) {
			return null;
		}

		Device device = (Device) element;
		if (columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			DEVICE_STATUS status = device.getStatus();
			
			if (status == null) {
				return  null;
			}
			
			if(element instanceof Disk && ((Disk)element).hasPartitions()) {
				// disk has partitions. so don't show status image at disk level.
				return null;
			}

			switch (status) {
			case INITIALIZED:
				if(glusterDataModelManager.isDeviceUsed(device)) {
					return guiHelper.getImage(IImageKeys.DISK_IN_USE);
				} else {
					return guiHelper.getImage(IImageKeys.DISK_AVAILABLE);
				}
			case IO_ERROR:
				return guiHelper.getImage(IImageKeys.IO_ERROR);
			case UNINITIALIZED:
				return guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED);
			case INITIALIZING:
				return guiHelper.getImage(IImageKeys.DISK_INITIALIZING_SMALL);
			default:
				throw new GlusterRuntimeException("Invalid disk status [" + status + "]");
			}
		}

		return null;
	}

	private String getDeviceFreeSpace(Device device) {
		if (device.hasErrors() || device.isUninitialized()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber((device.getFreeSpace() / 1024));
		}
	}

	private String getTotalDeviceSpace(Device device) {
		if (device.hasErrors() || device.isUninitialized()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber((device.getSpace() / 1024));
		}
	}

	public String getColumnText(Object element, int columnIndex) {
		Device device = (Device) element;
		if (columnIndex == DEVICE_COLUMN_INDICES.DISK.ordinal()) {
			// show value in "disk" column only if it's a disk
			if (device instanceof Disk) {
				return device.getName();
			} else {
				return "";
			}
		} 
		
		if(element instanceof Disk && ((Disk)element).hasPartitions()) {
			// disk has partitions. so don't show any other details
			return "";
		}

		if (columnIndex == DEVICE_COLUMN_INDICES.FREE_SPACE.ordinal()) {
			return "" + getDeviceFreeSpace(device);
		} else if (columnIndex == DEVICE_COLUMN_INDICES.SPACE_IN_USE.ordinal()) {
			return "" + getTotalDeviceSpace(device);
		} else if (columnIndex == DEVICE_COLUMN_INDICES.PARTITION.ordinal()) {
			if (device instanceof Partition) {
				return device.getName();
			} else {
				return "";
			}
		} else if (columnIndex == DEVICE_COLUMN_INDICES.STATUS.ordinal()) {
			if(device.isUninitialized()) {
				return "";
			}
			if(glusterDataModelManager.isDeviceUsed(device)) {
				return "In Use";
			} else {
				return device.getStatusStr();
			}
		} else {
			return "";
		}
	}	
	
}
