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
package com.gluster.storage.management.gui.views.details;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.Entity;

/**
 * For every entity that can be selected from the navigation view (cluster tree), a set of tabs are created on the
 * details view. Each entity has a corresponding tab creator that creates these tabs. These tab creators must implement
 * this interface.
 * <p>
 * <b>Important:</b> Tab creators are cached for performance reasons. Hence they should not store any state information
 * in class level variables.
 */
public interface TabCreator {
	/**
	 * Creates tabs for the given entity
	 * 
	 * @param entity
	 *            Entity for which tabs are to be created
	 * @param tabFolder
	 *            The tab folder in which the tabs are to be created
	 * @param toolkit
	 *            The form toolkit that can be used for create components using Forms API
	 * @param site
	 *            The workbench site that can be used to register as a selection provider
	 */
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site);
}
