package com.gluster.storage.management.gui;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.VolumesPage.VOLUME_TABLE_COLUMN_INDICES;

public class VolumeTableLabelProvider implements ITableLabelProvider {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (!(element instanceof Volume)) {
			return null;
		}
		
		Volume volume = (Volume) element;
		if(columnIndex == VOLUME_TABLE_COLUMN_INDICES.VOLUME_STATUS.ordinal()) {
			VOLUME_STATUS status = volume.getStatus();
			if(status == VOLUME_STATUS.ONLINE) {
				return guiHelper.getImage(IImageKeys.STATUS_ONLINE);
			} else {
				return guiHelper.getImage(IImageKeys.STATUS_OFFLINE);
			}
		}
		
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Volume)) {
			return null;
		}

		Volume volume = (Volume) element;
		return (columnIndex == VOLUME_TABLE_COLUMN_INDICES.NAME.ordinal() ? volume.getName()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.VOLUME_TYPE.ordinal() ? volume.getVolumeTypeStr()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.TRANSPORT_TYPE.ordinal() ? volume.getTransportTypeStr()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.NUM_OF_DISKS.ordinal() ? "" + volume.getNumOfDisks()
			: columnIndex == VOLUME_TABLE_COLUMN_INDICES.VOLUME_STATUS.ordinal() ? volume.getStatusStr() : "Invalid");
	}
}
