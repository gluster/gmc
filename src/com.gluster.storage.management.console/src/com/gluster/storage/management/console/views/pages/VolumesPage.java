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
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchSite;

import com.gluster.storage.management.console.EntityGroupContentProvider;
import com.gluster.storage.management.console.VolumeTableLabelProvider;
import com.gluster.storage.management.console.toolbar.GlusterToolbarManager;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Volume;

public class VolumesPage extends AbstractTableViewerPage<Volume> {
	private List<Volume> volumes;
	
	final GlusterToolbarManager toolbarManager = new GlusterToolbarManager( site.getWorkbenchWindow());
	
	public enum VOLUME_TABLE_COLUMN_INDICES {
		NAME, VOLUME_TYPE, NUM_OF_BRICKS, TRANSPORT_TYPE, VOLUME_STATUS
	};

	private static final String[] VOLUME_TABLE_COLUMN_NAMES = new String[] { "Name", "Volume Type",
			"Number of" + CoreConstants.NEWLINE + "Bricks", "Transport Type", "Status" };

	public VolumesPage(final Composite parent, IWorkbenchSite site, EntityGroup<Volume> volumes) {
		super(site, parent, SWT.NONE, true, true, volumes);
		this.volumes = volumes.getEntities();
	}

	@Override
	protected String[] getColumnNames() {
		return VOLUME_TABLE_COLUMN_NAMES;
	}
	
	@Override
	protected void setColumnProperties(Table table) {
		setColumnProperties(table, VOLUME_TABLE_COLUMN_INDICES.VOLUME_STATUS, SWT.CENTER, 50);
		setColumnProperties(table, VOLUME_TABLE_COLUMN_INDICES.NUM_OF_BRICKS, SWT.CENTER, 50);
		setColumnProperties(table, VOLUME_TABLE_COLUMN_INDICES.TRANSPORT_TYPE, SWT.CENTER, 70);
	}
	
	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new VolumeTableLabelProvider();
	}
	
	@Override
	protected IContentProvider getContentProvider() {
		return new EntityGroupContentProvider<Volume>();
	}
	
	@Override
	protected ClusterListener createClusterListener() {
		return new DefaultClusterListener() {
			@Override
			public void volumeCreated(Volume volume) {
				tableViewer.add(volume);
				parent.update();
				toolbarManager.updateToolbar(volume);
			}
			
			@Override
			public void volumeDeleted(Volume volume) {
				tableViewer.remove(volume);
				parent.update();
				toolbarManager.updateToolbar(volume);
			}
			
			@Override
			public void volumeChanged(Volume volume, Event event) {
				tableViewer.update(volume, null);
				parent.update();
				toolbarManager.updateToolbar(volume);
			}
		};
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
	
	@Override
	protected List<Volume> getAllEntities() {
		return volumes;
	}

	@Override
	protected ViewerComparator createViewerComparator() {
		// TODO Auto-generated method stub
		return null;
	}
}
