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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.gluster.storage.management.client.utils.ClientUtil;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.ServerDetailsResponse;
import com.gluster.storage.management.core.model.ServerListResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class DiscoveredServersClient {
	private final URI BASE_URI = UriBuilder.fromUri("http://localhost:8080/glustermc").build();

	public List<Server> getDiscoveredServers() {
		WebResource service = Client.create(new DefaultClientConfig()).resource(BASE_URI);

		@SuppressWarnings("unchecked")
		ServerListResponse<Server> response = service.path("resources").path("server").path("discover")
				.accept(MediaType.TEXT_XML).get(ServerListResponse.class);

		return response.getData();
	}

	private Builder getServerBuilder(String serverName) {
		WebResource service = Client.create(new DefaultClientConfig()).resource(
				new ClientUtil().getServerBaseURI(serverName));

		return service.path("resources").path("server").path("details")
				.accept(MediaType.TEXT_XML);
	}
	
	public Server getServer(String serverName) {
		return getServerBuilder(serverName).get(ServerDetailsResponse.class).getData();
	}

	public String getServerXML(String serverName) {
		return getServerBuilder(serverName).get(String.class);
	}

	public static void main(String[] args) {
		DiscoveredServersClient ServerResource = new DiscoveredServersClient();
		List<Server> discoveredServers = ServerResource.getDiscoveredServers();
		System.out.println(discoveredServers.size());

		Server serverDetails = ServerResource.getServer("localhost");
		System.out.println(serverDetails.getName());
	}
}
