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
package org.gluster.storage.management.console;

import org.eclipse.swt.graphics.Image;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.TasksPage.TASK_TABLE_COLUMN_INDICES;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;



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
				return guiHelper.getImage(IImageKeys.COMPLETED_TASK_16x16);
			case Status.STATUS_CODE_PAUSE:
				return guiHelper.getImage(IImageKeys.PAUSE_TASK_16x16);
			case Status.STATUS_CODE_RUNNING:
				return guiHelper.getImage(IImageKeys.RESUME_TASK_16x16);
			case Status.STATUS_CODE_FAILURE:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE_16x16);				
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
