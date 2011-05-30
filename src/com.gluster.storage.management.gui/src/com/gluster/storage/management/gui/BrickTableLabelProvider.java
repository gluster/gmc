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
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.BricksPage.BRICK_TABLE_COLUMN_INDICES;
import com.gluster.storage.management.gui.views.details.DisksPage.DISK_TABLE_COLUMN_INDICES;

public class BrickTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		
		if (!(element instanceof Brick)) {
			return null;
		}

		Brick brick = (Brick) element;
		Disk disk = GlusterDataModelManager.getInstance().getDiskDetails(brick.getDiskName());
		
		if (columnIndex == DISK_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			DISK_STATUS status = disk.getStatus();
			switch (status) {
			case READY:
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE);
			case IO_ERROR:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			case UNINITIALIZED:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			case INITIALIZING:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			default:
				throw new GlusterRuntimeException("Invalid brick status [" + status + "]");
			}
		}

		return null;
	}

	private String getDiskSpaceInUse(Disk disk) {
		if (disk.isReady() && disk.getSpaceInUse() != null) {
			return NumberUtil.formatNumber(disk.getSpaceInUse());
		} else {
			return "NA";
		}
	}

	private String getDiskFreeSpace(Disk disk) {
		if (disk.isReady() && disk.getFreeSpace() != null) {
			return NumberUtil.formatNumber(disk.getFreeSpace());
		} else {
			return "NA";
		}
	}
	
	private String getDiskSpace( Disk disk) {
		if (disk.isReady() && disk.getSpace() != null && disk.getSpace() != 0.0) {
			return NumberUtil.formatNumber(disk.getSpace());
		} else {
			return "NA";
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Brick)) {
			return null;
		}

		Brick brick = (Brick) element;
		Disk disk = GlusterDataModelManager.getInstance().getDiskDetails(brick.getDiskName());
		return (columnIndex == BRICK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? brick.getServerName()
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.BRICK.ordinal() ? brick.getBrickDirectory()
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.SPACE.ordinal() ? getDiskSpace(disk)
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal() ? getDiskFreeSpace(disk)
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.STATUS.ordinal() ? brick.getStatusStr() : "Invalid");
	}
}
