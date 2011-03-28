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

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.gui.EntityGroupContentProvider;
import com.gluster.storage.management.gui.GlusterServerTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class GlusterServersPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private TableViewer tableViewer;
	private GUIHelper guiHelper = GUIHelper.getInstance();

	public enum GLUSTER_SERVER_TABLE_COLUMN_INDICES {
		NAME, IP_ADDRESSES, NUM_OF_CPUS, TOTAL_MEMORY, TOTAL_DISK_SPACE, STATUS // Removed PREFERRED_NETWORK 
	};

	private static final String[] GLUSTER_SERVER_TABLE_COLUMN_NAMES = new String[] { "Name", 
			"IP Address(es)", "Number\nof CPUs", "Total\nMemory (GB)", "Total Disk\n Space (GB)", "Status" }; // Removed "Preferred\nNetwork", 

	public GlusterServersPage(Composite parent, int style) {
		super(parent, style);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		setupPageLayout();
		Text filterText = guiHelper.createFilterText(toolkit, this);
		setupServerTableViewer(filterText);
	}

	public GlusterServersPage(final Composite parent, int style, EntityGroup<GlusterServer> servers) {
		this(parent, style);

		tableViewer.setInput(servers);
		parent.layout(); // Important - this actually paints the table

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

	public void addDoubleClickListener(IDoubleClickListener listener) {
		tableViewer.addDoubleClickListener(listener);
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	private void setupServerTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, GLUSTER_SERVER_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.NAME, SWT.CENTER, 100);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS, SWT.CENTER, 70);
		// setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.PREFERRED_NETWORK, SWT.CENTER, 90);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.NUM_OF_CPUS, SWT.CENTER, 90);
		//setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.CPU_USAGE, SWT.CENTER, 90);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_MEMORY, SWT.CENTER, 90);
		//setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.MEMORY_IN_USE, SWT.CENTER, 90);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_DISK_SPACE, SWT.CENTER, 90);
		//setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.DISK_SPACE_IN_USE, SWT.CENTER, 90);
	}

	private TableViewer createServerTableViewer(Composite parent) {
		TableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		//TableViewer tableViewer = new TableViewer(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setLabelProvider(new GlusterServerTableLabelProvider());
		tableViewer.setContentProvider(new EntityGroupContentProvider<GlusterServer>());

		setupServerTable(parent, tableViewer.getTable());

		return tableViewer;
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	private void setupServerTableViewer(final Text filterText) {
		Composite tableViewerComposite = createTableViewerComposite();
		tableViewer = createServerTableViewer(tableViewerComposite);
		// Create a case insensitive filter for the table viewer using the filter text field
		guiHelper.createFilter(tableViewer, filterText, false);
	}

	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 */
	public void setColumnProperties(Table table, GLUSTER_SERVER_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}
}
