package com.gluster.storage.management.gui;

import org.eclipse.swt.graphics.Image;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.DisksPage.DISK_TABLE_COLUMN_INDICES;

public class DiskTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof Disk)) {
			return null;
		}

		Disk disk = (Disk) element;
		if (columnIndex == DISK_TABLE_COLUMN_INDICES.STATUS.ordinal()) {
			DISK_STATUS status = disk.getStatus();
			switch (status) {
			case READY:
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE);
			case OFFLINE:
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			case UNINITIALIZED:
				return guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED);
			case INITIALIZING:
				return guiHelper.getImage(IImageKeys.WORK_IN_PROGRESS);
			default:
				throw new GlusterRuntimeException("Invalid disk status [" + status + "]");
			}
		}

		return null;
	}

	private String getDiskSpaceInUse(Disk disk) {
		if (disk.isReady()) {
			return NumberUtil.formatNumber(disk.getSpaceInUse());
		} else {
			return "NA";
		}
	}

	private String getDiskSpace(Disk disk) {
		if (disk.isOffline()) {
			return "NA";
		} else {
			return NumberUtil.formatNumber(disk.getSpace());
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Disk)) {
			return null;
		}

		Disk disk = (Disk) element;
		return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? disk.getServer().getName()
				: columnIndex == DISK_TABLE_COLUMN_INDICES.DISK.ordinal() ? disk.getName()
				: columnIndex == DISK_TABLE_COLUMN_INDICES.SPACE.ordinal() ? getDiskSpace(disk)
				: columnIndex == DISK_TABLE_COLUMN_INDICES.SPACE_IN_USE.ordinal() ? getDiskSpaceInUse(disk)
				: columnIndex == DISK_TABLE_COLUMN_INDICES.STATUS.ordinal() ? disk.getStatusStr() : "Invalid");
	}
}
