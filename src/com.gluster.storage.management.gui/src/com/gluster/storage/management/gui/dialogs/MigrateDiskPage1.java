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
package com.gluster.storage.management.gui.dialogs;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.GlusterDummyModel;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.TableLabelProviderAdapter;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class MigrateDiskPage1 extends WizardPage {
	private static final String PAGE_NAME = "migrate.disk.page.1";
	private enum DISK_TABLE_COLUMN_INDICES {
		SERVER, DISK, SPACE, SPACE_IN_USE
	}
	private static final String[] DISK_TABLE_COLUMN_NAMES = { "Server", "Disk", "Space (GB)", "Used Space (GB)" };

	private Volume volume;
	private Disk fromDisk;
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	private ITableLabelProvider getDiskLabelProvider() {
		return new TableLabelProviderAdapter() {

			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (!(element instanceof Disk)) {
					return null;
				}

				Disk disk = (Disk) element;
				return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? disk.getServerName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.DISK.ordinal() ? disk.getName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.SPACE.ordinal() ? NumberUtil.formatNumber(disk.getSpace())
						: columnIndex == DISK_TABLE_COLUMN_INDICES.SPACE_IN_USE.ordinal() ? NumberUtil.formatNumber(disk.getSpaceInUse()) 
						: "Invalid");
			}
		};
	}
	
	private void setupDiskTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, DISK_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SERVER, SWT.CENTER, 100);
		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.DISK, SWT.CENTER, 100);
		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SPACE, SWT.CENTER, 90);
		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SPACE_IN_USE, SWT.CENTER, 90);
	}

	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 */
	public void setColumnProperties(Table table, DISK_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}


	/**
	 * Create the wizard.
	 */
	public MigrateDiskPage1(Volume volume, Disk disk) {
		super(PAGE_NAME);
		this.volume = volume;
		this.fromDisk = disk;
		setTitle("Migrate Disk [" + volume.getName() + "]");
//		setDescription("Migrate data from one disk to another for the chosen Volume. " +
//				"This will copy all data present in the \"from disk\" of the volume " +
//				"to \"to disk\", remove \"from disk\" from the volume, and " +
//				"add \"to disk\" to the volume");
		setDescription("Migrate volume data from \"From Disk\" to \"To Disk\"");
	}

	private void setupPageLayout(Composite container) {
		final GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 10;
		layout.marginTop = 10;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		container.setLayout(layout);
	}
	
	private Composite createTableViewerComposite(Composite parent) {
		Composite tableViewerComposite = new Composite(parent, SWT.NONE);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		
		setupPageLayout(container);

		GridData labelLayoutData = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
		labelLayoutData.minimumWidth = 100;
		labelLayoutData.verticalAlignment = SWT.BOTTOM;
		//labelLayoutData.verticalIndent = 10;
		
		Label lblFromDisk = new Label(container, SWT.NONE);
		lblFromDisk.setText("From Disk:");
		lblFromDisk.setLayoutData(labelLayoutData);
		Label lblToDisk = new Label(container, SWT.NONE);
		lblToDisk.setText("To Disk:");
		lblToDisk.setLayoutData(labelLayoutData);

		Text txtFilterFrom = guiHelper.createFilterText(container);
		Text txtFilterTo = guiHelper.createFilterText(container);
		
		ITableLabelProvider diskLabelProvider = getDiskLabelProvider();

		GlusterDummyModel glusterDummyModel = GlusterDummyModel.getInstance();
		List<Disk> fromDisks = glusterDummyModel.getReadyDisksOfVolume(volume);		
		List<Disk> toDisks = glusterDummyModel.getReadyDisksOfAllServersExcluding(volume.getDisks());
		
		TableViewer tableViewerFrom = createTableViewer(container, diskLabelProvider, fromDisks, txtFilterFrom);
		if(fromDisk != null) {
			setFromDisk(tableViewerFrom, fromDisk);
		}
		
		createTableViewer(container, diskLabelProvider, toDisks, txtFilterTo);
	}
	
	private void setFromDisk(TableViewer tableViewer, Disk diskToSelect) {
		Table table = tableViewer.getTable();
		for(int i = 0 ; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			if(item.getData() == diskToSelect) {
				table.select(i);
				return;
			}
		}
	}
	
	private TableViewer createTableViewer(Composite container, ITableLabelProvider diskLabelProvider, List<Disk> fromDisks, Text txtFilterText) {
		Composite tableViewerComposite = createTableViewerComposite(container);
		
		TableViewer tableViewer = new TableViewer(tableViewerComposite, SWT.SINGLE);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(diskLabelProvider);
		
		setupDiskTable(tableViewerComposite, tableViewer.getTable());
		guiHelper.createFilter(tableViewer, txtFilterText, false);
		
		tableViewer.setInput(fromDisks.toArray());
		return tableViewer;
	}	
}
