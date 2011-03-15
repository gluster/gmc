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

import com.gluster.storage.management.core.model.GenericResponse;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServerListResponse;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.sun.jersey.api.representation.Form;

public class GlusterServersClient extends AbstractClient {
	private static final String RESOURCE_NAME = "/cluster/servers";

	public GlusterServersClient(String serverName, String user, String password) {
		super(serverName, user, password);
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@SuppressWarnings("unchecked")
	public List<GlusterServer> getServers() {
		GlusterServerListResponse response = (GlusterServerListResponse) fetchResource(GlusterServerListResponse.class);
		return response.getServers();
	}

	public Server getServer(String serverName) {
		@SuppressWarnings("unchecked")
		GenericResponse<Server> response = (GenericResponse<Server>) fetchSubResource(serverName, GenericResponse.class);
		return response.getData();
	}

	public String getServerXML(String serverName) {
		return ((String) fetchSubResource(serverName, String.class));
	}

	@SuppressWarnings("unchecked")
	public Status addServer(Server discoveredServer) {
		Form form = new Form();
		form.add("serverName", discoveredServer.getName());
		GenericResponse<String> response = (GenericResponse<String>) postRequest(GenericResponse.class, form);
		return response.getStatus();
	}

	public static void main(String[] args) {
		GlusterServersClient ServerResource = new GlusterServersClient("localhost", "gluster", "gluster");
		List<GlusterServer> glusterServers = ServerResource.getServers();
		for(GlusterServer server : glusterServers) {
			System.out.println(server.getName());
		}

		// Add server
//		Server srv = new Server();
//		srv.setName("my-server");
//		Status response = ServerResource.addServer(srv);
//		System.out.println(response.toString());
	}
}
