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
package org.gluster.storage.management.core.model;

/**
 * By default, the {@link EntityViewerFilter} filters the objects by parsing the
 * output of {@link Object#toString()} with the filter string. Classes that need
 * specific filtering logic can implement this interface. The default logic will
 * then be overridden by the method {@link Filterable#filter(String)}.
 */
public interface Filterable {
	/**
	 * @param filterString
	 *            String to be used for filtering
	 * @param caseSensitive
	 *            Flag indicating whether the filtering should be case sensitive
	 * @return true if the object can be selected using the filter string, else
	 *         false
	 */
	public boolean filter(String filterString, boolean caseSensitive);
}
