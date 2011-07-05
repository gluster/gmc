/**
 * AbstractServersResource.java
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
package com.gluster.storage.management.server.resources.v1_0;

import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.sun.jersey.api.core.InjectParam;

/**
 * Abstract resource class for servers. Abstracts basic server related functionality like "get server details".
 */
public class AbstractServersResource extends AbstractResource {
	@InjectParam
	protected ServerUtil serverUtil;
	
	@InjectParam
	protected GlusterUtil glusterUtil;

	// TODO: Remove this class!
}
