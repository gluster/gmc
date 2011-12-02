/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.ServerLogsPage;
import org.gluster.storage.management.core.model.GlusterServer;


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
			server = guiHelper.getSelectedEntity(getSite(), GlusterServer.class);
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

