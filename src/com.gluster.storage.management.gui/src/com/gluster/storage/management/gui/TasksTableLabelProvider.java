/**
 * TasksTableLabelProvider.java
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
package com.gluster.storage.management.gui;

import org.eclipse.swt.graphics.Image;

import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.gui.DeviceTableLabelProvider.DEVICE_COLUMN_INDICES;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.pages.TasksPage.TASK_TABLE_COLUMN_INDICES;


public class TasksTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {

		if (!(element instanceof TaskInfo)) {
			return null;
		}
	
		TaskInfo taskInfo = (TaskInfo) element;
		if (columnIndex == TASK_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			int statusCode = taskInfo.getStatus().getCode();
			
			switch (statusCode) {
			case Status.STATUS_CODE_SUCCESS:
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE);
			case Status.STATUS_CODE_PAUSE:
				return guiHelper.getImage(IImageKeys.PAUSE_TASK_SMALL);
			case Status.STATUS_CODE_RUNNING:
				return guiHelper.getImage(IImageKeys.RESUME_TASK_SMALL);
			case Status.STATUS_CODE_FAILURE:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);				
			default:
				break;
			}
		}
		
		return null;
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof TaskInfo)) {
			return null;
		}

		TaskInfo taskInfo = (TaskInfo) element;
		return (columnIndex == TASK_TABLE_COLUMN_INDICES.TASK.ordinal()) ? taskInfo.getDescription().trim() : taskInfo.getStatus().getMessage().trim();
	}
}
