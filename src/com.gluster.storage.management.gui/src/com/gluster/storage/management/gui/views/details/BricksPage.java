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

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.gui.BrickTableLabelProvider;

public class BricksPage extends AbstractBricksPage {

	public enum BRICK_TABLE_COLUMN_INDICES {
		SERVER, BRICK, FREE_SPACE, TOTAL_SPACE, STATUS
	};

	private static final String[] DISK_TABLE_COLUMN_NAMES = new String[] { "Server", "Brick Directory", "Free Space (GB)",
			"Total Space (GB)", "Status" };

	public BricksPage(final Composite parent, int style, IWorkbenchSite site, List<Brick> bricks) {
		super(parent, style, site, bricks);
	}

	@Override
	protected void setupDiskTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, DISK_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		guiHelper.setColumnProperties(table, BRICK_TABLE_COLUMN_INDICES.SERVER.ordinal(), SWT.CENTER, 100);
		guiHelper.setColumnProperties(table, BRICK_TABLE_COLUMN_INDICES.BRICK.ordinal(), SWT.CENTER, 100);
		guiHelper.setColumnProperties(table, BRICK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal(), SWT.CENTER, 90);
		guiHelper.setColumnProperties(table, BRICK_TABLE_COLUMN_INDICES.TOTAL_SPACE.ordinal(), SWT.CENTER, 90);
		
	}

	@Override
	protected ITableLabelProvider getTableLabelProvider() {
		return new BrickTableLabelProvider();
	}

	@Override
	protected int getStatusColumnIndex() {
		return BRICK_TABLE_COLUMN_INDICES.STATUS.ordinal();
	}

	@Override
	public void entityChanged(Entity entity, String[] paremeters) {
		// TODO Auto-generated method stub
		
	}
}