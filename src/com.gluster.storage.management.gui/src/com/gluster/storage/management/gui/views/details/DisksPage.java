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
package com.gluster.storage.management.gui.views.details;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchSite;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.gui.DiskTableLabelProvider;

public class DisksPage extends AbstractDisksPage {

	public enum DISK_TABLE_COLUMN_INDICES {
		SERVER, DISK, FREE_SPACE, SPACE_IN_USE, STATUS
	};

	private static final String[] DISK_TABLE_COLUMN_NAMES = new String[] { "Server", "Brick Directory", "Free Space (GB)",
			"Space in Use (GB)", "Status" };

	public DisksPage(final Composite parent, int style, IWorkbenchSite site, List<Disk> disks) {
		super(parent, style, site, disks);
	}

	@Override
	protected void setupDiskTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, DISK_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SERVER.ordinal(), SWT.CENTER, 100);
		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.DISK.ordinal(), SWT.CENTER, 100);
		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal(), SWT.CENTER, 90);
		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SPACE_IN_USE.ordinal(), SWT.CENTER, 90);
	}

	@Override
	protected ITableLabelProvider getTableLabelProvider() {
		return new DiskTableLabelProvider();
	}

	@Override
	protected int getStatusColumnIndex() {
		return DISK_TABLE_COLUMN_INDICES.STATUS.ordinal();
	}
}