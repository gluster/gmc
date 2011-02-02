package com.gluster.storage.management.gui.views.details;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IDoubleClickListener;
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

import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.EntityGroupContentProvider;
import com.gluster.storage.management.gui.VolumeTableLabelProvider;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumesPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private TableViewer tableViewer;
	private GUIHelper guiHelper = GUIHelper.getInstance();

	public enum VOLUME_TABLE_COLUMN_INDICES {
		NAME, VOLUME_TYPE, NUM_OF_DISKS, TRANSPORT_TYPE, VOLUME_STATUS
	};
	
	private static final String[] VOLUME_TABLE_COLUMN_NAMES = new String[] { "Name",
			"Volume Type", "Number of\nDisks", "Transport Type", "Status" };

	public VolumesPage(Composite parent, int style) {
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
		setupVolumeTableViewer(filterText);
	}

	public VolumesPage(final Composite parent, int style, EntityGroup<Volume> volumes) {
		this(parent, style);

		tableViewer.setInput(volumes);
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
	
	public void addDoubleClickListener(IDoubleClickListener listener) {
		tableViewer.addDoubleClickListener(listener);
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	private void setupVolumeTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableColumnLayout columnLayout = guiHelper.createTableColumnLayout(table, VOLUME_TABLE_COLUMN_NAMES);
		parent.setLayout(columnLayout);

		setColumnProperties(table, VOLUME_TABLE_COLUMN_INDICES.VOLUME_STATUS, SWT.CENTER, 50);
		setColumnProperties(table, VOLUME_TABLE_COLUMN_INDICES.NUM_OF_DISKS, SWT.CENTER, 50);
		setColumnProperties(table, VOLUME_TABLE_COLUMN_INDICES.TRANSPORT_TYPE, SWT.CENTER, 70);
	}
	
	private TableViewer createVolumeTableViewer(Composite parent) {
		TableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		tableViewer.setLabelProvider(new VolumeTableLabelProvider());
		tableViewer.setContentProvider(new EntityGroupContentProvider<Volume>());

		setupVolumeTable(parent, tableViewer.getTable());

		return tableViewer;
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	private void setupVolumeTableViewer(final Text filterText) {
		Composite tableViewerComposite = createTableViewerComposite();
		tableViewer = createVolumeTableViewer(tableViewerComposite);
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
	public void setColumnProperties(Table table, VOLUME_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}
}
