/**
 * GlusterServerLogsView.java
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
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.views.pages.ServerLogsPage;
import com.gluster.storage.management.core.model.GlusterServer;

public class GlusterServerLogsView extends ViewPart {
	public static final String ID = GlusterServerLogsView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private GlusterServer server;
	private ServerLogsPage page;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (server == null) {
			server = (GlusterServer) guiHelper.getSelectedEntity(getSite(), GlusterServer.class);
		}
		
		page = new ServerLogsPage(parent, SWT.NONE, server);

		parent.layout(); // IMP: lays out the form properly
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		page.setFocus();
	}
}
