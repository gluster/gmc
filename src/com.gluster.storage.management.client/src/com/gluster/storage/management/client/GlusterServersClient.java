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

import com.gluster.storage.management.core.constants.RESTConstants;
import static com.gluster.storage.management.core.constants.RESTConstants.*;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class GlusterServersClient extends AbstractClient {
	public GlusterServersClient(String clusterName) {
		super(clusterName);
	}
	
	public GlusterServersClient(String securityToken, String clusterName) {
		super(securityToken, clusterName);
	}

	@Override
	public String getResourcePath() {
		return RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_SERVERS;
	}

	public GlusterServerListResponse getServers() {
		return (GlusterServerListResponse) fetchResource(GlusterServerListResponse.class);
	}

	@SuppressWarnings("unchecked")
	public Server getServer(String serverName) {
		GenericResponse<Server> response = (GenericResponse<Server>) fetchSubResource(serverName, GenericResponse.class);
		return response.getData();
	}

	public String getServerXML(String serverName) {
		return ((String) fetchSubResource(serverName, String.class));
	}

	public GlusterServerResponse addServer(Server discoveredServer) {
		Form form = new Form();
		form.add("serverName", discoveredServer.getName());
		return (GlusterServerResponse)postRequest(GlusterServerResponse.class, form);
	}
	
	public Status removeServer(String serverName) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_SERVER_NAME, serverName);
		return (Status) deleteResource(Status.class, queryParams);
	}

	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		if (usersClient.authenticate("gluster", "gluster").isSuccess()) {
			GlusterServersClient glusterServersClient = new GlusterServersClient(usersClient.getSecurityToken(), "cluster1");
			List<GlusterServer> glusterServers = glusterServersClient.getServers().getData();
			for (GlusterServer server : glusterServers) {
				System.out.println(server.getName());
			}

			// Add server
			 Server srv = new Server();
			 srv.setName("server3");
			 GlusterServerResponse response = glusterServersClient.addServer(srv);
			 System.out.println(response.getGlusterServer().getName());
			 System.out.println(response.getStatus().isSuccess());
			
		}
	}
}
