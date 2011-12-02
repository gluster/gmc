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
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.ServerDisksPage;
import org.gluster.storage.management.core.model.ClusterListener;
import org.gluster.storage.management.core.model.DefaultClusterListener;
import org.gluster.storage.management.core.model.Event;
import org.gluster.storage.management.core.model.GlusterServer;
import org.gluster.storage.management.core.model.Event.EVENT_TYPE;


public class GlusterServerDisksView extends ViewPart {
	public static final String ID = GlusterServerDisksView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private ClusterListener clusterListener;
	private GlusterServer server;
	private ServerDisksPage page;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		if (server == null) {
			server = guiHelper.getSelectedEntity(getSite(), GlusterServer.class);
		}
		page = new ServerDisksPage(parent, SWT.NONE, getSite(), server.getDisks());

		final ViewPart thisView = this;
		clusterListener = new DefaultClusterListener() {
			@Override
			public void serverChanged(GlusterServer server, Event event) {
				super.serverChanged(server, event);
				if(event.getEventType() == EVENT_TYPE.GLUSTER_SERVER_CHANGED) {
					if(!server.isOnline()) {
						getViewSite().getPage().hideView(thisView);
					}
				}
			}
		};
		
		GlusterDataModelManager.getInstance().addClusterListener(clusterListener);
		
		parent.layout(); // IMP: lays out the form properly
	}
	
	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(clusterListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		page.setFocus();
	}
}
