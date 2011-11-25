/**
 * DiscoveredServersView.java
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
package org.gluster.storage.management.console.views;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.VolumesPage;
import org.gluster.storage.management.core.model.Entity;
import org.gluster.storage.management.core.model.EntityGroup;
import org.gluster.storage.management.core.model.Volume;


/**
 *
 */
public class VolumesView extends ViewPart implements IDoubleClickListener {
	public static final String ID = VolumesView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private EntityGroup<Volume> volumes;
	private VolumesPage page;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void createPartControl(Composite parent) {
		if (volumes == null) {
			Object selectedObj = guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
			if (selectedObj != null && ((EntityGroup) selectedObj).getEntityType() == Volume.class) {
				volumes = (EntityGroup<Volume>)selectedObj;
			}
		}
		
		page = new VolumesPage(parent, getSite(), volumes);
		page.addDoubleClickListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		page.setFocus();
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		NavigationView clusterView = (NavigationView) guiHelper.getView(NavigationView.ID);
		if (clusterView != null) {
			clusterView.selectEntity((Entity) ((StructuredSelection) event.getSelection()).getFirstElement());
		}
	}
}
