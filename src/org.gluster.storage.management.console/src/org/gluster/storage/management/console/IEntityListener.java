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
package org.gluster.storage.management.console;

import org.gluster.storage.management.core.model.Entity;

/**
 * Any class that is interested in changes to entities in application scope should implement this interface and register
 * with the application using {@link Application#addEntityListener(IEntityListener)}
 * 
 * @author root
 * 
 */
public interface IEntityListener {
	/**
	 * This method is called whenever any attribute of an entity in application scope changes 
	 * @param entity Entity that has changed
	 * @param paremeters List of attribute names that have changed. This can be null.
	 */
	public void entityChanged(Entity entity, String[] paremeters);
}
