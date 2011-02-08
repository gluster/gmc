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
package com.gluster.storage.management.gui.utils;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.Disk;

public class DiskViewerEditingSupport extends EditingSupport {
	private FormToolkit toolkit;
	
	public DiskViewerEditingSupport(FormToolkit toolkit, TableViewer viewer) {
		super(viewer);
		this.toolkit = toolkit;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new HyperlinkCellEditor(toolkit, (TableViewer)getViewer(), (Disk) element);
	}

	@Override
	protected boolean canEdit(Object element) {
		Disk disk = (Disk) element;
		return (disk.isUninitialized());
	}

	@Override
	protected Object getValue(Object element) {
		return ((Disk) element).getStatus();
	}

	@Override
	protected void setValue(Object element, Object value) {
		getViewer().update(element, new String[] { "status" });
	}

}
