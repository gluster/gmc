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
import com.gluster.storage.management.gui.views.details.ServersPage;
import com.gluster.storage.management.gui.views.details.TabCreator;
import com.gluster.storage.management.gui.views.navigator.NavigationView;

public class EntityGroupServerTabCreator implements TabCreator, IDoubleClickListener {
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		createServersTab((EntityGroup<Server>) entity, tabFolder);
	}

	private void createServersTab(EntityGroup<Server> servers, TabFolder tabFolder) {
		Composite serversTab = guiHelper.createTab(tabFolder, "Discovered Servers", IImageKeys.SERVERS);
		ServersPage page = new ServersPage(serversTab, SWT.NONE, servers);
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
