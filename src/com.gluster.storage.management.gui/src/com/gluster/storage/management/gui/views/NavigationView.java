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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.toolbar.GlusterToolbarManager;
import com.gluster.storage.management.gui.views.navigator.ClusterAdapterFactory;

public class NavigationView extends ViewPart implements ISelectionListener {
	public static final String ID = NavigationView.class.getName();
	private GlusterDataModel model;
	private TreeViewer treeViewer;
	private IAdapterFactory adapterFactory = new ClusterAdapterFactory();
	private GlusterToolbarManager toolbarManager;
	private Entity entity;
	private GlusterViewsManager viewsManager;

	@Override
	public void createPartControl(Composite parent) {
		createNavigationTree(parent);

		// Create the views and toolbar managers
		toolbarManager = new GlusterToolbarManager(getSite().getWorkbenchWindow());
		viewsManager = new GlusterViewsManager(getSite().getPage());

		// listen to selection events to update views/toolbar accordingly
		getSite().getPage().addSelectionListener(this);
	}

	private void createNavigationTree(Composite parent) {
		model = GlusterDataModelManager.getInstance().getModel();

		Platform.getAdapterManager().registerAdapters(adapterFactory, Entity.class);
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		treeViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		treeViewer.setContentProvider(new BaseWorkbenchContentProvider());
		treeViewer.setInput(model);
		treeViewer.expandAll();
		// select the first element by default
		treeViewer.setSelection(new StructuredSelection(model.getChildren().get(0)));

		setupContextMenu();

		// register as selection provider so that other views can listen to any selection events on the tree
		getSite().setSelectionProvider(treeViewer);

		// Refresh the navigation tree whenever there is a change to the data model
		GlusterDataModelManager.getInstance().addClusterListener(new DefaultClusterListener() {
			public void clusterChanged() {
				treeViewer.refresh();
			}
			
			@Override
			public void volumeChanged(Volume volume, Event event) {
				super.volumeChanged(volume, event);
				selectEntity(volume); // this makes sure that the toolbar buttons get updated according to new status
			}
		});
	}

	private void setupContextMenu() {
		MenuManager menuManager = new MenuManager("&Gluster", "gluster.context.menu");
		Menu contextMenu = menuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getTree().setMenu(contextMenu);
		getSite().registerContextMenu(menuManager, treeViewer);
	}

	public void selectEntity(Entity entity) {
		treeViewer.setSelection(new StructuredSelection(entity));
		treeViewer.reveal(entity);
		setFocus(); // this ensures that the "selection changed" event gets fired
	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof NavigationView && selection instanceof TreeSelection) {
			Entity selectedEntity = (Entity) ((TreeSelection) selection).getFirstElement();

			if (selectedEntity != null && selectedEntity != entity) {
				entity = selectedEntity;

				// update views and toolbar buttons visibility based on selected entity
				viewsManager.updateViews(entity);
				toolbarManager.updateToolbar(entity);

				// Opening of other views may cause navigation tree to lose focus; get it back.
				setFocus();
			}
		}
	}
}
