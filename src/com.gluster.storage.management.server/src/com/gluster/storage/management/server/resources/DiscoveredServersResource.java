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

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Response;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.ServerListResponse;
import com.gluster.storage.management.core.response.StringListResponse;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_DISCOVERED_SERVERS;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path(RESOURCE_PATH_DISCOVERED_SERVERS)
public class DiscoveredServersResource extends AbstractServersResource {
	private List<String> discoveredServerNames = new ArrayList<String>();
	
	public List<String> getDiscoveredServerNames() {
		return discoveredServerNames;
	}
	
	public void setDiscoveredServerNames(List<String> discoveredServerNames) {
		synchronized (discoveredServerNames) {
			this.discoveredServerNames = discoveredServerNames;
		}
	}
	
	public void removeDiscoveredServer(String serverName) {
		discoveredServerNames.remove(serverName);
	}
	
	public void addDiscoveredServer(String serverName) {
		discoveredServerNames.add(serverName);
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	@SuppressWarnings("rawtypes")
	public Response getDiscoveredServers(@QueryParam("details") Boolean getDetails) {
		if(getDetails != null && getDetails == true) {
			return getDiscoveredServerDetails();
		}
		return new StringListResponse(getDiscoveredServerNames());
	}

	private ServerListResponse getDiscoveredServerDetails() {
		List<Server> discoveredServers = new ArrayList<Server>();
		List<String> serverNames = getDiscoveredServerNames();
		GenericResponse<Server> discoveredServerResponse;
		int errCount = 0;
		StringBuilder errMsg = new StringBuilder("Couldn't fetch details for server(s): ");
		for (String serverName : serverNames) {
			discoveredServerResponse = getDiscoveredServer(serverName);
			if (!discoveredServerResponse.getStatus().isSuccess()) {
				errMsg.append(CoreConstants.NEWLINE + serverName + " : " + discoveredServerResponse.getStatus());
				errCount++;
			} else {
				discoveredServers.add(discoveredServerResponse.getData());
			}
		}
		Status status = null;
		if(errCount == 0) {
			status = new Status(Status.STATUS_CODE_SUCCESS, "Success");
		} else if(errCount == serverNames.size()) {
			status = new Status(Status.STATUS_CODE_FAILURE, errMsg.toString());
		} else {
			status = new Status(Status.STATUS_CODE_PART_SUCCESS, errMsg.toString());
		}
		return new ServerListResponse(status, discoveredServers);
	}
	
	@Path("/{serverName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public GenericResponse<Server> getDiscoveredServer(@PathParam("serverName") String serverName) {
		Server server = new Server(serverName);
		try {
			fetchServerDetails(server);
		} catch (Exception e) {
			return new GenericResponse<Server>(new Status(e), null);
		}
		return new GenericResponse<Server>(Status.STATUS_SUCCESS, server);
	}
	
	public static void main(String[] args) {
		StringListResponse listResponse = (StringListResponse)new DiscoveredServersResource().getDiscoveredServers(false);
		for (String server : listResponse.getData()) {
			System.out.println(server);
		}
	}
}
