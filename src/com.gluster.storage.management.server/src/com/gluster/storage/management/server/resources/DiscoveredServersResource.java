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
package com.gluster.storage.management.server.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.model.Response;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.ServerListResponse;
import com.gluster.storage.management.core.response.StringListResponse;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path("/discoveredservers")
public class DiscoveredServersResource extends AbstractServersResource {
	private List<String> discoveredServerNames = new ArrayList<String>();
	
	@InjectParam
	private static ServerUtil serverUtil;

	public List<String> getDiscoveredServerNames() {
		return discoveredServerNames;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public Object getDiscoveredServers(@QueryParam("details") Boolean getDetails) {
		if(getDetails != null && getDetails == true) {
			return getDiscoveredServerDetails();
		}
		return new StringListResponse(getDiscoveredServerNames());
	}

	private ServerListResponse getDiscoveredServerDetails() {
		List<Server> discoveredServers = new ArrayList<Server>();
		List<String> serverNames = getDiscoveredServerNames();
		GenericResponse<Server> discoveredServer;
		for (String serverName : serverNames) {
			discoveredServer = getDiscoveredServer(serverName);
			if (!discoveredServer.getStatus().isSuccess()) {
				return new ServerListResponse(discoveredServer.getStatus(), discoveredServers);
			}
			discoveredServers.add(discoveredServer.getData());
		}
		return new ServerListResponse(Status.STATUS_SUCCESS, discoveredServers);
	}
	
	public void setDiscoveredServerNames(List<String> discoveredServerNames) {
		synchronized (discoveredServerNames) {
			this.discoveredServerNames = discoveredServerNames;
		}
	}

	@Path("/{serverName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public GenericResponse<Server> getDiscoveredServer(@PathParam("serverName") String serverName) {
		Server server = new Server(serverName);
		return fetchServerDetails(server);
	}
	
	protected GenericResponse<Server> fetchServerDetails(Server server) {
		// fetch standard server details like cpu, disk, memory details
		Object response = serverUtil.executeOnServer(true, server.getName(), "get_server_details.py", Server.class);
		if (response instanceof Status) {
			return new GenericResponse<Server>((Status)response, server);
		}
		server.copyFrom((Server) response); // Update the details in <Server> object
		return new GenericResponse<Server>( Status.STATUS_SUCCESS, (Server) response);
	}
	

	public static void main(String[] args) {
		StringListResponse listResponse = (StringListResponse)new DiscoveredServersResource().getDiscoveredServers(false);
		for (String server : listResponse.getData()) {
			System.out.println(server);
		}
	}
}
