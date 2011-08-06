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
package com.gluster.storage.management.console.views.pages;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchSite;

import com.gluster.storage.management.console.EntityGroupContentProvider;
import com.gluster.storage.management.console.ServerTableLabelProvider;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Server;

public class ServersPage extends AbstractTableViewerPage<Server> {
	private List<Server> servers;
	
	public enum SERVER_TABLE_COLUMN_INDICES {
		NAME, IP_ADDRESSES, NUM_OF_DISKS, TOTAL_DISK_SPACE
	};

	private static final String[] SERVER_TABLE_COLUMN_NAMES = new String[] { "Name", "IP Address(es)", "Number of Disks", "Total Disk Space (GB)" };

	public ServersPage(final Composite parent, IWorkbenchSite site, EntityGroup<Server> serversGroup) {
		super(site, parent, SWT.NONE, true, true, serversGroup);
		this.servers = serversGroup.getEntities();
	}

	@Override
	protected ClusterListener createClusterListener() {
		return new DefaultClusterListener() {
			@Override
			public void discoveredServerRemoved(Server server) {
				tableViewer.remove(server);
				parent.update();
			}
			
			@Override
			public void discoveredServerAdded(Server server) {
				tableViewer.add(server);
				parent.update();
			}
			
			@Override
			public void discoveredServerChanged(Server server, Event event) {
				tableViewer.update(server, null);
				parent.update();
			}
		};
	}
	
	public void setInput(EntityGroup<Server> servers) {
		tableViewer.setInput(servers);
		tableViewer.refresh();		
	}

	@Override
	protected void setColumnProperties(Table table) {
		setColumnProperties(table, SERVER_TABLE_COLUMN_INDICES.NAME, SWT.CENTER, 70);
		setColumnProperties(table, SERVER_TABLE_COLUMN_INDICES.IP_ADDRESSES, SWT.CENTER, 100);
		setColumnProperties(table, SERVER_TABLE_COLUMN_INDICES.NUM_OF_DISKS, SWT.CENTER, 70);
		setColumnProperties(table, SERVER_TABLE_COLUMN_INDICES.TOTAL_DISK_SPACE, SWT.CENTER, 70);
		// setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.NUM_OF_CPUS, SWT.CENTER, 90);
		// setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.CPU_USAGE, SWT.CENTER, 90);
		// setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.TOTAL_MEMORY, SWT.CENTER, 90);
		// setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.MEMORY_IN_USE, SWT.CENTER, 90);
		// setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.DISK_SPACE_IN_USE, SWT.CENTER, 90);
	}
	
	@Override
	protected String[] getColumnNames() {
		return SERVER_TABLE_COLUMN_NAMES;
	}
	
	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new ServerTableLabelProvider();
	}
	
	@Override
	protected IContentProvider getContentProvider() {
		return new EntityGroupContentProvider<Server>();
	}
	
	@Override
	protected List<Server> getAllEntities() {
		return servers;
	}	

	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 */
	private void setColumnProperties(Table table, SERVER_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}
}
