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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.TabCreator;

public class ServerTabCreator implements TabCreator {
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	private void createServerSummarySection(Server server, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Summary", null, 2, false);
		
		toolkit.createLabel(section, "Number of CPUs: ", SWT.NONE);
		toolkit.createLabel(section, "" + server.getNumOfCPUs(), SWT.NONE);

//		toolkit.createLabel(section, "CPU Usage (%): ", SWT.NONE);
//		toolkit.createLabel(section, "" + server.getCpuUsage(), SWT.NONE);

		toolkit.createLabel(section, "Total Memory (GB): ", SWT.NONE);
		toolkit.createLabel(section, "" + server.getTotalMemory(), SWT.NONE);

//		toolkit.createLabel(section, "Memory in Use (GB): ", SWT.NONE);
//		toolkit.createLabel(section, "" + server.getMemoryInUse(), SWT.NONE);

		toolkit.createLabel(section, "Total Disk Space (GB): ", SWT.NONE);
		toolkit.createLabel(section, "" + NumberUtil.formatNumber(server.getTotalDiskSpace()), SWT.NONE);

//		toolkit.createLabel(section, "Disk Space in Use (GB): ", SWT.NONE);
//		toolkit.createLabel(section, "" + server.getDiskSpaceInUse(), SWT.NONE);
	}

	private void createServerSummaryTab(Server server, TabFolder tabFolder, FormToolkit toolkit) {
		String serverName = server.getName();
		Composite serverSummaryTab = guiHelper.createTab(tabFolder, serverName, IImageKeys.SERVER);
		final ScrolledForm form = guiHelper.setupForm(serverSummaryTab, toolkit, "Discovered Server Summary [" + serverName + "]");
		createServerSummarySection(server, toolkit, form);

		serverSummaryTab.layout(); // IMP: lays out the form properly
	}

	@Override
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		createServerSummaryTab((Server) entity, tabFolder, toolkit);
	}
}
