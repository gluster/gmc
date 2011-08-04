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

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchSite;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.gui.EntityGroupContentProvider;
import com.gluster.storage.management.gui.GlusterServerTableLabelProvider;

public class GlusterServersPage extends AbstractTableViewerPage<GlusterServer> {
	private List<GlusterServer> glusterServers;

	public enum GLUSTER_SERVER_TABLE_COLUMN_INDICES {
		NAME, IP_ADDRESSES, NUM_OF_CPUS, TOTAL_MEMORY, TOTAL_FREE_SPACE, TOTAL_DISK_SPACE, STATUS // Removed PREFERRED_NETWORK 
	};

	private static final String[] GLUSTER_SERVER_TABLE_COLUMN_NAMES = new String[] { "Name", "IP Address(es)",
			"Number" + CoreConstants.NEWLINE + "of CPUs", "Total" + CoreConstants.NEWLINE + "Memory (GB)",
			"Free Space (GB)", "Total " + CoreConstants.NEWLINE + " Space (GB)", "Status" }; // Removed "Preferred\nNetwork",

	public GlusterServersPage(IWorkbenchSite site, final Composite parent, int style, final EntityGroup<GlusterServer> servers) {
		super(site, parent, style, true, true, servers);
		this.glusterServers = servers.getEntities();
	}

	@Override
	protected ClusterListener createClusterListener() {
		return new DefaultClusterListener() {
			
			@Override
			public void serverAdded(GlusterServer server) {
				tableViewer.add(server);
				parent.update();
			}
			
			@Override
			public void serverRemoved(GlusterServer server) {
				tableViewer.remove(server);
				parent.update();
			}
			
			@Override
			public void serverChanged(GlusterServer server, Event event) {
				tableViewer.update(server, null);
				parent.update();
			}
		};
	}

	@Override
	protected void setColumnProperties(Table table) {
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.NAME, SWT.CENTER, 100);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.STATUS, SWT.CENTER, 70);
		// setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.PREFERRED_NETWORK, SWT.CENTER, 90);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.NUM_OF_CPUS, SWT.CENTER, 90);
		//setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.CPU_USAGE, SWT.CENTER, 90);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_MEMORY, SWT.CENTER, 90);
		//setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.MEMORY_IN_USE, SWT.CENTER, 90);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_FREE_SPACE, SWT.CENTER, 90);
		setColumnProperties(table, GLUSTER_SERVER_TABLE_COLUMN_INDICES.TOTAL_DISK_SPACE, SWT.CENTER, 90);
		//setColumnProperties(table, SERVER_DISK_TABLE_COLUMN_INDICES.DISK_SPACE_IN_USE, SWT.CENTER, 90);
	}

	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new GlusterServerTableLabelProvider();
	}
	
	@Override
	protected IContentProvider getContentProvider() {
		return new EntityGroupContentProvider<GlusterServer>();
	}

	@Override
	protected String[] getColumnNames() {
		return GLUSTER_SERVER_TABLE_COLUMN_NAMES;
	}

	@Override
	protected List<GlusterServer> getAllEntities() {
		return glusterServers;
	}
	
	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 */
	public void setColumnProperties(Table table, GLUSTER_SERVER_TABLE_COLUMN_INDICES columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex.ordinal());
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}
}
