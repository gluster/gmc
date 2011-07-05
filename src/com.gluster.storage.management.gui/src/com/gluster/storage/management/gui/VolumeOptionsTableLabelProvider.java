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

import java.util.Map.Entry;

import com.gluster.storage.management.gui.views.pages.VolumeOptionsPage.OPTIONS_TABLE_COLUMN_INDICES;

public class VolumeOptionsTableLabelProvider extends TableLabelProviderAdapter {
	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof Entry)) {
			return null;
		}

		Entry<String, String> entry = (Entry<String, String>) element;
		String key = entry.getKey();
		String value = entry.getValue();
		return (columnIndex == OPTIONS_TABLE_COLUMN_INDICES.OPTION_KEY.ordinal() ? key
			: columnIndex == OPTIONS_TABLE_COLUMN_INDICES.OPTION_VALUE.ordinal() ? value
			: "Invalid");
	}
}
