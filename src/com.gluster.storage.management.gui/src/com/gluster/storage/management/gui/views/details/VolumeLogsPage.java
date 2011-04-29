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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.LogMessage;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.response.LogMessageListResponse;
import com.gluster.storage.management.gui.VolumeLogTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeLogsPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Text filterText;
	private Text lineCountText;
	private Volume volume;
	
	public enum LOG_TABLE_COLUMN_INDICES {
		DATE, TIME, DISK, SEVERITY, MESSAGE
	};

	private static final String[] LOG_TABLE_COLUMN_NAMES = new String[] { "Date", "Time", "Disk", "Severity", "Message" };
	private TableViewer tableViewer;

	/**
	 * Create the volume logs page
	 * @param parent
	 * @param style
	 * @param volume Volume for which the logs page is to be created	
	 */
	public VolumeLogsPage(Composite parent, int style, Volume volume) {
		super(parent, style);
		this.volume = volume;
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);
		
		configureLayout();
		
		Composite composite = toolkit.createComposite(this, SWT.NONE);
		toolkit.paintBordersFor(composite);
		
		createLineCountLabel(composite);
		createLineCountText(composite);
		
		createDiskLabel(composite);
		createDisksCombo(composite);
		
		createSeverityLabel(composite);
		createSeverityCombo(composite);
		
		createFromDateLabel(composite);
		createFromDateField(composite);
		createFromTimeField(composite);
		
		createToDateLabel(composite);
		createToDateField(composite);
		createToTimeField(composite);
		
		createSearchButton(composite);
		
		createSeparator(composite);
		
		createFilterLabel(composite);
		createFilterText(composite);

		createLogTableViewer();
	}

	private void createLogTableViewer() {
		Composite tableViewerComposite = createTableViewerComposite();
		
		tableViewer = new TableViewer(tableViewerComposite, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setLabelProvider(new VolumeLogTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		setupLogsTable(tableViewerComposite, tableViewer.getTable());
		guiHelper.createFilter(tableViewer, filterText, false);
	}

	private void createFilterText(Composite composite) {
		filterText = guiHelper.createFilterText(toolkit, composite);
		filterText.setBounds(90, 105, 250, 20);
	}

	private void createFilterLabel(Composite composite) {
		Label lblFilterString = toolkit.createLabel(composite, "Filter String", SWT.LEFT);
		lblFilterString.setBounds(0, 105, 85, 20);
	}

	private void createSeparator(Composite composite) {
		Label separator = toolkit.createLabel(composite, "", SWT.SEPARATOR | SWT.HORIZONTAL | SWT.FILL);
		separator.setBounds(0, 95, 680, 2);
	}

	private void createSearchButton(Composite composite) {
		Button btnGo = toolkit.createButton(composite, "&Go", SWT.NONE);
		btnGo.setBounds(605, 55, 60, 30);
		btnGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				VolumesClient client = new VolumesClient(GlusterDataModelManager.getInstance().getSecurityToken());
				LogMessageListResponse response = client.getLogs(volume.getName(), Integer.parseInt(lineCountText.getText()));
				Status status = response.getStatus();
				if(status.isSuccess()) {
					List<LogMessage> logMessages = response.getLogMessages();
					tableViewer.setInput(logMessages.toArray(new LogMessage[0]));
					tableViewer.refresh();
				} else {
					MessageDialog.openError(getShell(), "Volume Logs", "Error while fetching volume logs: [" + status
							+ "]");
				}
			}
		});
	}

	private void createToTimeField(Composite composite) {
		DateTime toTime = new DateTime(composite, SWT.BORDER | SWT.TIME);
		toTime.setBounds(480, 60, 120, 20);
		toolkit.adapt(toTime);
		toolkit.paintBordersFor(toTime);
	}

	private void createToDateField(Composite composite) {
		DateTime toDate = new DateTime(composite, SWT.BORDER | SWT.DROP_DOWN);
		toDate.setBounds(355, 60, 120, 20);
		toolkit.adapt(toDate);
		toolkit.paintBordersFor(toDate);
	}

	private void createToDateLabel(Composite composite) {
		Label lblTo = toolkit.createLabel(composite, "To", SWT.NONE);
		lblTo.setBounds(329, 60, 26, 20);
	}

	private void createFromTimeField(Composite composite) {
		DateTime fromTime = new DateTime(composite, SWT.BORDER | SWT.TIME);
		fromTime.setBounds(171, 60, 120, 20);
		toolkit.adapt(fromTime);
		toolkit.paintBordersFor(fromTime);
	}

	private void createFromDateField(Composite composite) {
		DateTime fromDate = new DateTime(composite, SWT.BORDER | SWT.DROP_DOWN);
		fromDate.setBounds(45, 60, 120, 20);
		toolkit.adapt(fromDate);
		toolkit.paintBordersFor(fromDate);
	}

	private void createFromDateLabel(Composite composite) {
		Label lblFrom = toolkit.createLabel(composite, "from", SWT.NONE);
		lblFrom.setBounds(0, 60, 40, 20);
	}

	private void createSeverityCombo(Composite composite) {
		Combo severityCombo = new Combo(composite, SWT.NONE);
		severityCombo.setBounds(555, 15, 110, 20);
		severityCombo.setItems(new String[] {"ALL", "SEVERE", "WARNING", "DEBUG", "INFO"});
		toolkit.adapt(severityCombo);
		toolkit.paintBordersFor(severityCombo);
		severityCombo.select(1);
	}

	private void createSeverityLabel(Composite composite) {
		Label lblSeverity = toolkit.createLabel(composite, "Severity", SWT.NONE);
		lblSeverity.setBounds(480, 15, 70, 20);
	}

	private void createDisksCombo(Composite composite) {
		Combo disksCombo = new Combo(composite, SWT.NONE);
		disksCombo.setBounds(365, 15, 100, 20);
		disksCombo.setItems(new String[] {"ALL", "sda", "sdb", "sdc", "sdd"});
		toolkit.adapt(disksCombo);
		toolkit.paintBordersFor(disksCombo);
		disksCombo.select(0);
	}

	private void createDiskLabel(Composite composite) {
		Label lblMessagesAndFilter = toolkit.createLabel(composite, "messages, and filter on disk", SWT.NONE);
		lblMessagesAndFilter.setBounds(160, 15, 200, 20);
	}

	private void createLineCountText(Composite composite) {
		lineCountText = toolkit.createText(composite, "100", SWT.NONE);
		lineCountText.setBounds(85, 15, 60, 20);
	}

	private void createLineCountLabel(Composite composite) {
		Label lblScanLast = toolkit.createLabel(composite, "Scan last", SWT.NONE);
		lblScanLast.setBounds(0, 15, 80, 20);
	}

	private void configureLayout() {
		setLayout(new GridLayout(1, false));
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		//layoutData.verticalIndent = 10;
		setLayoutData(layoutData);
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
