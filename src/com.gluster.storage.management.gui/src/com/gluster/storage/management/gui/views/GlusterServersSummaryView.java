/**
 * GlusterServersSummaryView.java
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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.utils.PieChartViewerComposite;

/**
 *
 */
public class GlusterServersSummaryView extends ViewPart {
	public static final String ID = GlusterServersSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private EntityGroup<GlusterServer> servers;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (servers == null) {
			servers = (EntityGroup<GlusterServer>)guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
		}
		setPartName("Summary");
		createSections(parent);
	}

	/**
	 * @param parent
	 */
	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, "Servers - Summary");
		
		createSummarySection();
		createRunningTasksSection();
		createAlertsSection();
		
		parent.layout(); // IMP: lays out the form properly
	}
	
	private void createSummarySection() {
		Composite section = guiHelper.createSection(form, toolkit, "Availability", null, 2, false);

		Double[] values = new Double[] { Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.ONLINE)),
				Double.valueOf(getServerCountByStatus(servers, SERVER_STATUS.OFFLINE)) };
		createStatusChart(section, values);
	}
	
	private int getServerCountByStatus(EntityGroup<GlusterServer> servers, SERVER_STATUS status) {
		int count = 0;
		for (GlusterServer server : (List<GlusterServer>)servers.getEntities()) {
			if (server.getStatus() == status) {
				count++;
			}
		}
		return count;
	}

	private void createStatusChart(Composite section, Double[] values) {
		String[] categories = new String[] { "Online", "Offline" };
		PieChartViewerComposite chartViewerComposite = new PieChartViewerComposite(section, SWT.NONE, categories, values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 300;
		data.heightHint = 150;
		chartViewerComposite.setLayoutData(data);	
	}


	private void createAlertsSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Alerts", null, 2, false);

		toolkit.createLabel(section, "Any alerts related to servers\nwill be displayed here.");
	}

	private void createRunningTasksSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Running Tasks", null, 2, false);

		toolkit.createLabel(section, "List of running tasks related to\nservers will be displayed here.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}
}