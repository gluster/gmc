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

import java.util.ArrayList;
import java.util.List;

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

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.TableLabelProviderAdapter;
import com.gluster.storage.management.gui.utils.EntityViewerFilter;
import com.gluster.storage.management.gui.utils.GUIHelper;
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
	private CustomTableDualListComposite<Disk> dualTableViewer;
	private Text filterText;
	// This list keeps track of the order of the disks as user changes the same by clicking on up/down arrow buttons
	private List<Disk> chosenDisks = new ArrayList<Disk>();

	private IRemovableContentProvider<Disk> chosenBricksContentProvider;

	private Button btnUp;

	private Button btnDown;

	public BricksSelectionPage(final Composite parent, int style, List<Disk> allDisks, List<Disk> selectedDisks,
			String volumeName) {
		super(parent, style);

		createPage(allDisks, selectedDisks, volumeName);

		parent.layout();
	}

	public void addDiskSelectionListener(ListContentChangedListener<Disk> listener) {
		dualTableViewer.addChosenListChangedSelectionListener(listener);
	}

	private TableLabelProviderAdapter getDiskLabelProvider(final String volumeName) {
		return new TableLabelProviderAdapter() {

			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (!(element instanceof Disk)) {
					return null;
				}

				Disk disk = (Disk) element;
				return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? disk.getServerName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.BRICK_DIRECTORY.ordinal() ? disk.getMountPoint()
								+ "/" + volumeName
								: columnIndex == DISK_TABLE_COLUMN_INDICES.FREE_SPACE.ordinal() ? NumberUtil
										.formatNumber((disk.getFreeSpace() / 1024))
										: columnIndex == DISK_TABLE_COLUMN_INDICES.TOTAL_SPACE.ordinal() ? NumberUtil
												.formatNumber((disk.getSpace() / 1024)) : "Invalid");
			}
		};
	}

	private int indexOf(List<Disk> disks, Disk searchDisk) {
		for (Disk disk : disks) {
			if (disk.getQualifiedName().equals(searchDisk.getQualifiedName())) {
				return disks.indexOf(disk);
			}
		}
		return -1;
	}

	private void createPage(List<Disk> allDisks, List<Disk> selectedDisks, String volumeName) {
		setupPageLayout();

		filterText = guiHelper.createFilterText(this);
		new Label(this, SWT.NONE);

		createDualTableViewer(allDisks, selectedDisks, volumeName);
		createFilter(filterText, false); // attach filter text to the dual table viewer for auto-filtering

		Composite buttonContainer = new Composite(this, SWT.NONE);
		buttonContainer.setLayout(new GridLayout(1, false));
		GridData buttonContainerData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		buttonContainerData.minimumWidth = 40;
		buttonContainer.setLayoutData(buttonContainerData);

		btnUp = new Button(buttonContainer, SWT.TOGGLE);
		GridData btnUpData = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
		btnUpData.minimumWidth = 30;
		btnUp.setLayoutData(btnUpData);
		btnUp.setImage(guiHelper.getImage(IImageKeys.ARROW_UP));
		btnUp.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				chosenDisks = getChosenDisks();
				List<Disk> selectedDisks = getSelectedChosenDisks();

				chosenBricksContentProvider.removeElements(chosenDisks);
				for (Disk disk : selectedDisks) {
					int index = chosenDisks.indexOf(disk);
					Disk diskAbove = chosenDisks.get(index - 1);
					chosenDisks.set(index - 1, disk);
					chosenDisks.set(index, diskAbove);
				}
				chosenBricksContentProvider.addElements(chosenDisks);
				dualTableViewer.refreshChosenViewer();
				updateButtons();
			}
		});

		btnDown = new Button(buttonContainer, SWT.TOGGLE);
		btnDown.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		btnDown.setImage(guiHelper.getImage(IImageKeys.ARROW_DOWN));
		btnDown.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				chosenDisks = getChosenDisks();
				List<Disk> selectedDisks = getSelectedChosenDisks();

				chosenBricksContentProvider.removeElements(chosenDisks);
				for (Disk disk : selectedDisks) {
					int index = chosenDisks.indexOf(disk);
					Disk diskBelow = chosenDisks.get(index + 1);
					chosenDisks.set(index + 1, disk);
					chosenDisks.set(index, diskBelow);
				}
				chosenBricksContentProvider.addElements(chosenDisks);
				dualTableViewer.refreshChosenViewer();
				updateButtons();

			}
		});
	}

	private List<Disk> getSelectedChosenDisks() {
		TableItem[] selectedItems = dualTableViewer.getChosenTable().getSelection();
		List<Disk> selectedDisks = new ArrayList<Disk>();
		for (TableItem item : selectedItems) {
			selectedDisks.add((Disk) item.getData());
		}
		return selectedDisks;
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

	private void createDualTableViewer(List<Disk> allDisks, List<Disk> selectedDisks, String volumeName) {
		TableColumnData[] columnData = createColumnData();
		ITableLabelProvider diskLabelProvider = getDiskLabelProvider(volumeName);

		dualTableViewer = new CustomTableDualListComposite<Disk>(this, SWT.NONE, columnData, columnData);

		dualTableViewer.setViewerLabels("Available:", "Selected:");

		dualTableViewer.setAvailableTableLinesVisible(false);
		dualTableViewer.setAvailableTableHeaderVisible(true);
		dualTableViewer.setAvailableContentProvider(new RemovableContentProvider<Disk>(getAvailableDisks(allDisks,
				selectedDisks)));
		dualTableViewer.setAvailableLabelProvider(diskLabelProvider);

		dualTableViewer.setChosenTableLinesVisible(true);
		dualTableViewer.setChosenTableHeaderVisible(true);

		chosenBricksContentProvider = new RemovableContentProvider<Disk>(selectedDisks);
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
		List<Disk> selectedChosenDisks = getSelectedChosenDisks();
		List<Disk> chosenDisks = getChosenDisks();
		for (Disk disk : selectedChosenDisks) {
			int index = chosenDisks.indexOf(disk);
			if (index == 0) {
				btnUp.setEnabled(false);
			}
			if (index == chosenDisks.size() - 1) {
				btnDown.setEnabled(false);
			}
		}
	}

	/**
	 * @param allDisks
	 * @param selectedDisks
	 * @return
	 */
	private List<Disk> getAvailableDisks(List<Disk> allDisks, List<Disk> selectedDisks) {
		List<Disk> availableDisks = new ArrayList<Disk>();
		for (Disk disk : allDisks) {
			if (!selectedDisks.contains(disk)) {
				availableDisks.add(disk);
			}
		}
		return availableDisks;
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

	public List<Disk> getChosenDisks() {
		Object[] disksArr = (Object[]) chosenBricksContentProvider.getElements(dualTableViewer);
		if (disksArr != null) {
			List<Disk> disks = new ArrayList<Disk>();
			for (Object disk : disksArr) {
				disks.add((Disk) disk);
			}
			return disks;
		}
		return null;
	}

	public List<Brick> getChosenBricks(String volumeName) {
		Object[] bricksArr = (Object[]) chosenBricksContentProvider.getElements(dualTableViewer);

		if (bricksArr != null) {
			List<Brick> bricks = new ArrayList<Brick>();
			for (Object disk : bricksArr) {
				bricks.add(new Brick(((Disk) disk).getServerName(), BRICK_STATUS.ONLINE, ((Disk) disk).getName(),
						((Disk) disk).getMountPoint() + "/" + volumeName)); // Assumption mount point is not having
																			// trailing "/"
			}
			return bricks;
		}
		return null;
	}

}
