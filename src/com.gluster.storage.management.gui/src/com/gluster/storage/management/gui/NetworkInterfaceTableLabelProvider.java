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


import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.NetworkInterface;
import com.gluster.storage.management.gui.views.details.tabcreators.GlusterServerTabCreator.NETWORK_INTERFACE_TABLE_COLUMN_INDICES;

public class NetworkInterfaceTableLabelProvider extends TableLabelProviderAdapter {
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof NetworkInterface)) {
			return null;
		}

		NetworkInterface networkInterface = (NetworkInterface) element;
		String columnText = (columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.INTERFACE.ordinal() ? networkInterface.getName() 
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.MODEL.ordinal() ? networkInterface.getModel()
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.SPEED.ordinal() ? networkInterface.getSpeed()
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.IP_ADDRESS.ordinal() ? networkInterface.getIpAddress()
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.NETMASK.ordinal() ? networkInterface.getNetMask()
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.GATEWAY.ordinal() ? networkInterface.getDefaultGateway()
			: "Invalid");
		return ((columnText == null || columnText.trim().equals("")) ? CoreConstants.NA : columnText);
	}
}
