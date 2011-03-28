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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.TabCreator;

public class ClusterTabCreator implements TabCreator {
	private GUIHelper guiHelper = GUIHelper.getInstance();

	private int getVolumeCountByStatus(Cluster cluster, VOLUME_STATUS status) {
		int count = 0;
		for (Volume volume : cluster.getVolumes()) {
			if (volume.getStatus() == status) {
				count++;
			}
		}
		return count;
	}
	
	private int getServerCountByStatus(Cluster cluster, SERVER_STATUS status) {
		int count = 0;
		for (GlusterServer server : cluster.getServers()) {
			if (server.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createVolumesSection(Cluster cluster, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Volumes", null, 1, false);

		// toolkit.createLabel(sectionClient, "Number of Volumes: ", SWT.NONE);
		// toolkit.createLabel(sectionClient, "12", SWT.NONE);
		//
		// toolkit.createLabel(sectionClient, "Online: ", SWT.NONE);
		// Label labelOnlineCount = toolkit.createLabel(sectionClient, "9", SWT.NONE);
		// labelOnlineCount.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
		//
		// toolkit.createLabel(sectionClient, "Offline: ", SWT.NONE);
		// Label lblOfflineCount = toolkit.createLabel(sectionClient, "3", SWT.NONE);
		// lblOfflineCount.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));

		Double[] values = new Double[] { Double.valueOf(getVolumeCountByStatus(cluster, VOLUME_STATUS.ONLINE)),
				Double.valueOf(getVolumeCountByStatus(cluster, VOLUME_STATUS.OFFLINE)) };
		createStatusChart(toolkit, section, values);
	}

	private void createServersSection(Cluster cluster, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Servers", null, 1, false);

//		toolkit.createLabel(sectionClient, "Number of Servers: ", SWT.NONE);
//		toolkit.createLabel(sectionClient, "7", SWT.NONE);
//
//		toolkit.createLabel(sectionClient, "Online: ", SWT.NONE);
//		Label labelOnlineCount = toolkit.createLabel(sectionClient, "6", SWT.NONE);
//		labelOnlineCount.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
//
//		toolkit.createLabel(sectionClient, "Offline: ", SWT.NONE);
//		Label lblOfflineCount = toolkit.createLabel(sectionClient, "1", SWT.NONE);
//		lblOfflineCount.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));

		Double[] values = new Double[] { Double.valueOf(getServerCountByStatus(cluster, SERVER_STATUS.ONLINE)),
				Double.valueOf(getServerCountByStatus(cluster, SERVER_STATUS.OFFLINE)) };

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

	private void createActionsSection(final Cluster cluster, final FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Actions", null, 1, false);

		ImageHyperlink imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Create Volume");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.CREATE_VOLUME_BIG));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			// TODO: Override appropriate method and handle hyperlink event
		});

		imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Add Server(s)");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.ADD_SERVER_BIG));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			// TODO: Override appropriate method and handle hyperlink event
		});
	}

	private void createClusterSummaryTab(final Cluster cluster, final TabFolder tabFolder, final FormToolkit toolkit) {
		Composite summaryTab = guiHelper.createTab(tabFolder, cluster.getName(), IImageKeys.CLUSTER);

		final ScrolledForm form = guiHelper.setupForm(summaryTab, toolkit, "Cluster Summary");
		createVolumesSection(cluster, toolkit, form);
		createServersSection(cluster, toolkit, form);
		createActionsSection(cluster, toolkit, form);

		summaryTab.layout(); // IMP: lays out the form properly
	}

	@Override
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		createClusterSummaryTab((Cluster) entity, tabFolder, toolkit);
	}
}
