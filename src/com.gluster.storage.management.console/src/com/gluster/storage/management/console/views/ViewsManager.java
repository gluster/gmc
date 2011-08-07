/**
 * ViewsManager.java
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

import com.gluster.storage.management.core.model.Entity;

/**
 * Whenever the current selection/action demands opening different set of views, the views manager is used to open
 * appropriate views.
 */
public interface ViewsManager {
	/**
	 * Updates the views for given entity. This typically means that user is working with the given entity, and hence
	 * the views related to that entity should be made visible, and other un-related views should be hidden.
	 * 
	 * @param entity
	 *            The entity for which views are to be updated
	 */
	public void updateViews(Entity entity);
}