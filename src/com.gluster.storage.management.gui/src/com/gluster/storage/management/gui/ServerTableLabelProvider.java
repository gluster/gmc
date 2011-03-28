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


import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.pages.ServersPage.SERVER_TABLE_COLUMN_INDICES;

public class ServerTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Server)) {
			return null;
		}

		Server server = (Server) element;
		return (columnIndex == SERVER_TABLE_COLUMN_INDICES.NAME.ordinal() ? server.getName()
			: columnIndex == SERVER_TABLE_COLUMN_INDICES.IP_ADDRESSES.ordinal() ? server.getIpAddressesAsString()
			: columnIndex == SERVER_TABLE_COLUMN_INDICES.NUM_OF_DISKS.ordinal() ? "" + server.getNumOfDisks()
			: columnIndex == SERVER_TABLE_COLUMN_INDICES.TOTAL_DISK_SPACE.ordinal() ? NumberUtil.formatNumber(server.getTotalDiskSpace())
//			: columnIndex == SERVER_TABLE_COLUMN_INDICES.NUM_OF_CPUS.ordinal() ? "" + server.getNumOfCPUs()
//			: columnIndex == SERVER_TABLE_COLUMN_INDICES.CPU_USAGE.ordinal() ? "" + server.getCpuUsage()
//			: columnIndex == SERVER_TABLE_COLUMN_INDICES.TOTAL_MEMORY.ordinal() ? "" + server.getTotalMemory()
//			: columnIndex == SERVER_TABLE_COLUMN_INDICES.MEMORY_IN_USE.ordinal() ? "" + server.getMemoryInUse()
//			: columnIndex == SERVER_TABLE_COLUMN_INDICES.DISK_SPACE_IN_USE.ordinal() ? "" + server.getDiskSpaceInUse() 
			: "Invalid");
	}
}
