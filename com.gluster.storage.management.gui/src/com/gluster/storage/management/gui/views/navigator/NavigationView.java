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
package com.gluster.storage.management.gui.views.navigator;

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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.toolbar.ToolbarManager;
import com.gluster.storage.management.gui.views.ClusterSummaryView;
import com.gluster.storage.management.gui.views.DiscoveredServerView;
import com.gluster.storage.management.gui.views.DiscoveredServersView;
import com.gluster.storage.management.gui.views.VolumesSummaryView;
import com.gluster.storage.management.gui.views.VolumesView;

public class NavigationView extends ViewPart implements ISelectionListener {
	public static final String ID = NavigationView.class.getName();
	private GlusterDataModel model;
	private TreeViewer treeViewer;
	private IAdapterFactory adapterFactory = new ClusterAdapterFactory();
	private ToolbarManager toolbarManager;
	private Entity entity;

	public NavigationView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		model = GlusterDataModelManager.getInstance().getModel();

		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		Platform.getAdapterManager().registerAdapters(adapterFactory, Entity.class);
		treeViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		treeViewer.setContentProvider(new BaseWorkbenchContentProvider());
		treeViewer.setInput(model);
		treeViewer.expandAll();
		// select the first element by default
		treeViewer.setSelection(new StructuredSelection(model.getChildren().get(0)));

		MenuManager menuManager = new MenuManager("&Gluster", "gluster.context.menu");
		Menu contextMenu = menuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getTree().setMenu(contextMenu);

		getSite().registerContextMenu(menuManager, treeViewer);

		GlusterDataModelManager.getInstance().addClusterListener(new DefaultClusterListener() {
			@Override
			public void serverAdded(GlusterServer server) {
				treeViewer.refresh();
			}
		});

		getSite().setSelectionProvider(treeViewer);
		getSite().getPage().addSelectionListener(this);
		// Create the toolbar manager
		toolbarManager = new ToolbarManager(getSite().getWorkbenchWindow());
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

			if (entity == selectedEntity || selectedEntity == null) {
				// entity selection has not changed. do nothing.
				return;
			}

			IViewReference[] viewReferences = getSite().getPage().getViewReferences();
			for (final IViewReference viewReference : viewReferences) {
				if (!viewReference.getId().equals(ID)) {
					getSite().getPage().hideView(viewReference);
				}
			}

			entity = selectedEntity;
			try {
				if (entity instanceof EntityGroup) {
					if ((((EntityGroup) entity).getEntityType()) == Server.class) {
						getSite().getPage().showView(DiscoveredServersView.ID);
					} else if ((((EntityGroup) entity).getEntityType()) == Volume.class) {
						IViewPart summaryView = getSite().getPage().showView(VolumesSummaryView.ID);
						getSite().getPage().showView(VolumesView.ID);
						getSite().getPage().bringToTop(summaryView);
					}
				} else if (entity.getClass() == Server.class) {
					getSite().getPage().showView(DiscoveredServerView.ID);
				} else if (entity instanceof Cluster) {
					try {
						getSite().getPage().showView(ClusterSummaryView.ID);
					} catch (RuntimeException e) {
						// happens when navigation view is opening for the first time. just ignore it!
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			
			// update toolbar buttons visibility based on selected entity
			toolbarManager.updateToolbar(entity);
		}
	}
}
