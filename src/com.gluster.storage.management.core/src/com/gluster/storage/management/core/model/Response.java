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
package com.gluster.storage.management.core.model;

/**
 * Interface for server response. Any request handled by the Gluster Management
 * Server will result in a response that must be an instance of a class
 * implementing this interface.
 */
public interface Response<T> {
	/**
	 * @return Status of request processing
	 */
	public Status getStatus();

	/**
	 * @return Data associated with the response. e.g. A "discover servers"
	 *         request will return the list of discovered servers.
	 */
	public T getData();
}
