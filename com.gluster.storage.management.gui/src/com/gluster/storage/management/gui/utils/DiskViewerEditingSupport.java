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
