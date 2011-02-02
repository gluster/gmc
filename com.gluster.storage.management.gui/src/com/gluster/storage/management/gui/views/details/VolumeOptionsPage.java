package com.gluster.storage.management.gui.views.details;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.VolumeOptionsTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeOptionsPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private TableViewer tableViewer;
	private GUIHelper guiHelper = GUIHelper.getInstance();

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

	private void setupDiskTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, OPTIONS_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table, OPTIONS_TABLE_COLUMN_INDICES.OPTION_KEY, SWT.CENTER, 100);
		setColumnProperties(table, OPTIONS_TABLE_COLUMN_INDICES.OPTION_VALUE, SWT.CENTER, 100);
	}

	private TableViewer createDiskTableViewer(Composite parent) {
		TableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		// TableViewer tableViewer = new TableViewer(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setLabelProvider(new VolumeOptionsTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		setupDiskTable(parent, tableViewer.getTable());

		return tableViewer;
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	private void setupDiskTableViewer(final Text filterText) {
		Composite tableViewerComposite = createTableViewerComposite();
		tableViewer = createDiskTableViewer(tableViewerComposite);
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
