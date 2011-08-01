/**
 * TasksPage.java
 *
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
 */
package com.gluster.storage.management.gui.views.pages;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.gui.TasksTableLabelProvider;
import com.gluster.storage.management.gui.toolbar.GlusterToolbarManager;

public class TasksPage extends AbstractTableViewerPage<TaskInfo> {
	private List<TaskInfo> taskInfoList;
	private TaskInfo selectedTask;
	
	public enum TASK_TABLE_COLUMN_INDICES {
		TASK, STATUS
	};
	
	private static final String[] TASK_TABLE_COLUMN_NAMES = new String[] { "Task", "Status"};
	

	public TasksPage(IWorkbenchSite site, Composite parent, int style, List<TaskInfo> taskInfo) {
		super(site, parent, style, false, false, taskInfo);
		this.taskInfoList = taskInfo;
	}
	
	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.views.pages.AbstractTableViewerPage#createClusterListener()
	 */
	@Override
	protected ClusterListener createClusterListener() {
		return new DefaultClusterListener() {
			@Override
			public void taskAdded(TaskInfo taskInfo) {
				tableViewer.add(taskInfo);
				parent.update();
			}
			
			@Override
			public void taskRemoved(TaskInfo taskInfo) {
				tableViewer.remove(taskInfo);
				parent.update();
				// hide the task related actionset as no task is selected
				// site.getPage().hideActionSet(IActionConstants.ACTION_SET_TASK);
			}
			
			@Override
			public void taskUpdated(TaskInfo taskInfo) {
				tableViewer.update(taskInfo, null);
				parent.update();
				// fire selection event so that toolbar gets updated
				// (the action class listens to selection and enables/disables automatically)
				tableViewer.setSelection(new StructuredSelection(taskInfo));
			}
		};
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.views.pages.AbstractTableViewerPage#getColumnNames()
	 */
	@Override
	protected String[] getColumnNames() {
		return TASK_TABLE_COLUMN_NAMES;
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.views.pages.AbstractTableViewerPage#setColumnProperties(org.eclipse.swt.widgets.Table)
	 */
	@Override
	protected void setColumnProperties(Table table) {
		guiHelper.setColumnProperties(table, TASK_TABLE_COLUMN_INDICES.TASK.ordinal(), SWT.LEFT, 50);
		guiHelper.setColumnProperties(table, TASK_TABLE_COLUMN_INDICES.STATUS.ordinal(), SWT.LEFT, 50);
	}
	
	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.views.pages.AbstractTableViewerPage#getLabelProvider()
	 */
	@Override
	protected IBaseLabelProvider getLabelProvider() {
		return new TasksTableLabelProvider();
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.views.pages.AbstractTableViewerPage#getContentProvider()
	 */
	@Override
	protected IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.views.pages.AbstractTableViewerPage#getAllEntities()
	 */
	@Override
	protected List<TaskInfo> getAllEntities() {
		return taskInfoList;
	}
	
	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.views.pages.AbstractTableViewerPage#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			Entity selectedEntity = (Entity) ((StructuredSelection) selection).getFirstElement();
			if (selectedEntity != null && selectedEntity instanceof TaskInfo && selectedEntity != selectedTask) {
				selectedTask = (TaskInfo)selectedEntity;
				new GlusterToolbarManager(part.getSite().getWorkbenchWindow()).updateToolbar(selectedTask);
			}
		}	
	}
}
