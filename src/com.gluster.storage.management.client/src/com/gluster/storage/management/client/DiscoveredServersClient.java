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

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.core.model.Response;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.ServerListResponse;
import com.gluster.storage.management.core.response.StringListResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DiscoveredServersClient extends AbstractClient {
	private static final String RESOURCE_NAME = "discoveredservers";

	public DiscoveredServersClient(String serverName, String securityToken) {
		super(securityToken);
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@SuppressWarnings("rawtypes")
	private Object getDiscoveredServers(Boolean getDetails, Class responseClass) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.putSingle("details", getDetails.toString());

		return ((Response) fetchResource(queryParams, responseClass)).getData();
	}

	@SuppressWarnings("unchecked")
	public List<String> getDiscoveredServerNames() {
		return (List<String>) getDiscoveredServers(Boolean.FALSE, StringListResponse.class);
	}

	@SuppressWarnings("unchecked")
	public List<Server> getDiscoveredServerDetails() {
		return (List<Server>) getDiscoveredServers(Boolean.TRUE, ServerListResponse.class);
	}

	@SuppressWarnings("unchecked")
	public Server getServer(String serverName) {
		GenericResponse<Server> response = (GenericResponse<Server>) fetchSubResource(serverName, GenericResponse.class);
		return response.getData();
	}

	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		if (usersClient.authenticate("gluster", "gluster").isSuccess()) {
			DiscoveredServersClient serverResource = new DiscoveredServersClient("localhost",
					usersClient.getSecurityToken());
			List<String> discoveredServerNames = serverResource.getDiscoveredServerNames();
			System.out.println(discoveredServerNames);
			List<Server> discoveredServers = serverResource.getDiscoveredServerDetails();
			System.out.println(discoveredServers);

			// Server serverDetails = ServerResource.getServer("localhost");
			// System.out.println(serverDetails.getName());
		}
	}
}
