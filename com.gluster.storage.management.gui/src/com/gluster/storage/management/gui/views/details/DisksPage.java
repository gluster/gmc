package com.gluster.storage.management.gui.views.details;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchSite;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.gui.DiskTableLabelProvider;
import com.gluster.storage.management.gui.utils.DiskViewerEditingSupport;

public class DisksPage extends AbstractDisksPage {

	public enum DISK_TABLE_COLUMN_INDICES {
		SERVER, DISK, SPACE, SPACE_IN_USE, STATUS
	};

	private static final String[] DISK_TABLE_COLUMN_NAMES = new String[] { "Server", "Disk", "Space (GB)",
			"Space in Use (GB)", "Status" };

	public DisksPage(final Composite parent, int style, IWorkbenchSite site, List<Disk> disks) {
		super(parent, style, site, disks);
	}

	@Override
	protected void setupDiskTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, DISK_TABLE_COLUMN_NAMES);
		parent.setLayout(tableColumnLayout);

		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SERVER.ordinal(), SWT.CENTER, 100);
		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.DISK.ordinal(), SWT.CENTER, 100);
		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SPACE.ordinal(), SWT.CENTER, 90);
		guiHelper.setColumnProperties(table, DISK_TABLE_COLUMN_INDICES.SPACE_IN_USE.ordinal(), SWT.CENTER, 90);
	}

	@Override
	protected ITableLabelProvider getTableLabelProvider() {
		return new DiskTableLabelProvider();
	}

	@Override
	protected int getStatusColumnIndex() {
		return DISK_TABLE_COLUMN_INDICES.STATUS.ordinal();
	}
}