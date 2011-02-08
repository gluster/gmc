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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.TableLabelProviderAdapter;
import com.gluster.storage.management.gui.utils.EntityViewerFilter;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.richclientgui.toolbox.duallists.CustomTableDualListComposite;
import com.richclientgui.toolbox.duallists.RemovableContentProvider;
import com.richclientgui.toolbox.duallists.TableColumnData;

public class CreateVolumeDisksPage extends Composite {
	private enum DISK_TABLE_COLUMN_INDICES {
		SERVER, DISK, SPACE, SPACE_USED
	}

	private static final String[] DISK_TABLE_COLUMNS_NAMES = { "Server", "Disk", "Space (GB)", "Used Space (GB)" };

	private GUIHelper guiHelper = GUIHelper.getInstance();
	private CustomTableDualListComposite<Disk> dualTableViewer;
	private Text filterText;
	
	public CreateVolumeDisksPage(final Composite parent, int style, List<Disk> disks) {
		super(parent, style);

		createPage(disks);
		
		parent.layout();
	}

	private TableLabelProviderAdapter getDiskLabelProvider() {
		return new TableLabelProviderAdapter() {

			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (!(element instanceof Disk)) {
					return null;
				}

				Disk disk = (Disk) element;
				return (columnIndex == DISK_TABLE_COLUMN_INDICES.SERVER.ordinal() ? disk.getServer().getName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.DISK.ordinal() ? disk.getName()
						: columnIndex == DISK_TABLE_COLUMN_INDICES.SPACE.ordinal() ? NumberUtil.formatNumber(disk.getSpace())
						: columnIndex == DISK_TABLE_COLUMN_INDICES.SPACE_USED.ordinal() ? NumberUtil.formatNumber(disk.getSpaceInUse()) 
						: "Invalid");
			}
		};
	}

	private void createPage(List<Disk> disks) {
		setupPageLayout();
		
		filterText = guiHelper.createFilterText(this);
		new Label(this, SWT.NONE);
		
		createDualTableViewer(disks);		
		createFilter(filterText, false); // attach filter text to the dual table viewer for auto-filtering
		
		Composite buttonContainer = new Composite(this, SWT.NONE);
		buttonContainer.setLayout(new GridLayout(1, false));
		GridData buttonContainerData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		buttonContainerData.minimumWidth = 40;
		buttonContainer.setLayoutData(buttonContainerData);
		
		Button btnUp = new Button(buttonContainer, SWT.TOGGLE);
		GridData btnUpData = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
		btnUpData.minimumWidth = 30;
		btnUp.setLayoutData(btnUpData);
		btnUp.setImage(guiHelper.getImage(IImageKeys.ARROW_UP));
		
		Button btnDown = new Button(buttonContainer, SWT.TOGGLE);
		btnDown.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		btnDown.setImage(guiHelper.getImage(IImageKeys.ARROW_DOWN));
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

	private void createDualTableViewer(List<Disk> disks) {
		TableColumnData[] columnData = createColumnData();
		ITableLabelProvider diskLabelProvider = getDiskLabelProvider();		

		dualTableViewer = new CustomTableDualListComposite<Disk>(this, SWT.NONE,
				columnData, columnData);		

		dualTableViewer.setViewerLabels("Available:", "Chosen:");

		dualTableViewer.setAvailableTableLinesVisible(false);
		dualTableViewer.setAvailableTableHeaderVisible(true);
		dualTableViewer.setAvailableContentProvider(new RemovableContentProvider<Disk>());
		dualTableViewer.setAvailableLabelProvider(diskLabelProvider);
		
		dualTableViewer.setChosenTableLinesVisible(true);
		dualTableViewer.setChosenTableHeaderVisible(true);
		dualTableViewer.setChosenContentProvider(new RemovableContentProvider<Disk>(disks));
		dualTableViewer.setChosenLabelProvider(diskLabelProvider);
	}

	private TableColumnData[] createColumnData() {
		DISK_TABLE_COLUMN_INDICES[] columns = DISK_TABLE_COLUMN_INDICES.values();
		TableColumnData[] columnData = new TableColumnData[columns.length];

		for (DISK_TABLE_COLUMN_INDICES column : columns) {
			int columnNum = column.ordinal();
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
}
