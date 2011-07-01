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

import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_SERVERS;

import java.util.List;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.sun.jersey.api.representation.Form;

public class GlusterServersClient extends AbstractClient {
	
	public GlusterServersClient() {
		super();
	}
	
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

	public List<GlusterServer> getServers() {
		return ((GlusterServerListResponse) fetchResource(GlusterServerListResponse.class)).getServers();
	}

	public GlusterServer getGlusterServer(String serverName) {
		return (GlusterServer) fetchSubResource(serverName, GlusterServer.class);
	}

	public void addServer(Server discoveredServer) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_SERVER_NAME, discoveredServer.getName());
		postRequest(form);
	}
	
	public void initializeDisk(String serverName, String diskName) {
		putRequest(serverName + "/" + RESTConstants.RESOURCE_DISKS + "/" + diskName);
	}
	
	public void removeServer(String serverName) {
		deleteSubResource(serverName);
	}

	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		try {
			usersClient.authenticate("gluster", "gluster");
			GlusterServersClient glusterServersClient = new GlusterServersClient(usersClient.getSecurityToken(), "cluster1");
			List<GlusterServer> glusterServers = glusterServersClient.getServers();
			for (GlusterServer server : glusterServers) {
				System.out.println(server.getName());
			}

			// Add server
			 Server srv = new Server();
			 srv.setName("server3");
			 glusterServersClient.addServer(srv);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
