package com.gluster.storage.management.gui.views.details;

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
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.toolbar.ToolbarManager;
import com.gluster.storage.management.gui.views.navigator.NavigationView;

/**
 * This view is displayed on the right hand side of the platform UI. It updates itself with appropriate tabs
 * whenever selection changes on the navigation view (cluster tree) on the left hand side of the UI.
 */
public class DetailsView extends ViewPart implements ISelectionListener {
	public static final String ID = "com.gluster.storage.management.gui.views.details";
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
