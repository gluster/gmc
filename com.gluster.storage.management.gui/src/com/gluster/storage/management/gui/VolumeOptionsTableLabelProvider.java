package com.gluster.storage.management.gui;

import java.util.Map.Entry;

import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.VolumeOptionsPage.OPTIONS_TABLE_COLUMN_INDICES;

public class VolumeOptionsTableLabelProvider extends TableLabelProviderAdapter {
	private GUIHelper guiHelper = GUIHelper.getInstance();
	
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Entry)) {
			return null;
		}

		Entry<String, String> entry = (Entry) element;
		String key = entry.getKey();
		String value = entry.getValue();
		return (columnIndex == OPTIONS_TABLE_COLUMN_INDICES.OPTION_KEY.ordinal() ? key
			: columnIndex == OPTIONS_TABLE_COLUMN_INDICES.OPTION_VALUE.ordinal() ? value
			: "Invalid");
	}
}
