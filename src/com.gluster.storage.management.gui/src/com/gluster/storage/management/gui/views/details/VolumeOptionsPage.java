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

import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.VolumeOptionsTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeOptionsPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private TableViewer tableViewer;
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private Volume volume;

	public enum OPTIONS_TABLE_COLUMN_INDICES {
		OPTION_KEY, OPTION_VALUE
	};

	private static final String[] OPTIONS_TABLE_COLUMN_NAMES = new String[] { "Option Key", "Option Value" };

	public VolumeOptionsPage(Composite parent, int style) {
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
		setupDiskTableViewer(filterText);
	}

	public VolumeOptionsPage(final Composite parent, int style, Volume volume) {
		this(parent, style);
		this.volume = volume;

		tableViewer.setInput(volume.getOptions().entrySet().toArray());
		
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

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	private void setupDiskTable(Composite parent) {
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = createTableColumnLayout();
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table, OPTIONS_TABLE_COLUMN_INDICES.OPTION_KEY, SWT.CENTER, 100);
		setColumnProperties(table, OPTIONS_TABLE_COLUMN_INDICES.OPTION_VALUE, SWT.CENTER, 100);
	}
	
	private TableColumnLayout createTableColumnLayout() {
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		ColumnLayoutData defaultColumnLayoutData = new ColumnWeightData(100);

		tableColumnLayout.setColumnData(createKeyColumn(), defaultColumnLayoutData);
		tableColumnLayout.setColumnData(createValueColumn(), defaultColumnLayoutData);

		return tableColumnLayout;
	}
	
	private class OptionValueEditingSupport extends EditingSupport {
		private CellEditor cellEditor;

		public OptionValueEditingSupport(ColumnViewer viewer) {
			super(viewer);
			cellEditor = new TextCellEditor((Composite) viewer.getControl());
		}
		
		@Override
		protected void setValue(final Object element, final Object value) {
			final Entry<String, String> entry = (Entry<String, String>)element;
			if(entry.getValue().equals(value)) {
				// value is same as that present in the model. return without doing anything.
				return;
			}
			
			final Cursor oldCursor = getViewer().getControl().getCursor();
			//getViewer().getControl().setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_WAIT));
			// value has changed. set volume option at back-end and update model accordingly 
			BusyIndicator.showWhile(getDisplay(), new Runnable() {
				
				@Override
				public void run() {
					VolumesClient client = new VolumesClient(GlusterDataModelManager.getInstance().getSecurityToken());
					Status status = client.setVolumeOption(volume.getName(), entry.getKey(), (String)value);
					if(status.isSuccess()) {
						volume.setOption(entry.getKey(), (String)value);
					} else {
						MessageDialog.openError(getShell(), "Set Volume Option", status.getMessage());
					}
					getViewer().update(entry, null);
					//getViewer().refresh();
					//getViewer().getControl().setCursor(oldCursor);
				}
			});
		}
		
		@Override
		protected Object getValue(Object element) {
			return ((Entry<String, String>) element).getValue();
		}
		
		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}
		
		@Override
		protected boolean canEdit(Object element) {
			return true;
		}
	}

	private TableColumn createValueColumn() {
		TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		valueColumn.getColumn()
				.setText(OPTIONS_TABLE_COLUMN_NAMES[OPTIONS_TABLE_COLUMN_INDICES.OPTION_VALUE.ordinal()]);
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Entry<String, String>) element).getValue();
			}
		});
		
		// User can edit value of a volume option
		valueColumn.setEditingSupport(new OptionValueEditingSupport(valueColumn.getViewer()));
		
		return valueColumn.getColumn();
	}

	private TableColumn createKeyColumn() {
		TableViewerColumn keyColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		keyColumn.getColumn().setText(OPTIONS_TABLE_COLUMN_NAMES[OPTIONS_TABLE_COLUMN_INDICES.OPTION_KEY.ordinal()]);
		keyColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Entry<String, String>) element).getKey();
			}
		});
		return keyColumn.getColumn();
	}

	private void createDiskTableViewer(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		// TableViewer tableViewer = new TableViewer(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setLabelProvider(new VolumeOptionsTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		setupDiskTable(parent);
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	private void setupDiskTableViewer(final Text filterText) {
		Composite tableViewerComposite = createTableViewerComposite();
		createDiskTableViewer(tableViewerComposite);
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
	public void setColumnProperties(Table table, OPTIONS_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}
}
