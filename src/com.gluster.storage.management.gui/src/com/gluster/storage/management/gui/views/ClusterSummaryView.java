/**
 * DiscoveredServerView.java
 *
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
 */
package com.gluster.storage.management.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.actions.IActionConstants;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.tabcreators.PieChartViewerComposite;

/**
 * @author root
 * 
 */
public class ClusterSummaryView extends ViewPart {
	public static final String ID = ClusterSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private Cluster cluster;
	private Entity entity;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (cluster == null) {
			// cluster = (Cluster)guiHelper.getSelectedEntity(getSite(), Cluster.class);
			cluster = (Cluster) GlusterDataModelManager.getInstance().getModel().getChildren().get(0);
		}

		createSections(parent);
	}

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

	private void createVolumesSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Volumes", null, 1, false);

		Double[] values = new Double[] { Double.valueOf(getVolumeCountByStatus(cluster, VOLUME_STATUS.ONLINE)),
				Double.valueOf(getVolumeCountByStatus(cluster, VOLUME_STATUS.OFFLINE)) };
		createStatusChart(toolkit, section, values);
	}

	private void createServersSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Servers", null, 1, false);

		Double[] values = new Double[] { Double.valueOf(getServerCountByStatus(cluster, SERVER_STATUS.ONLINE)),
				Double.valueOf(getServerCountByStatus(cluster, SERVER_STATUS.OFFLINE)) };

		createStatusChart(toolkit, section, values);
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

	private void createActionsSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Actions", null, 1, false);

		ImageHyperlink imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Create Volume");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.CREATE_VOLUME_BIG));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				IHandlerService hs = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					hs.executeCommand(IActionConstants.COMMAND_CREATE_VOLUME, null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		imageHyperlink = toolkit.createImageHyperlink(section, SWT.NONE);
		imageHyperlink.setText("Add Server(s)");
		imageHyperlink.setImage(guiHelper.getImage(IImageKeys.ADD_SERVER_BIG));
		imageHyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				// TODO Open the "discovered servers" view programmatically"
				IHandlerService hs = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					hs.executeCommand(IActionConstants.COMMAND_ADD_SERVER, null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				NavigationView clusterView = (NavigationView) guiHelper
						.getView(IActionConstants.VIEW_DISCOVERED_SERVER);
				clusterView.selectEntity((entity));
			}
		});
	}

	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, "Cluster Summary");

		createVolumesSection();
		createServersSection();
		createActionsSection();

		parent.layout(); // IMP: lays out the form properly
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (form != null) {
			form.setFocus();
		}
	}
}
