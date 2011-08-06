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
package com.gluster.storage.management.console.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.utils.NumberUtil;

/**
 * @author root
 * 
 */
public class DiscoveredServerView extends ViewPart {
	public static final String ID = DiscoveredServerView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private Server server;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (server == null) {
			server = (Server) guiHelper.getSelectedEntity(getSite(), Server.class);
		}
		createSections(parent);
	}

	private void createServerSummarySection() {
		Composite section = guiHelper.createSection(form, toolkit, "Summary", null, 2, false);

		toolkit.createLabel(section, "Number of CPUs: ", SWT.NONE);
		toolkit.createLabel(section, "" + server.getNumOfCPUs(), SWT.NONE);

		toolkit.createLabel(section, "Total Memory (GB): ", SWT.NONE);
		toolkit.createLabel(section, "" + NumberUtil.formatNumber((server.getTotalMemory() / 1024)), SWT.NONE);

		toolkit.createLabel(section, "Total Disk Space (GB): ", SWT.NONE);
		toolkit.createLabel(section, "" + NumberUtil.formatNumber((server.getTotalDiskSpace() / 1024)), SWT.NONE);
	}

	private void createSections(Composite parent) {
		String serverName = server.getName();
		form = guiHelper.setupForm(parent, toolkit, "Discovered Server Summary [" + serverName + "]");
		createServerSummarySection();

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
