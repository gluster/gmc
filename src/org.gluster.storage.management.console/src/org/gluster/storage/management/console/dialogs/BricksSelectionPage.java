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
package org.gluster.storage.management.console.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.gluster.storage.management.console.IImageKeys;
import org.gluster.storage.management.console.TableLabelProviderAdapter;
import org.gluster.storage.management.console.utils.EntityViewerFilter;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import org.gluster.storage.management.core.model.Device;
import org.gluster.storage.management.core.utils.NumberUtil;

import com.richclientgui.toolbox.duallists.CustomTableDualListComposite;
import com.richclientgui.toolbox.duallists.DualListComposite.ListContentChangedListener;
import com.richclientgui.toolbox.duallists.IRemovableContentProvider;
import com.richclientgui.toolbox.duallists.RemovableContentProvider;
import com.richclientgui.toolbox.duallists.TableColumnData;

public class BricksSelectionPage extends Composite {
	private enum DISK_TABLE_COLUMN_INDICES {
		SERVER, BRICK_DIRECTORY, FREE_SPACE, TOTAL_SPACE
	}

	private static final String[] DISK_TABLE_COLUMNS_NAMES = { "Server", "Brick Directory", "Free Space (GB)",
			"Total Space (GB)" };

	private GUIHelper guiHelper = GUIHelper.getInstance();
	private CustomTableDualListComposite<Device> dualTableViewer;
	private Text filterText;
	// This list keeps track of the order of the disks as user changes the same by clicking on up/down arrow buttons
	private List<Device> chosenDevices = new ArrayList<Device>();

	private IRemovableContentProvider<Device> chosenBricksContentProvider;

	private Button btnUp;

	private Button btnDown;

	public BricksSelectionPage(final Composite parent, int style, List<Device> allDevices, List<Device> selectedDevices,
			String volumeName) {
		super(parent, style);

		createPage(allDevices, selectedDevices, volumeName);

		parent.layout();
	}

	public void addDiskSelectionListener(ListContentChangedListener<Device> listener) {
		dualTableViewer.addChosenListChangedSelectionListener(listener);
	}

	private TableLabelProviderAdapter getDiskLabelProvider(final String volumeName) {
		return new TableLabelProviderAdapter() {

			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (!(element instanceof Device)) {
					return null;
				}

				Device device = (Device) element;
				return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? device.getServerName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.BRICK_DIRECTORY.ordinal() ? device.getMountPoint()
								+ "/" + volumeName
								: columnIndex == DISK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal() ? NumberUtil
										.formatNumber((device.getFreeSpace() / 1024))
										: columnIndex == DISK_TABLE_COLUMN_INDICES.TOTAL_SPACE.ordinal() ? NumberUtil
												.formatNumber((device.getSpace() / 1024)) : "Invalid");
			}
		};
	}

	private void createPage(List<Device> allDevice, List<Device> selectedDevice, String volumeName) {
		setupPageLayout();

		filterText = guiHelper.createFilterText(this);
		new Label(this, SWT.NONE);

		createDualTableViewer(allDevice, selectedDevice, volumeName);
		createFilter(filterText, false); // attach filter text to the dual table viewer for auto-filtering

		Composite buttonContainer = new Composite(this, SWT.NONE);
		buttonContainer.setLayout(new GridLayout(1, false));
		GridData buttonContainerData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		buttonContainerData.minimumWidth = 40;
		buttonContainer.setLayoutData(buttonContainerData);

		btnUp = new Button(buttonContainer, SWT.PUSH);
		GridData btnUpData = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
		btnUpData.minimumWidth = 30;
		btnUp.setLayoutData(btnUpData);
		btnUp.setImage(guiHelper.getImage(IImageKeys.ARROW_UP_16x16));
		btnUp.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				chosenDevices = getChosenDevices();
				List<Device> selectedDevices = getSelectedChosenDevices();

				chosenBricksContentProvider.removeElements(chosenDevices);
				for (Device device : selectedDevices) {
					int index = chosenDevices.indexOf(device);
					Device deviceAbove = chosenDevices.get(index - 1);
					chosenDevices.set(index - 1, device);
					chosenDevices.set(index, deviceAbove);
				}
				chosenBricksContentProvider.addElements(chosenDevices);
				dualTableViewer.refreshChosenViewer();
				updateButtons();
			}
		});

		btnDown = new Button(buttonContainer, SWT.PUSH);
		GridData btnDownData = new GridData(SWT.LEFT, SWT.TOP, true, false);
		btnDownData.minimumWidth = 30;
		btnDown.setLayoutData(btnDownData);
		btnDown.setImage(guiHelper.getImage(IImageKeys.ARROW_DOWN_16x16));
		btnDown.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				chosenDevices = getChosenDevices();
				List<Device> selectedDevices = getSelectedChosenDevices();

				chosenBricksContentProvider.removeElements(chosenDevices);
				for (Device disk : selectedDevices) {
					int index = chosenDevices.indexOf(disk);
					Device deviceBelow = chosenDevices.get(index + 1);
					chosenDevices.set(index + 1, disk);
					chosenDevices.set(index, deviceBelow);
				}
				chosenBricksContentProvider.addElements(chosenDevices);
				dualTableViewer.refreshChosenViewer();
				updateButtons();

			}
		});
	}

	private List<Device> getSelectedChosenDevices() {
		TableItem[] selectedItems = dualTableViewer.getChosenTable().getSelection();
		List<Device> selectedDevices = new ArrayList<Device>();
		for (TableItem item : selectedItems) {
			selectedDevices.add((Device) item.getData());
		}
		return selectedDevices;
	}

	private void createFilter(final Text filterText, boolean caseSensitive) {
		final String initialFilterString = filterText.getText();

		final EntityViewerFilter filter = new EntityViewerFilter(initialFilterString, caseSensitive);
		// On every keystroke inside the text field, update the filter string
		filterText.addKeyListener(new KeyAdapter() {
			private String filterString = initialFilterString;

			@Override
			public void keyReleased(KeyEvent e) {
				String enteredString = filterText.getText();
				if (enteredString.equals(filterString)) {
					// Filter string has not changed. don't do anything
					return;
				}

				// Update filter string
				filterString = enteredString;
				filter.setFilterString(filterString);

				// Refresh viewer with newly filtered content
				dualTableViewer.refreshAvailableViewer();
				dualTableViewer.refreshChosenViewer();
			}
		});

		dualTableViewer.setAvailableViewerFilter(filter);
		dualTableViewer.setChosenViewerFilter(filter);
	}

	private void createDualTableViewer(List<Device> allDevices, List<Device> selectedDevices, String volumeName) {
		TableColumnData[] columnData = createColumnData();
		ITableLabelProvider diskLabelProvider = getDiskLabelProvider(volumeName);

		dualTableViewer = new CustomTableDualListComposite<Device>(this, SWT.NONE, columnData, columnData);

		dualTableViewer.setViewerLabels("Available:", "Selected:");

		dualTableViewer.setAvailableTableLinesVisible(false);
		dualTableViewer.setAvailableTableHeaderVisible(true);
		dualTableViewer.setAvailableContentProvider(new RemovableContentProvider<Device>(getAvailableDevice(allDevices,
				selectedDevices)));
		dualTableViewer.setAvailableLabelProvider(diskLabelProvider);

		dualTableViewer.setChosenTableLinesVisible(true);
		dualTableViewer.setChosenTableHeaderVisible(true);

		chosenBricksContentProvider = new RemovableContentProvider<Device>(selectedDevices);
		dualTableViewer.setChosenContentProvider(chosenBricksContentProvider);
		dualTableViewer.setChosenLabelProvider(diskLabelProvider);

		dualTableViewer.getChosenTable().addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
	}

	private void updateButtons() {
		btnUp.setEnabled(true);
		btnDown.setEnabled(true);
		List<Device> selectedChosenDevices = getSelectedChosenDevices();
		List<Device> chosenDevices = getChosenDevices();
		for (Device device : selectedChosenDevices) {
			int index = chosenDevices.indexOf(device);
			if (index == 0) {
				btnUp.setEnabled(false);
			}
			if (index == chosenDevices.size() - 1) {
				btnDown.setEnabled(false);
			}
		}
	}

	/**
	 * @param allDevices
	 * @param selectedDevices
	 * @return
	 */
	private List<Device> getAvailableDevice(List<Device> allDevices, List<Device> selectedDevices) {
		List<Device> availableDevices = new ArrayList<Device>();
		for (Device device : allDevices) {
			if (!selectedDevices.contains(device)) {
				availableDevices.add(device);
			}
		}
		return availableDevices;
	}

	private TableColumnData[] createColumnData() {
		DISK_TABLE_COLUMN_INDICES[] columns = DISK_TABLE_COLUMN_INDICES.values();
		TableColumnData[] columnData = new TableColumnData[columns.length];
		int columnNum;
		for (DISK_TABLE_COLUMN_INDICES column : columns) {
			columnNum = column.ordinal();
			columnData[columnNum] = new TableColumnData(columnNum, DISK_TABLE_COLUMNS_NAMES[columnNum], 100);
		}
		return columnData;
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public List<Device> getChosenDevices() {
		Object[] devicesArr = chosenBricksContentProvider.getElements(dualTableViewer);
		if (devicesArr != null) {
			List<Device> devices = new ArrayList<Device>();
			for (Object device : devicesArr) {
				devices.add((Device) device);
			}
			return devices;
		}
		return null;
	}

	public Set<Brick> getChosenBricks(String volumeName) {
		Object[] bricksArr = chosenBricksContentProvider.getElements(dualTableViewer);

		if (bricksArr != null) {
			Set<Brick> bricks = new HashSet<Brick>();
			for (Object device : bricksArr) {
				bricks.add(new Brick(((Device) device).getServerName(), BRICK_STATUS.ONLINE, ((Device) device)
						.getMountPoint() + "/" + volumeName)); // Assumption mount point is not having trailing "/" 
			}
			return bricks;
		}
		return null;
	}

}
