/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console;

import org.eclipse.swt.graphics.Image;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.BricksPage.BRICK_TABLE_COLUMN_INDICES;
import org.gluster.storage.management.console.views.pages.DisksPage.DISK_TABLE_COLUMN_INDICES;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Device;
import org.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import org.gluster.storage.management.core.utils.NumberUtil;


public class BrickTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		if (!(element instanceof Brick)) {
			return null;
		}

		Brick brick = (Brick) element;
		if (columnIndex == DISK_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			BRICK_STATUS status = brick.getStatus();
			
			switch(status) {
			case ONLINE:
				return guiHelper.getImage(IImageKeys.BRICK_ONLINE_16x16);
			case OFFLINE:
				return guiHelper.getImage(IImageKeys.BRICK_OFFLINE_16x16);
			}
		}
		return null;
	}

	private String getDeviceFreeSpace(Device device) {
		if (device != null && device.isReady() && device.getFreeSpace() != null) {
			return NumberUtil.formatNumber((device.getFreeSpace() / 1024));
		} else {
			return "NA";
		}
	}

	private String getDeviceCapacity(Device device) {
		if (device != null && device.isReady() && device.getSpace() != null && device.getSpace() != 0.0) {
			return NumberUtil.formatNumber((device.getSpace() / 1024));
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
		Device device = GlusterDataModelManager.getInstance().getDeviceForBrickDir(brick);
		return (columnIndex == BRICK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? brick.getServerName()
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.BRICK.ordinal() ? brick.getBrickDirectory()
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal() ? getDeviceFreeSpace(device)
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.TOTAL_SPACE.ordinal() ? getDeviceCapacity(device)
				: columnIndex == BRICK_TABLE_COLUMN_INDICES.STATUS.ordinal() ? brick.getStatusStr() : "Invalid");
	}
}
