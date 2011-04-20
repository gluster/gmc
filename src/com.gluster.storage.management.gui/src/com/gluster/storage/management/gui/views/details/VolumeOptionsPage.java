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

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Event.EVENT_TYPE;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.VolumeOptionInfo;
import com.gluster.storage.management.gui.VolumeOptionsTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeOptionsPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private TableViewer tableViewer;
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private Volume volume;
	private DefaultClusterListener clusterListener;

	public enum OPTIONS_TABLE_COLUMN_INDICES {
		OPTION_KEY, OPTION_VALUE
	};

	private static final String[] OPTIONS_TABLE_COLUMN_NAMES = new String[] { "Option Key", "Option Value" };
	private Button addButton;
	private TableViewerColumn keyColumn;
	private OptionKeyEditingSupport keyEditingSupport;

	public VolumeOptionsPage(final Composite parent, int style, Volume volume) {
		super(parent, style);
		
		this.volume = volume;

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		setupPageLayout();
		setupOptionsTableViewer(guiHelper.createFilterText(toolkit, this));
		
		createAddButton();

		tableViewer.setInput(volume.getOptions().entrySet());
		
		parent.layout(); // Important - this actually paints the table
		registerListeners(parent);
	}

	private void createAddButton() {
		addButton = toolkit.createButton(this, "&Add", SWT.FLAT);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// add an empty option to be filled up by user
				volume.setOption("", "");
				
				tableViewer.refresh();
				tableViewer.setSelection(new StructuredSelection(getEntry("")));
				keyColumn.getViewer().editElement(getEntry(""), 0); // edit newly created entry				
				
				// disable the add button till user fills up the new option
				addButton.setEnabled(false);
			}

			private Entry<String, String> getEntry(String key) {
				for(Entry<String, String> entry : volume.getOptions().entrySet()) {
					if(entry.getKey().equals(key)) {
						return entry;
					}
				}
				return null;
			}
		});
	}

	private void registerListeners(final Composite parent) {
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if(!addButton.isEnabled()) {
					// user has selected key, but not added value. Since this is not a valid entry,
					// remove the last option (without value) from the volume
					volume.getOptions().remove(keyEditingSupport.getEntryBeingAdded().getKey());
				}

				GlusterDataModelManager.getInstance().removeClusterListener(clusterListener);
				toolkit.dispose();
			}
		});

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
		
		parent.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if(!addButton.isEnabled()) {
					// user has selected key, but not added value. Since this is not a valid entry,
					// remove the last option (without value) from the volume
					Entry<String, String> entryBeingAdded = keyEditingSupport.getEntryBeingAdded();
					volume.getOptions().remove(entryBeingAdded.getKey());
				}
			}
		});

		
		clusterListener = new DefaultClusterListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void volumeChanged(Volume volume, Event event) {
				super.volumeChanged(volume, event);
				if(event.getEventType() == EVENT_TYPE.VOLUME_OPTIONS_RESET) {
					if(!tableViewer.getControl().isDisposed()) {
						tableViewer.refresh();
					}
				}
				
				if(event.getEventType() == EVENT_TYPE.VOLUME_OPTION_SET) {
					Entry<String, String> eventEntry = (Entry<String, String>)event.getEventData();
					if (eventEntry.getKey().equals(volume.getOptions().keySet().toArray()[volume.getOptions().size()-1])) {
						// option has been set successfully by the user. re-enable the add button
						addButton.setEnabled(true);
					}
					
					if(tableViewer.getTable().getItemCount() < volume.getOptions().size()) {
						// new volume set from outside this page. refresh the viewer.
						tableViewer.refresh();
					} else {
						// existing volume option value changed. update that element.
						tableViewer.update(eventEntry, null);
					}
				}
			}
		};
		GlusterDataModelManager.getInstance().addClusterListener(clusterListener);
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	private void setupOptionsTable(Composite parent) {
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
	
	private TableColumn createValueColumn() {
		TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		valueColumn.getColumn()
				.setText(OPTIONS_TABLE_COLUMN_NAMES[OPTIONS_TABLE_COLUMN_INDICES.OPTION_VALUE.ordinal()]);
		valueColumn.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return ((Entry<String, String>) element).getValue();
			}
		});
		
		// User can edit value of a volume option
		valueColumn.setEditingSupport(new OptionValueEditingSupport(valueColumn.getViewer(), volume));
		
		return valueColumn.getColumn();
	}

	private TableColumn createKeyColumn() {
		keyColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		keyColumn.getColumn().setText(OPTIONS_TABLE_COLUMN_NAMES[OPTIONS_TABLE_COLUMN_INDICES.OPTION_KEY.ordinal()]);
		keyColumn.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return ((Entry<String, String>) element).getKey();
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public String getToolTipText(Object element) {
				String key = ((Entry<String, String>) element).getKey();
				if(key.isEmpty()) {
					return "Click to select a volume option key";
				}
				
				VolumeOptionInfo optionInfo = GlusterDataModelManager.getInstance().getVolumeOptionInfo(key);
				// Wrap the description before adding to tooltip so that long descriptions are displayed properly 
				return WordUtils.wrap(optionInfo.getDescription(), 60) + CoreConstants.NEWLINE + "Default value: "
						+ optionInfo.getDefaultValue();
			}
		});
		
		// Editing support required when adding new key
		keyEditingSupport = new OptionKeyEditingSupport(keyColumn.getViewer(), volume);
		keyColumn.setEditingSupport(keyEditingSupport);
		
		return keyColumn.getColumn();
	}

	private void createOptionsTableViewer(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.SINGLE);
		tableViewer.setLabelProvider(new VolumeOptionsTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.getTable().setLinesVisible(true);

		setupOptionsTable(parent);
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	private void setupOptionsTableViewer(final Text filterText) {
		Composite tableViewerComposite = createTableViewerComposite();
		createOptionsTableViewer(tableViewerComposite);
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		// Create a case insensitive filter for the table viewer using the filter text field
		guiHelper.createFilter(tableViewer, filterText, false);
	}

	private void setColumnProperties(Table table, OPTIONS_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}
}
