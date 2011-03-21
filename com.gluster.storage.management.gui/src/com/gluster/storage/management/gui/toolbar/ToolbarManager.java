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
package com.gluster.storage.management.gui.toolbar;

import com.gluster.storage.management.core.model.Entity;

/**
 * Whenever the current selection/action demands changes to the toolbar, the toolbar manager is used to update the
 * toolbar.
 */
public interface ToolbarManager {
	/**
	 * Updates the toolbar for given entity. This typically means that user is working with the given entity, and hence
	 * the toolbar actions related to that entity should be made visible, and other un-related actions should be hidden.
	 * 
	 * @param entity
	 */
	public void updateToolbar(Entity entity);
}
