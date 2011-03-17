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
package com.gluster.storage.management.gui.views.details.tabcreators;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.TabCreator;
import com.gluster.storage.management.gui.views.navigator.NavigationView;
import com.gluster.storage.management.gui.views.pages.ServersPage;

public class EntityGroupServerTabCreator implements TabCreator, IDoubleClickListener {
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		createServersTab((EntityGroup<Server>) entity, tabFolder);
	}

	private void createServersTab(EntityGroup<Server> servers, TabFolder tabFolder) {
		Composite serversTab = guiHelper.createTab(tabFolder, "Discovered Servers", IImageKeys.SERVERS);
		ServersPage page = new ServersPage(serversTab, null, servers);
		page.addDoubleClickListener(this);
	}
	
	@Override
	public void doubleClick(DoubleClickEvent event) {
		NavigationView clusterView = (NavigationView) guiHelper.getView(NavigationView.ID);
		if (clusterView != null) {
			clusterView.selectEntity((Entity) ((StructuredSelection) event.getSelection()).getFirstElement());
		}
	}
}
