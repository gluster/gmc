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
package com.gluster.storage.management.client;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.gluster.storage.management.core.model.ConnectionDetails;

public class AuthManager {
	public boolean authenticate(ConnectionDetails connectionDetails) {
//		WebResource service = Client.create(new DefaultClientConfig()).resource(getBaseURI());
//
//		AuthStatus authStatus = service.path("resources").path("login")
//				.queryParam("user", connectionDetails.getUserId())
//				.queryParam("password", connectionDetails.getPassword()).accept(MediaType.TEXT_XML)
//				.get(AuthStatus.class);
//
//		return authStatus.getIsAuthenticated();
		
		// Dummy authentication for demo application
		return (connectionDetails.getPassword().equals("gluster") ? true : false);
	}

	public static void main(String[] args) {
		AuthManager authManager = new AuthManager();
		System.out.println(authManager.authenticate(new ConnectionDetails("", "gluster", "gluster")));
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/glustermc").build();
	}
}
