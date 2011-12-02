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
package org.gluster.storage.management.client.utils;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.gluster.storage.management.client.constants.ClientConstants;


public class ClientUtil {

	public static URI getServerBaseURI() {
		return UriBuilder.fromUri(getBaseURL()).path(ClientConstants.REST_API_VERSION).build();
	}
	
	private static String getBaseURL() {
		// remove the platform path (e.g. /linux.gtk.x86_64) from the URL
		return System.getProperty(ClientConstants.SYS_PROP_SERVER_URL, ClientConstants.DEFAULT_SERVER_URL)
				.replaceAll(ClientConstants.CONTEXT_ROOT + "\\/.*", ClientConstants.CONTEXT_ROOT + "\\/");
	}
}
