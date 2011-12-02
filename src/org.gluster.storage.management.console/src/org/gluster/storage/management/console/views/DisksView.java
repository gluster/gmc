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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.DisksPage;
import org.gluster.storage.management.core.model.Disk;
import org.gluster.storage.management.core.model.EntityGroup;
import org.gluster.storage.management.core.model.GlusterServer;


public class DisksView extends ViewPart {
	public static final String ID = DisksView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private EntityGroup<GlusterServer> servers;
	private DisksPage page;

	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		if (servers == null) {
			servers = guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
		}
		
		page = new DisksPage(parent, SWT.NONE, getSite(), getAllDisks(servers));
		//page.layout(); // IMP: lays out the form properly
	}

	private List<Disk> getAllDisks(EntityGroup<GlusterServer> servers) {
		List<Disk> disks = new ArrayList<Disk>();
		for(GlusterServer server : servers.getEntities()) {
			disks.addAll(server.getDisks());
		}
		return disks;
	}

	@Override
	public void setFocus() {
		page.setFocus();
	}
}