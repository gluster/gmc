package com.gluster.storage.management.gui.views.navigator;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.GlusterDummyModel;

public class NavigationView extends ViewPart {
	public static final String ID = "com.gluster.storage.management.gui.views.navigator";
	private GlusterDataModel model;
	private TreeViewer treeViewer;
	private IAdapterFactory adapterFactory = new ClusterAdapterFactory();

	public NavigationView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		model = GlusterDummyModel.getInstance().getModel();
		
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		getSite().setSelectionProvider(treeViewer);
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
}
