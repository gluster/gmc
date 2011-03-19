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
package com.gluster.storage.management.gui.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.gui.toolbar.ToolbarManager;
import com.gluster.storage.management.gui.views.details.TabCreatorFactory;
import com.gluster.storage.management.gui.views.details.TabCreatorFactoryImpl;

/**
 * This view is displayed on the right hand side of the platform UI. It updates itself with appropriate tabs
 * whenever selection changes on the navigation view (cluster tree) on the left hand side of the UI.
 */
public class DetailsView extends ViewPart implements ISelectionListener {
	public static final String ID = DetailsView.class.getName();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private TabFolder tabFolder;
	private Entity entity;
	private TabCreatorFactory tabCreatorFactory = new TabCreatorFactoryImpl();
	private ToolbarManager toolbarManager;
	private IWorkbenchPartSite site;

	public DetailsView() {
		super();
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});

		tabFolder = new TabFolder(parent, SWT.TOP);

		// listen to selection event on the navigation tree view
		IWorkbenchWindow window = getViewSite().getWorkbenchWindow();
		window.getSelectionService().addSelectionListener(this);
		
		// Create the toolbar manager
		toolbarManager = new ToolbarManager(window);
		site = getSite();
	}

	@Override
	public void setFocus() {
		tabFolder.setFocus();
	}

	private void removeAllTabs() {
		for (TabItem item : tabFolder.getItems()) {
			item.getControl().dispose();
			item.dispose();
		}
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof NavigationView && selection instanceof TreeSelection) {
			Entity selectedEntity = (Entity) ((TreeSelection) selection).getFirstElement();
			
			if (entity == selectedEntity || selectedEntity == null) {
				// entity selection has not changed. do nothing.
				return;
			}

			entity = selectedEntity;
			removeAllTabs();

			// Create tabs for newly selected entity
			tabCreatorFactory.getTabCreator(entity).createTabs(entity, tabFolder, toolkit, site);
			
			// update toolbar buttons visibility based on selected entity
			toolbarManager.updateToolbar(entity);
		}
	}
}
