package com.gluster.storage.management.gui;


import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.LogMessage;
import com.gluster.storage.management.core.utils.DateUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.VolumeLogsPage.LOG_TABLE_COLUMN_INDICES;

public class VolumeLogTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	private String getFormattedDiskName(Disk disk) {
		return disk.getServer().getName() + ":" + disk.getName();
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
