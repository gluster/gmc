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
