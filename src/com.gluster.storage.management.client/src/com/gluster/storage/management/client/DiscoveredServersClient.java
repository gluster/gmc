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

import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DETAILS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_DISCOVERED_SERVERS;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.response.ServerListResponse;
import com.gluster.storage.management.core.response.ServerNameListResponse;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DiscoveredServersClient extends AbstractClient {
	
	public DiscoveredServersClient(String clusterName) {
		super(clusterName);
	}
	
	public DiscoveredServersClient(String securityToken, String clusterName) {
		super(securityToken, clusterName);
	}

	@Override
	public String getResourcePath() {
		return RESOURCE_PATH_DISCOVERED_SERVERS;
	}

	private <T> T getDiscoveredServers(Boolean details, Class<T> responseClass) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.putSingle(QUERY_PARAM_DETAILS, details.toString());
		return fetchResource(queryParams, responseClass);
	}

	public List<String> getDiscoveredServerNames() {
		return ((ServerNameListResponse) getDiscoveredServers(Boolean.FALSE, ServerNameListResponse.class))
				.getServerNames();
	}

	public List<Server> getDiscoveredServerDetails() {
		List<Server> servers = ((ServerListResponse) getDiscoveredServers(Boolean.TRUE, ServerListResponse.class))
				.getServers();

		for (Server server : servers) {
			GlusterCoreUtil.updateServerNameOnDevices(server);
		}
		return servers;
	}

	public Server getServer(String serverName) {
		return (Server) fetchSubResource(serverName, Server.class);
	}

	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		try {
			usersClient.authenticate("gluster", "gluster");
			DiscoveredServersClient serverResource = new DiscoveredServersClient(usersClient.getSecurityToken(), "new");
			List<String> discoveredServerNames = serverResource.getDiscoveredServerNames();
			System.out.println(discoveredServerNames);
			List<Server> discoveredServers = serverResource.getDiscoveredServerDetails();
			System.out.println(discoveredServers);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
