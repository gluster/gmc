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
package com.gluster.storage.management.console;

import org.eclipse.swt.graphics.Image;

import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.views.pages.GlusterServersPage.GLUSTER_SERVER_TABLE_COLUMN_INDICES;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.utils.NumberUtil;

public class GlusterServerTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof GlusterServer)) {
			return null;
		}
		
		GlusterServer server = (GlusterServer) element;
		if(columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			SERVER_STATUS status = server.getStatus();
			if(status == SERVER_STATUS.ONLINE) {
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE_16x16);
			} else {
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE_16x16);
			}
		}
		
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof GlusterServer)) {
			return null;
		}
		
		GlusterServer server = (GlusterServer) element;
		
		if (server.getStatus() == SERVER_STATUS.OFFLINE
				&& columnIndex != GLUSTER_SERVER_TABLE_COLUMN_INDICES.NAME.ordinal()
				&& columnIndex != GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			return "NA";
		}
		
		return (columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.NAME.ordinal() ? server.getName()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS.ordinal() ? server.getStatusStr()
			// : columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.PREFERRED_NETWORK.ordinal() ? server.getPreferredNetworkInterface().getName()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.NUM_OF_CPUS.ordinal() ? "" + server.getNumOfCPUs()
			//: columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.CPU_USAGE.ordinal() ? "" + server.getCpuUsage()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_MEMORY.ordinal() ? "" + NumberUtil.formatNumber((server.getTotalMemory() / 1024))
			//: columnIndex == SERVER_DISK_TABLE_COLUMN_INDICES.MEMORY_IN_USE.ordinal() ? "" + server.getMemoryInUse()
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_FREE_SPACE.ordinal() ? NumberUtil.formatNumber((server.getFreeDiskSpace() / 1024))
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.IP_ADDRESSES.ordinal() ? server.getIpAddressesAsString() 
			: columnIndex == GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_DISK_SPACE.ordinal() ? NumberUtil.formatNumber((server.getTotalDiskSpace() / 1024))
			: "Invalid");
	}
}
