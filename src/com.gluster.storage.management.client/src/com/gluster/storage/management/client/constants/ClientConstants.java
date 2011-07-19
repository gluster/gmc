/**
 * ClientConstants.java
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
package com.gluster.storage.management.client.constants;

/**
 *
 */
public class ClientConstants {
	public static final String SYS_PROP_SERVER_URL = "gluster.server.url";
	public static final String DEFAULT_SERVER_URL = "https://localhost:8443/glustermg/linux.gtk.x86_64";
	public static final String CONTEXT_ROOT = "glustermg";
	public static final String REST_API_VERSION = "1.0";
	
	// SSL related
	public static final String TRUSTED_KEYSTORE = "gmc-trusted.keystore"; 
	public static final String TRUSTED_KEYSTORE_ACCESS = "gluster";
	public static final String PROTOCOL_TLS = "TLS";
	public static final String ALGORITHM_SUNX509 = "SunX509";
	public static final String KEYSTORE_TYPE_JKS = "JKS";
}

