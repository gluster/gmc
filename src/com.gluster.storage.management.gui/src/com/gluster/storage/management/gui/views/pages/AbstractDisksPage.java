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
package com.gluster.storage.management.gui.views.pages;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IEntityListener;
import com.gluster.storage.management.gui.jobs.InitializeDiskJob;

public abstract class AbstractDisksPage extends AbstractTableViewerPage<Disk> implements IEntityListener {
	private List<Disk> disks;
	
	/**
	 * @return Index of the "status" column in the table. Return -1 if status column is not displayed
	 */
	protected abstract int getStatusColumnIndex();

	public AbstractDisksPage(final Composite parent, int style, IWorkbenchSite site, List<Disk> disks) {
		super(site, parent, style, true, true, disks);
		this.disks = disks;
		
		// creates hyperlinks for "unitialized" disks
		setupStatusCellEditor(); 
		// Listen for disk status change events
		Application.getApplication().addEntityListener(this);
	}
	
	@Override
	protected IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}
	
	@Override
	protected List<Disk> getAllEntities() {
		return disks;
	}
	
	@Override
	protected ClusterListener createClusterListener() {
		return new DefaultClusterListener();
	}

	private void createInitializeLink(final TableItem item, final int rowNum, final Disk disk) {
		final Table table = tableViewer.getTable();
		final TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.RIGHT;

		table.addPaintListener(new PaintListener() {
			private TableItem myItem = item;
			private int myRowNum = rowNum;
			private ImageHyperlink myLink = null;
			private TableEditor myEditor = null;

			private void createLinkFor(Disk disk1, TableItem item1, int rowNum1) {
				myItem = item1;
				myRowNum = rowNum1;

				myEditor = new TableEditor(table);
				myEditor.grabHorizontal = true;
				myEditor.horizontalAlignment = SWT.RIGHT;

				myLink = toolkit.createImageHyperlink(table, SWT.NONE);
				// link.setImage(guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED));
				myLink.setText("Initialize");
				myLink.addHyperlinkListener(new StatusLinkListener(myLink, myEditor, myItem, tableViewer, disk1));

				myEditor.setEditor(myLink, item1, getStatusColumnIndex());

				myItem.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						myLink.dispose();
						myEditor.dispose();
					}
				});
			}

			@Override
			public void paintControl(PaintEvent e) {
				int itemCount = table.getItemCount();

				// Find the table item corresponding to our disk
				Disk disk1 = null;
				int rowNum1 = -1;
				TableItem item1 = null;
				for (int i = 0; i < itemCount; i++) {
					item1 = table.getItem(i);
					disk1 = (Disk) item1.getData();
					if (disk1 != null && disk1 == disk) {
						rowNum1 = i;
						break;
					}
				}

				if (rowNum1 == -1) {
					// item disposed and disk not visible. nothing to do.
					return;
				}

				if (myEditor == null || myItem.isDisposed()) {
					// item visible, and
					// either editor never created, OR
					// old item disposed. create the link for it
					createLinkFor(disk1, item1, rowNum1);
				}

				if (rowNum1 != myRowNum) {
					// disk visible, but at a different row num. re-create the link
					myLink.dispose();
					myEditor.dispose();
					createLinkFor(disk1, item1, rowNum1);
				}

				myEditor.layout(); // IMPORTANT. Without this, the link location goes for a toss on maximize + restore
			}
		});
	}

	private void setupStatusCellEditor() {
		final TableViewer viewer = tableViewer;
		final Table table = viewer.getTable();
		for (int i = 0; i < table.getItemCount(); i++) {
			final TableItem item = table.getItem(i);
			if (item.isDisposed() || item.getData() == null) {
				continue;
			}
			final Disk disk = (Disk) item.getData();
			if (disk.isUninitialized()) {
				createInitializeLink(item, i, disk);
			}
		}
	}

	private final class StatusLinkListener extends HyperlinkAdapter {
		private final Disk disk;
		private final TableEditor myEditor;
		private final ImageHyperlink myLink;
		private final TableViewer viewer;

		private StatusLinkListener(ImageHyperlink link, TableEditor editor, TableItem item, TableViewer viewer,
				Disk disk) {
			this.disk = disk;
			this.viewer = viewer;
			this.myEditor = editor;
			this.myLink = link;
		}

		private void updateStatus(final DISK_STATUS status, final boolean disposeEditor) {
			if (disposeEditor) {
				myLink.dispose();
				myEditor.dispose();
			}
			disk.setStatus(status);
			viewer.update(disk, new String[] { "status" });
			Application.getApplication().entityChanged(disk, new String[] { "status" });
		}

		@Override
		public void linkActivated(HyperlinkEvent e) {
			updateStatus(DISK_STATUS.INITIALIZING, true);
			
			GlusterServersClient serversClient = new GlusterServersClient();
			serversClient.initializeDisk(disk.getServerName(), disk.getName());
			
			guiHelper.showProgressView();
			new InitializeDiskJob(disk).schedule();
		}
	}

	@Override
	public void entityChanged(final Entity entity, final String[] paremeters) {
		if (!(entity instanceof Disk)) {
			return;
		}
		final Disk disk = (Disk) entity;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				tableViewer.update(disk, paremeters);

				if (disk.isUninitialized()) {
					Table table = tableViewer.getTable();

					for (int rowNum = 0; rowNum < table.getItemCount(); rowNum++) {
						TableItem item = table.getItem(rowNum);
						if (item.getData() == disk) {
							createInitializeLink(item, rowNum, disk);
						}
					}
				}
			}
		});
	}
}
