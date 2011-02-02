package com.gluster.storage.management.gui.views.details.tabcreators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.DisksPage;
import com.gluster.storage.management.gui.views.details.GlusterServersPage;
import com.gluster.storage.management.gui.views.details.TabCreator;
import com.gluster.storage.management.gui.views.navigator.NavigationView;

public class EntityGroupGlusterServerTabCreator implements TabCreator, IDoubleClickListener {
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	private int getServerCountByStatus(EntityGroup<GlusterServer> servers, SERVER_STATUS status) {
		int count = 0;
		for (GlusterServer server : (List<GlusterServer>)servers.getEntities()) {
			if (server.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createAlertsSection(final EntityGroup<GlusterServer> servers, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Alerts", null, 2, false);

		toolkit.createLabel(section, "Any alerts related to servers\nwill be displayed here.");
	}

	private void createRunningTasksSection(final EntityGroup<GlusterServer> servers, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Running Tasks", null, 2, false);

		toolkit.createLabel(section, "List of running tasks related to\nservers will be displayed here.");
	}

	@Override
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		createSummaryTab((EntityGroup<GlusterServer>)entity, tabFolder, toolkit);
		createServersTab((EntityGroup<GlusterServer>)entity, tabFolder, toolkit);
		createDisksTab((EntityGroup<GlusterServer>)entity, tabFolder, toolkit, site);
	}

	private void createServersTab(EntityGroup<GlusterServer> servers, TabFolder tabFolder, FormToolkit toolkit) {
		Composite serversTab = guiHelper.createTab(tabFolder, "Servers", IImageKeys.SERVERS);
		GlusterServersPage page = new GlusterServersPage(serversTab, SWT.NONE, servers);
		page.addDoubleClickListener(this);
	}

	private void createSummaryTab(EntityGroup<GlusterServer> servers, TabFolder tabFolder, FormToolkit toolkit) {
		Composite summaryTab = guiHelper.createTab(tabFolder, "Summary", IImageKeys.SERVERS);
		final ScrolledForm form = guiHelper.setupForm(summaryTab, toolkit, "Servers - Summary");
		createSummarySection(servers, toolkit, form);
		createRunningTasksSection(servers, toolkit, form);
		createAlertsSection(servers, toolkit, form);
		
		summaryTab.layout(); // IMP: lays out the form properly
	}

	private void createSummarySection(final EntityGroup<GlusterServer> servers, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Availability", null, 2, false);

		Double[] values = new Double[] { Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.ONLINE)),
				Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.OFFLINE)) };
		createStatusChart(toolkit, section, values);
	}

	private void createStatusChart(FormToolkit toolkit, Composite section, Double[] values) {
		String[] categories = new String[] { "Online", "Offline" };
		PieChartViewerComposite chartViewerComposite = new PieChartViewerComposite(section, SWT.NONE, categories, values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 250;
		data.heightHint = 250;
		chartViewerComposite.setLayoutData(data);	
	}


	private void createDisksTab(EntityGroup<GlusterServer> servers, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		Composite disksTab = guiHelper.createTab(tabFolder, "Disks", IImageKeys.SERVERS);
		DisksPage page = new DisksPage(disksTab, SWT.NONE, site, getAllDisks(servers));
		
		disksTab.layout(); // IMP: lays out the form properly
	}

	private List<Disk> getAllDisks(EntityGroup<GlusterServer> servers) {
		List<Disk> disks = new ArrayList<Disk>();
		for(GlusterServer server : (List<GlusterServer>)servers.getEntities()) {
			disks.addAll(server.getDisks());
		}
		return disks;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		NavigationView clusterView = (NavigationView) guiHelper.getView(NavigationView.ID);
		if (clusterView != null) {
			clusterView.selectEntity((Entity) ((StructuredSelection) event.getSelection()).getFirstElement());
		}
	}
}
