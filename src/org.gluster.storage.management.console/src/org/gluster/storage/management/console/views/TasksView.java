/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.views.pages.TasksPage;
import org.gluster.storage.management.core.model.TaskInfo;


public class TasksView extends ViewPart {
	
	public static final String ID = TasksView.class.getName();
	private TasksPage page;
	

	public TasksView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		page = new TasksPage(getSite(), parent, SWT.NONE, getAllTasks());
		page.layout(); // IMP: lays out the form properly
	}

	
	private List<TaskInfo> getAllTasks() {
		return GlusterDataModelManager.getInstance().getModel().getCluster().getTaskInfoList();
	}

	@Override
	public void setFocus() {
		page.setFocus();
	}

}
