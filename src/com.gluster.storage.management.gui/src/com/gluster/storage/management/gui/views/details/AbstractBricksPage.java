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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IEntityListener;
import com.gluster.storage.management.gui.jobs.InitializeDiskJob;
import com.gluster.storage.management.gui.utils.GUIHelper;

public abstract class AbstractBricksPage extends Composite implements IEntityListener {
	protected final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	protected CheckboxTableViewer tableViewer;
	private IWorkbenchSite site;
	protected static final GUIHelper guiHelper = GUIHelper.getInstance();

	/**
	 * Setup properties of the table e.g. column headers, widths, etc.
	 * 
	 * @param parent
	 *            The parent composite. (TableColumnLayout has to be set on this)
	 * @param table
	 *            The table to be set up
	 */
	protected abstract void setupDiskTable(Composite parent, Table table);

	/**
	 * @return The label provider to be used with the disk table viewer
	 */
	protected abstract ITableLabelProvider getTableLabelProvider();

	/**
	 * @return Index of the "status" column in the table. Return -1 if status column is not displayed
	 */
	protected abstract int getStatusColumnIndex();

	private void init(final Composite parent, IWorkbenchSite site, List<Brick> bricks) {
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});

		this.site = site;

		setupPageLayout();
		Text filterText = guiHelper.createFilterText(toolkit, this);
		setupBrickTableViewer(createTableViewerComposite(), filterText);
		site.setSelectionProvider(tableViewer);

		tableViewer.setInput(bricks);

		site.setSelectionProvider(tableViewer);
		Application.getApplication().addEntityListener(this);

		parent.layout(); // Important - this actually paints the table

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		/**
		 * Ideally not required. However the table viewer is not getting laid out properly on performing
		 * "maximize + restore" So this is a hack to make sure that the table is laid out again on re-size of the window
		 */
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				parent.layout();
			}
		});
	}

	public AbstractBricksPage(final Composite parent, int style, IWorkbenchSite site, List<Brick> bricks) {
		super(parent, style);
		init(parent, site, bricks);
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	private CheckboxTableViewer createBrickTableViewer(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI );

		tableViewer.setLabelProvider(getTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		setupDiskTable(parent, tableViewer.getTable());
		
		// make sure that table selection is driven by checkbox selection
		guiHelper.configureCheckboxTableViewer(tableViewer);

		return tableViewer;
	}

	private void setupBrickTableViewer(Composite parent, final Text filterText) {
		tableViewer = createBrickTableViewer(parent);
		// Create a case insensitive filter for the table viewer using the filter text field
		guiHelper.createFilter(tableViewer, filterText, false);
	}
}
