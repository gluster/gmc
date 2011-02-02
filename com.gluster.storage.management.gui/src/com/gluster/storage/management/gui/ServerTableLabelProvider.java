package com.gluster.storage.management.gui;


import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.ServersPage.SERVER_TABLE_COLUMN_INDICES;

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
