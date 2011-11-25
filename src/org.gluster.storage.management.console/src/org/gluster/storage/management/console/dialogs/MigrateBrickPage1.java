/**
 * MigrateBrickPage1.java
 *
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
 */

package org.gluster.storage.management.console.dialogs;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.TableLabelProviderAdapter;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Device;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.utils.NumberUtil;


public class MigrateBrickPage1 extends WizardPage {
	private static final String PAGE_NAME = "migrate.disk.page.1";

	private enum DISK_TABLE_COLUMN_INDICES {
		SERVER, BRICK_DIRECTORY, FREE_SPACE, TOTAL_SPACE
	}

	private static final String[] DISK_TABLE_COLUMN_NAMES = { "Server", "Brick Directory", "Free Space (GB)", "Total Space (GB)" };

	private Volume volume;
	private Brick fromBrick;
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	private TableViewer tableViewerTo;

	private TableViewer tableViewerFrom;

	private Button autoCompleteCheckbox;

	private ITableLabelProvider getBrickLabelProvider(final String volumeName) {
		return new TableLabelProviderAdapter() {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (!(element instanceof Brick)) {
					return null;
				}
				Brick brick = (Brick) element;
				GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
				Device device = modelManager.getDeviceForBrickDir(brick);
				// convert MB to GB
				String freeSpace = (device == null ? "NA" : NumberUtil.formatNumber(device.getFreeSpace() / 1024));
				String totalSpace = (device == null ? "NA" : NumberUtil.formatNumber(device.getSpace() / 1024));
				return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? brick.getServerName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.BRICK_DIRECTORY.ordinal() ? brick.getBrickDirectory()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal() ? freeSpace
						: columnIndex == DISK_TABLE_COLUMN_INDICES.TOTAL_SPACE.ordinal() ? totalSpace 
						: "Invalid");
			}
		};
	}
	
	private ITableLabelProvider getDiskLabelProvider(final String volumeName) {
		return new TableLabelProviderAdapter() {
			
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (!(element instanceof Device)) {
					return null;
				}
				Device device = (Device) element;
				return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? device.getServerName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.BRICK_DIRECTORY.ordinal() ? device.getMountPoint() + "/" + volumeName
						: columnIndex == DISK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal() ? NumberUtil.formatNumber(device.getFreeSpace() / 1024 )  /* Coverted to GB */
						: columnIndex == DISK_TABLE_COLUMN_INDICES.TOTAL_SPACE.ordinal() ? NumberUtil.formatNumber(device.getSpace() / 1024) : "Invalid");
			}
		};
	}

	private void setupDiskTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, DISK_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SERVER, SWT.CENTER, 100);
		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.BRICK_DIRECTORY, SWT.CENTER, 100);
		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.FREE_SPACE, SWT.CENTER, 90);
		setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.TOTAL_SPACE, SWT.CENTER, 90);
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
	public MigrateBrickPage1(Volume volume, Brick brick) {
		super(PAGE_NAME);
		this.volume = volume;
		this.fromBrick = brick;
		setTitle("Migrate Brick [" + volume.getName() + "]");
		setPageDescription(fromBrick.getQualifiedName(), null);
		setPageComplete(false);
	}

	private void setPageDescription(String source, String target) {
		if (source == null) {
			source = "";
		}
		if (target == null) {
			target = "";
		}
		setDescription("Migrate data from \"" + source + "\" to \"" + target + "\"");
	}

	private Object getSelectedItem(TableViewer tableViewer) {
		TableItem[] selectedItems = tableViewer.getTable().getSelection();
		Object selectedDevice = null;
		for (TableItem item : selectedItems) {
			selectedDevice = item.getData();
		}
		return selectedDevice;
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

	public String getSourceBrickDir() {
		Brick sourceBrick = (Brick)getSelectedItem(tableViewerFrom); 
		return sourceBrick.getQualifiedName();
	}

	public String getTargetBrickDir() {
		Device targetDevice = (Device)getSelectedItem(tableViewerTo);
		if (targetDevice == null) {
			return "";
		}
		return targetDevice.getQualifiedBrickName(volume.getName());
	}
	
	public Boolean getAutoCommitSelection() {
		return autoCompleteCheckbox.getSelection();
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
		lblFromDisk.setText("From Brick:");
		lblFromDisk.setLayoutData(labelLayoutData);
		Label lblToDisk = new Label(container, SWT.NONE);
		lblToDisk.setText("To Brick:");
		lblToDisk.setLayoutData(labelLayoutData);

		Text txtFilterFrom = guiHelper.createFilterText(container);
		Text txtFilterTo = guiHelper.createFilterText(container);
		
		GlusterDataModelManager glusterDataModelManager = GlusterDataModelManager.getInstance();
		List<Brick> fromBricks = volume.getBricks();
		List<Device> toDevices = glusterDataModelManager.getReadyDevicesOfAllServersExcluding(glusterDataModelManager
				.getDevicesOfVolume(volume));
		
		tableViewerFrom = createTableViewer(container, getBrickLabelProvider(volume.getName()), fromBricks,
				txtFilterFrom);
		
		if(fromBrick != null) {
			setFromDisk(tableViewerFrom, fromBrick);
		}
		tableViewerTo = createTableViewer(container, getDiskLabelProvider(volume.getName()), toDevices, txtFilterTo);
		tableViewerTo.setSelection(new StructuredSelection(fromBrick));
		
		// Auto commit selection field
		Composite autoCommitContainer = new Composite(container, SWT.NONE);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		autoCommitContainer.setLayoutData(data);
		autoCompleteCheckbox = new Button(autoCommitContainer, SWT.CHECK);
		autoCompleteCheckbox.setSelection(true);
		Label lblAutoComplete = new Label(autoCommitContainer, SWT.NONE);
		lblAutoComplete.setText("Auto commit on migration complete");
		autoCommitContainer.setLayout( container.getLayout());
	}

	private void setFromDisk(TableViewer tableViewer, Brick brickToSelect) {
		Table table = tableViewer.getTable();
		for (int i = 0; i < table.getItemCount(); i++) {
			TableItem item = table.getItem(i);
			if (item.getData() == brickToSelect) {
				table.select(i);
				return;
			}
		}
	}
	
	private void refreshButtonStatus() {
		if(tableViewerFrom.getSelection().isEmpty() || tableViewerTo.getSelection().isEmpty()) {
			setPageComplete(false);
		} else {
			setPageComplete(true);
		}
	}

	private <T> TableViewer createTableViewer(Composite container, ITableLabelProvider diskLabelProvider,
			List<T> bricks, Text txtFilterText) {
		Composite tableViewerComposite = createTableViewerComposite(container);

		TableViewer tableViewer = new TableViewer(tableViewerComposite, SWT.SINGLE);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(diskLabelProvider);

		setupDiskTable(tableViewerComposite, tableViewer.getTable());
		guiHelper.createFilter(tableViewer, txtFilterText, false);

		tableViewer.setInput(bricks.toArray());
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				refreshButtonStatus();
				setPageDescription(getSourceBrickDir(), getTargetBrickDir());
			}
		});
		return tableViewer;
	}

}
