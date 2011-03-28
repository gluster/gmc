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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.GlusterDummyModel;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.VolumeLogTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeLogsPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Text text;
	public enum LOG_TABLE_COLUMN_INDICES {
		DATE, TIME, DISK, SEVERITY, MESSAGE
	};

	private static final String[] LOG_TABLE_COLUMN_NAMES = new String[] { "Date", "Time", "Disk", "Severity", "Message" };

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public VolumeLogsPage(Composite parent, int style, Volume volume) {
		super(parent, style);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);
		
		setLayout(new GridLayout(1, false));
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		//layoutData.verticalIndent = 10;
		setLayoutData(layoutData);
		
		Composite composite = toolkit.createComposite(this, SWT.NONE);
		toolkit.paintBordersFor(composite);
		
		Label lblScanLast = toolkit.createLabel(composite, "Scan last", SWT.NONE);
		lblScanLast.setBounds(0, 15, 80, 20);
		
		text = toolkit.createText(composite, "100", SWT.NONE);
		text.setBounds(85, 15, 60, 20);
		
		Label lblMessagesAndFilter = toolkit.createLabel(composite, "messages, and filter on disk", SWT.NONE);
		lblMessagesAndFilter.setBounds(160, 15, 200, 20);
		
		Combo combo = new Combo(composite, SWT.NONE);
		combo.setBounds(365, 15, 100, 20);
		combo.setItems(new String[] {"ALL", "sda", "sdb", "sdc", "sdd"});
		toolkit.adapt(combo);
		toolkit.paintBordersFor(combo);
		combo.select(0);
		
		Label lblSeverity = toolkit.createLabel(composite, "Severity", SWT.NONE);
		lblSeverity.setBounds(480, 15, 70, 20);
		
		Combo combo_1 = new Combo(composite, SWT.NONE);
		combo_1.setBounds(555, 15, 110, 20);
		combo_1.setItems(new String[] {"ALL", "SEVERE", "WARNING", "DEBUG", "INFO"});
		toolkit.adapt(combo_1);
		toolkit.paintBordersFor(combo_1);
		combo_1.select(1);
		
		Label lblFrom = toolkit.createLabel(composite, "from", SWT.NONE);
		lblFrom.setBounds(0, 60, 40, 20);
		
		DateTime dateTime = new DateTime(composite, SWT.BORDER | SWT.DROP_DOWN);
		dateTime.setBounds(45, 60, 120, 20);
		toolkit.adapt(dateTime);
		toolkit.paintBordersFor(dateTime);

		DateTime dateTime_1 = new DateTime(composite, SWT.BORDER | SWT.TIME);
		dateTime_1.setBounds(171, 60, 120, 20);
		toolkit.adapt(dateTime_1);
		toolkit.paintBordersFor(dateTime_1);
		
		Label lblTo = toolkit.createLabel(composite, "To", SWT.NONE);
		lblTo.setBounds(329, 60, 26, 20);
		
		DateTime dateTime_2 = new DateTime(composite, SWT.BORDER | SWT.DROP_DOWN);
		dateTime_2.setBounds(355, 60, 120, 20);
		toolkit.adapt(dateTime_2);
		toolkit.paintBordersFor(dateTime_2);
		
		DateTime dateTime_3 = new DateTime(composite, SWT.BORDER | SWT.TIME);
		dateTime_3.setBounds(480, 60, 120, 20);
		toolkit.adapt(dateTime_3);
		toolkit.paintBordersFor(dateTime_3);
		
		Button btngo = toolkit.createButton(composite, "&Go", SWT.NONE);
		btngo.setBounds(605, 55, 60, 30);
		
		Label separator = toolkit.createLabel(composite, "", SWT.SEPARATOR | SWT.HORIZONTAL | SWT.FILL);
		separator.setBounds(0, 95, 680, 2);
		
		Label lblFilterString = toolkit.createLabel(composite, "Filter String", SWT.LEFT);
		lblFilterString.setBounds(0, 105, 85, 20);

		text = guiHelper.createFilterText(toolkit, composite);
		text.setBounds(90, 105, 250, 20);

		Composite tableViewerComposite = createTableViewerComposite();
		
		TableViewer tableViewer = new TableViewer(tableViewerComposite, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setLabelProvider(new VolumeLogTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		setupLogsTable(tableViewerComposite, tableViewer.getTable());
		guiHelper.createFilter(tableViewer, text, false);
		tableViewer.setInput(GlusterDummyModel.getDummyLogMessages().toArray());
	}
	
	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.verticalIndent = 10;
		tableViewerComposite.setLayoutData(layoutData);
		return tableViewerComposite;
	}
	
	private void setupLogsTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, LOG_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table, LOG_TABLE_COLUMN_INDICES.DATE, SWT.CENTER, 50);
		setColumnProperties(table, LOG_TABLE_COLUMN_INDICES.TIME, SWT.CENTER, 50);
		setColumnProperties(table, LOG_TABLE_COLUMN_INDICES.DISK, SWT.CENTER, 50);
		setColumnProperties(table, LOG_TABLE_COLUMN_INDICES.SEVERITY, SWT.CENTER, 50);
		setColumnProperties(table, LOG_TABLE_COLUMN_INDICES.MESSAGE, SWT.LEFT, 100);
	}
	
	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 */
	private void setColumnProperties(Table table, LOG_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}
}
