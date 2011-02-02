package com.gluster.storage.management.gui.views.details.tabcreators;

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

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.TabCreator;
import com.gluster.storage.management.gui.views.details.VolumesPage;
import com.gluster.storage.management.gui.views.navigator.NavigationView;

public class EntityGroupVolumeTabCreator implements TabCreator, IDoubleClickListener {
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		createSummaryTab((EntityGroup<Volume>) entity, tabFolder, toolkit);
		createVolumesTab((EntityGroup<Volume>) entity, tabFolder, toolkit);
	}

	private int getVolumeCountByStatus(EntityGroup<Volume> volumes, VOLUME_STATUS status) {
		int count = 0;
		for (Volume volume : (List<Volume>) volumes.getEntities()) {
			if (volume.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createStatusChart(FormToolkit toolkit, Composite section, Double[] values) {
		String[] categories = new String[] { "Online", "Offline" };
		PieChartViewerComposite chartViewerComposite = new PieChartViewerComposite(section, SWT.NONE, categories,
				values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 250;
		data.heightHint = 250;
		chartViewerComposite.setLayoutData(data);
	}

	private void createAlertsSection(final EntityGroup<Volume> volumes, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Alerts", null, 2, false);

		toolkit.createLabel(section, "Any alerts related to volumes\nwill be displayed here.");
	}

	private void createRunningTasksSection(final EntityGroup<Volume> volumes, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Running Tasks", null, 2, false);

		toolkit.createLabel(section, "List of running tasks related to\nvolumes will be displayed here.");
	}

	private void createSummarySection(final EntityGroup<Volume> volumes, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Availability", null, 2, false);

		Double[] values = new Double[] { Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.ONLINE)),
				Double.valueOf(getVolumeCountByStatus(volumes, VOLUME_STATUS.OFFLINE)) };
		createStatusChart(toolkit, section, values);
	}

	private void createSummaryTab(final EntityGroup<Volume> volumes, TabFolder tabFolder, FormToolkit toolkit) {
		Composite summaryTab = guiHelper.createTab(tabFolder, "Summary", IImageKeys.VOLUMES);
		final ScrolledForm form = guiHelper.setupForm(summaryTab, toolkit, "Volumes - Summary");
		createSummarySection(volumes, toolkit, form);
		createRunningTasksSection(volumes, toolkit, form);
		createAlertsSection(volumes, toolkit, form);
		
		summaryTab.layout(); // IMP: lays out the form properly
	}

	private void createVolumesTab(EntityGroup<Volume> volumes, TabFolder tabFolder, FormToolkit toolkit) {
		Composite volumesTab = guiHelper.createTab(tabFolder, "Volumes", IImageKeys.VOLUMES);
		VolumesPage page = new VolumesPage(volumesTab, SWT.NONE, volumes);
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
