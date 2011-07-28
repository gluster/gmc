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
package com.gluster.storage.management.gui.views.pages;

import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.layout.TableColumnLayout;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Event.EVENT_TYPE;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.VolumeOption;
import com.gluster.storage.management.core.model.VolumeOptionInfo;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.VolumeOptionsContentProvider;
import com.gluster.storage.management.gui.VolumeOptionsTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeOptionsPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private TableViewer tableViewer;
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private Volume volume;
	private DefaultClusterListener clusterListener;
	private Text filterText;
	private List<VolumeOptionInfo> defaultVolumeOptions = GlusterDataModelManager.getInstance()
			.getVolumeOptionsDefaults();

	public enum OPTIONS_TABLE_COLUMN_INDICES {
		OPTION_KEY, OPTION_VALUE
	};

	private static final String[] OPTIONS_TABLE_COLUMN_NAMES = new String[] { "Option Key", "Option Value" };
	private Button addTopButton;
	private Button addBottomButton;
	private TableViewerColumn keyColumn;
	private OptionKeyEditingSupport keyEditingSupport;

	public VolumeOptionsPage(final Composite parent, int style, Volume volume) {
		super(parent, style);

		this.volume = volume;

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		setupPageLayout();
		addTopButton = createAddButton();
		filterText = guiHelper.createFilterText(toolkit, this);

		setupOptionsTableViewer(filterText);

		addBottomButton = createAddButton();

		if (defaultVolumeOptions.size() == volume.getOptions().size()) {
			setAddButtonsEnabled(false);
		}

		tableViewer.setInput(volume.getOptions());

		parent.layout(); // Important - this actually paints the table
		registerListeners(parent);
	}

	private void setAddButtonsEnabled(boolean enable) {
		addTopButton.setEnabled(enable);
		addBottomButton.setEnabled(enable);
	}

	private Button createAddButton() {
		return toolkit.createButton(this, "&Add", SWT.FLAT);
	}
	
	private void registerListeners(final Composite parent) {
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

		clusterListener = new DefaultClusterListener() {
			@Override
			public void volumeChanged(Volume volume, Event event) {
				super.volumeChanged(volume, event);
				
				switch (event.getEventType()) {
				case VOLUME_OPTIONS_RESET:
					if (!tableViewer.getControl().isDisposed()) {
						//While reseting the options, clear the filter text before refreshing the tree
						filterText.setText("");  
						tableViewer.refresh();
						setAddButtonsEnabled(true);
					}
					break;

				case VOLUME_OPTION_SET:
					String key = (String)event.getEventData();
					if (isNewOption(volume, key)) {
						// option has been set successfully by the user. re-enable the add button and search filter
						// textbox
						setAddButtonsEnabled(true);
						filterText.setEnabled(true);
					}

					if (defaultVolumeOptions.size() == volume.getOptions().size()) {
						setAddButtonsEnabled(false);
					}

					tableViewer.refresh();
					break;
				case VOLUME_CHANGED:
					tableViewer.refresh();
					if(volume.getOptions().size() == defaultVolumeOptions.size()) {
						setAddButtonsEnabled(false);
					} else {
						setAddButtonsEnabled(true);
					}
				default:
					break;
				}
			}

			private boolean isNewOption(Volume volume, String optionKey) {
				if (filterText.getText().length() > 0) {
					// user has been filtering the contents. adding new option is allowed only when contents are NOT
					// filtered. Thus it's impossible that this is a newly added option
					return false;
				}

				// if this is the last option in the volume options, it must be the new option
				return optionKey.equals(volume.getOptions().getOptions().get(volume.getOptions().size() - 1).getKey());
			}
		};
		
		SelectionListener addButtonSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// add an empty option to be filled up by user
				volume.setOption("", "");

				tableViewer.refresh();
				tableViewer.setSelection(new StructuredSelection(getEntry("")));
				keyColumn.getViewer().editElement(getEntry(""), 0); // edit newly created entry

				// disable the add button AND search filter textbox till user fills up the new option
				setAddButtonsEnabled(false);
				filterText.setEnabled(false);
			}

			private VolumeOption getEntry(String key) {
				for (VolumeOption entry : volume.getOptions().getOptions()) {
					if (entry.getKey().equals(key)) {
						return entry;
					}
				}
				return null;
			}
		};
		addTopButton.addSelectionListener(addButtonSelectionListener);
		addBottomButton.addSelectionListener(addButtonSelectionListener);

		// Make sure that add button is enabled only when search filter textbox is empty
		filterText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (filterText.getText().length() > 0) {
					setAddButtonsEnabled(false);
				} else {
					if (defaultVolumeOptions.size() == volume.getOptions().size()) {
						setAddButtonsEnabled(false);
					} else {
						setAddButtonsEnabled(true);
					}
				}
			}
		});
		
		GlusterDataModelManager.getInstance().addClusterListener(clusterListener);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();

				if (!(addTopButton.isEnabled() || addBottomButton.isEnabled())) {
					// user has selected key, but not added value. Since this is not a valid entry,
					// remove the last option (without value) from the volume
					volume.getOptions().remove(keyEditingSupport.getEntryBeingAdded().getKey());
				}

				GlusterDataModelManager.getInstance().removeClusterListener(clusterListener);
			}
		});
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	private void setupOptionsTable(Composite parent) {
		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

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
			@Override
			public String getText(Object element) {
				return ((VolumeOption) element).getValue();
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
			@Override
			public String getText(Object element) {
				return ((VolumeOption) element).getKey();
			}

			@Override
			public String getToolTipText(Object element) {
				String key = ((VolumeOption) element).getKey();
				if (key.isEmpty()) {
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
		tableViewer.setContentProvider(new VolumeOptionsContentProvider());
		tableViewer.getTable().setLinesVisible(true);

		setupOptionsTable(parent);
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 2;
		tableViewerComposite.setLayoutData(layoutData);
		
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
