/**
 * DiscoveredServersView.java
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
package com.gluster.storage.management.gui.views;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.ServersPage;
import com.gluster.storage.management.gui.views.navigator.NavigationView;

/**
 *
 */
public class DiscoveredServersView extends ViewPart implements IDoubleClickListener, ISelectionListener {
	public static final String ID = DiscoveredServersView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private EntityGroup<Server> servers;
	private ServersPage page;

	public DiscoveredServersView() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (servers == null) {
			ISelection selection = getSite().getPage().getSelection();
			if (selection instanceof TreeSelection) {
				Entity selectedEntity = (Entity) ((TreeSelection) selection).getFirstElement();
				if (selectedEntity instanceof EntityGroup) {
					EntityGroup group = (EntityGroup) selectedEntity;
					if (group.getEntityType() == Server.class) {
						servers = group;
					}
				}
			}
		}
		page = new ServersPage(parent, SWT.NONE, servers);

		page.addDoubleClickListener(this);
		getSite().getPage().addSelectionListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		page.setFocus();
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		NavigationView clusterView = (NavigationView) guiHelper.getView(NavigationView.ID);
		if (clusterView != null) {
			clusterView.selectEntity((Entity) ((StructuredSelection) event.getSelection()).getFirstElement());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof NavigationView && selection instanceof TreeSelection) {
			Entity selectedEntity = (Entity) ((TreeSelection) selection).getFirstElement();

			if (servers == selectedEntity || selectedEntity == null || !(selectedEntity instanceof EntityGroup) || ((EntityGroup) selectedEntity).getEntityType() != Server.class) {
				// entity selection has not changed. do nothing.
				return;
			}

			servers = (EntityGroup<Server>) selectedEntity;
			page.setInput(servers);
		}
	}
}
