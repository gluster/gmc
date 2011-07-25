/**
 * GlusterServerDisksView.java
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Event.EVENT_TYPE;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.pages.ServerDisksPage;

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
			server = (GlusterServer) guiHelper.getSelectedEntity(getSite(), GlusterServer.class);
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
