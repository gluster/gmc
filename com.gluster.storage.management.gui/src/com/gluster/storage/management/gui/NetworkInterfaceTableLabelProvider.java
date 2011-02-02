package com.gluster.storage.management.gui;


import com.gluster.storage.management.core.model.NetworkInterface;
import com.gluster.storage.management.gui.views.details.tabcreators.GlusterServerTabCreator.NETWORK_INTERFACE_TABLE_COLUMN_INDICES;

public class NetworkInterfaceTableLabelProvider extends TableLabelProviderAdapter {
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof NetworkInterface)) {
			return null;
		}

		NetworkInterface networkInterface = (NetworkInterface) element;
		return (columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.INTERFACE.ordinal() ? networkInterface.getName() 
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.IP_ADDRESS.ordinal() ? networkInterface.getIpAddress()
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.NETMASK.ordinal() ? networkInterface.getNetMask()
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.GATEWAY.ordinal() ? networkInterface.getDefaultGateway()
			: columnIndex == NETWORK_INTERFACE_TABLE_COLUMN_INDICES.PREFERRED.ordinal() ? (networkInterface.isPreferred() ? "Yes" : "No")
			: "Invalid");
	}
}
