package com.gluster.storage.management.gui.views.details;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.progress.IProgressConstants;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IEntityListener;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.jobs.InitializeDiskJob;
import com.gluster.storage.management.gui.utils.GUIHelper;

public abstract class AbstractDisksPage extends Composite implements IEntityListener {
	protected final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	protected TableViewer tableViewer;
	private IWorkbenchSite site;
	protected static final GUIHelper guiHelper = GUIHelper.getInstance();

	/**
	 * Setup properties of the table e.g. column headers, widths, etc.
	 * 
	 * @param parent
	 *            The parent composite. (TableColumnLayout has to be set on this)
	 * @param table
	 *            The table to be set up
	 */
	protected abstract void setupDiskTable(Composite parent, Table table);

	/**
	 * @return The label provider to be used with the disk table viewer
	 */
	protected abstract ITableLabelProvider getTableLabelProvider();

	/**
	 * @return Index of the "status" column in the table. Return -1 if status column is not displayed
	 */
	protected abstract int getStatusColumnIndex();

	private void init(final Composite parent, IWorkbenchSite site, List<Disk> disks) {
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});

		this.site = site;

		setupPageLayout();
		Text filterText = guiHelper.createFilterText(toolkit, this);
		setupDiskTableViewer(createTableViewerComposite(), filterText);

		tableViewer.setInput(disks);
		setupStatusCellEditor(); // creates hyperlinks for "unitialized" disks

		site.setSelectionProvider(tableViewer);
		Application.getApplication().addEntityListener(this);

		parent.layout(); // Important - this actually paints the table

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

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

	public AbstractDisksPage(final Composite parent, int style, IWorkbenchSite site, List<Disk> disks) {
		super(parent, style);
		init(parent, site, disks);
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	private void createInitializeLink(final TableItem item, final int rowNum, final Disk disk) {
		final Table table = tableViewer.getTable();
		final TableEditor editor = new TableEditor(table);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.RIGHT;

		table.addPaintListener(new PaintListener() {
			private TableItem myItem = item;
			private int myRowNum = rowNum;
			private ImageHyperlink myLink = null;
			private TableEditor myEditor = null;

			private void createLinkFor(Disk disk1, TableItem item1, int rowNum1) {
				myItem = item1;
				myRowNum = rowNum1;

				myEditor = new TableEditor(table);
				myEditor.grabHorizontal = true;
				myEditor.horizontalAlignment = SWT.RIGHT;

				myLink = toolkit.createImageHyperlink(table, SWT.NONE);
				// link.setImage(guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED));
				myLink.setText("Initialize");
				myLink.addHyperlinkListener(new StatusLinkListener(myLink, myEditor, myItem, tableViewer, disk1, site));

				myEditor.setEditor(myLink, item1, getStatusColumnIndex());

				myItem.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						myLink.dispose();
						myEditor.dispose();
					}
				});
			}

			@Override
			public void paintControl(PaintEvent e) {
				int itemCount = table.getItemCount();

				// Find the table item corresponding to our disk
				Disk disk1 = null;
				int rowNum1 = -1;
				TableItem item1 = null;
				for (int i = 0; i < itemCount; i++) {
					item1 = table.getItem(i);
					disk1 = (Disk) item1.getData();
					if (disk1 != null && disk1 == disk) {
						rowNum1 = i;
						break;
					}
				}

				if (rowNum1 == -1) {
					// item disposed and disk not visible. nothing to do.
					return;
				}

				if (myEditor == null || myItem.isDisposed()) {
					// item visible, and
					// either editor never created, OR
					// old item disposed. create the link for it
					createLinkFor(disk1, item1, rowNum1);
				}

				if (rowNum1 != myRowNum) {
					// disk visible, but at a different row num. re-create the link
					myLink.dispose();
					myEditor.dispose();
					createLinkFor(disk1, item1, rowNum1);
				}

				myEditor.layout(); // IMPORTANT. Without this, the link location goes for a toss on maximize + restore
			}
		});
	}

	private void setupStatusCellEditor() {
		final TableViewer viewer = tableViewer;
		final Table table = viewer.getTable();
		for (int i = 0; i < table.getItemCount(); i++) {
			final TableItem item = table.getItem(i);
			if (item.isDisposed() || item.getData() == null) {
				continue;
			}
			final Disk disk = (Disk) item.getData();
			if (disk.isUninitialized()) {
				createInitializeLink(item, i, disk);
			}
		}
	}

	private Composite createTableViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return tableViewerComposite;
	}

	private TableViewer createDiskTableViewer(Composite parent) {
		tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);

		tableViewer.setLabelProvider(getTableLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		setupDiskTable(parent, tableViewer.getTable());

		return tableViewer;
	}

	private void setupDiskTableViewer(Composite parent, final Text filterText) {
		tableViewer = createDiskTableViewer(parent);
		// Create a case insensitive filter for the table viewer using the filter text field
		guiHelper.createFilter(tableViewer, filterText, false);
	}

	private final class StatusLinkListener extends HyperlinkAdapter {
		private final Disk disk;
		private final TableEditor myEditor;
		private final ImageHyperlink myLink;
		private final TableViewer viewer;
		private final IWorkbenchSite site;

		private StatusLinkListener(ImageHyperlink link, TableEditor editor, TableItem item, TableViewer viewer,
				Disk disk, IWorkbenchSite site) {
			this.disk = disk;
			this.viewer = viewer;
			this.myEditor = editor;
			this.myLink = link;
			this.site = site;
		}

		private void updateStatus(final DISK_STATUS status, final boolean disposeEditor) {
			if (disposeEditor) {
				myLink.dispose();
				myEditor.dispose();
			}
			disk.setStatus(status);
			viewer.update(disk, new String[] { "status" });
			Application.getApplication().entityChanged(disk, new String[] { "status" });
		}

		@Override
		public void linkActivated(HyperlinkEvent e) {
			updateStatus(DISK_STATUS.INITIALIZING, true);

			try {
				site.getWorkbenchWindow().getActivePage().showView(IProgressConstants.PROGRESS_VIEW_ID);
			} catch (PartInitException e1) {
				e1.printStackTrace();
				throw new GlusterRuntimeException("Could not open the progress view!", e1);
			}

			new InitializeDiskJob(disk).schedule();
		}
	}

	@Override
	public void entityChanged(final Entity entity, final String[] paremeters) {
		if (!(entity instanceof Disk)) {
			return;
		}
		final Disk disk = (Disk) entity;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				tableViewer.update(disk, paremeters);

				if (disk.isUninitialized()) {
					Table table = tableViewer.getTable();

					for (int rowNum = 0; rowNum < table.getItemCount(); rowNum++) {
						TableItem item = table.getItem(rowNum);
						if (item.getData() == disk) {
							createInitializeLink(item, rowNum, disk);
						}
					}
				}
			}
		});
	}
}
