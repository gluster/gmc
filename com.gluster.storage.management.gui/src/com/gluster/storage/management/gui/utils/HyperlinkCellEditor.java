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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.gluster.storage.management.core.model.Disk;

public class HyperlinkCellEditor extends CellEditor {
	private FormToolkit toolkit;
	private Disk disk;
	private ImageHyperlink link;
	private TableViewer viewer;
	
	public HyperlinkCellEditor(FormToolkit toolkit, TableViewer viewer, Disk disk) {
		this.toolkit = toolkit;
		this.viewer = viewer;
		this.disk = disk;
	}
	
	@Override
	protected Control createControl(Composite parent) {
		link = toolkit.createImageHyperlink(viewer.getTable(), SWT.NONE);
		// link.setImage(guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED));
		link.setText("Initialize");
		return link;
	}

	@Override
	protected Object doGetValue() {
		return disk.getStatus();
	}

	@Override
	protected void doSetFocus() {
		link.setFocus();
	}

	@Override
	protected void doSetValue(Object value) {
		if(!disk.isUninitialized()) {
			this.deactivate();
			this.dispose();
		}
	}
}
