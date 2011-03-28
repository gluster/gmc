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


import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.LogMessage;
import com.gluster.storage.management.core.utils.DateUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.VolumeLogsPage.LOG_TABLE_COLUMN_INDICES;

public class VolumeLogTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	private String getFormattedDiskName(Disk disk) {
		return disk.getServerName() + ":" + disk.getName();
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof LogMessage)) {
			return null;
		}

		LogMessage logMessage = (LogMessage) element;
		return (columnIndex == LOG_TABLE_COLUMN_INDICES.DATE.ordinal() ? DateUtil.formatDate(logMessage.getTimestamp()) 
			: columnIndex == LOG_TABLE_COLUMN_INDICES.TIME.ordinal() ? DateUtil.formatTime(logMessage.getTimestamp())
			: columnIndex == LOG_TABLE_COLUMN_INDICES.DISK.ordinal() ? getFormattedDiskName(logMessage.getDisk())
			: columnIndex == LOG_TABLE_COLUMN_INDICES.SEVERITY.ordinal() ? "" + logMessage.getSeverity()
			: columnIndex == LOG_TABLE_COLUMN_INDICES.MESSAGE.ordinal() ? logMessage.getMessage() : "Invalid");
	}
}
