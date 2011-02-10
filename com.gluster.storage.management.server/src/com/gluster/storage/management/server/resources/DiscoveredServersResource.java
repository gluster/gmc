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
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.gluster.storage.management.client.DiscoveredServersClient;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.ServerListResponse;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path("/discoveredservers")
public class DiscoveredServersResource {
	private List<String> discoveredServerNames;

	// TODO: xml should be read from a "work" directory under the tomcat server.
	// Use relative path - do not hard code the absolute path.
	public static final String DISCOVERED_SERVERS_XML = "/GLUSTER/discovered-servers.xml";

	private List<String> getDiscoveredServerNames() {
		// TODO: Run required python script get list of discovered servers and return the same
		return null;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public ServerListResponse<Server> getDiscoveredServers() {
//		File discoveredServersFile = new File(DISCOVERED_SERVERS_XML);
//		String serverNames = new FileUtil().readFileAsString(discoveredServersFile);
		
		ServerListResponse<Server> response = new ServerListResponse<Server>();
		response.setServers(getDiscoveredServerDetails());
		response.setStatus(Status.STATUS_SUCCESS);
		
		return response;
	}

	private List<Server> getDiscoveredServerDetails() {
		List<Server> discoveredServers = new ArrayList<Server>();
		List<String> serverNames = getDiscoveredServerNames();
		for(String serverName : serverNames) {
			DiscoveredServersClient client = new DiscoveredServersClient();
			Server server = client.getServer(serverName);
			discoveredServers.add(server);
		}
		return discoveredServers;
	}

	public void setDiscoveredServerNames(List<String> discoveredServerNames) {
		synchronized (discoveredServerNames) {
			this.discoveredServerNames = discoveredServerNames;
		}
	}

	@Path("/{serverName}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public String getDiscoveredServer(@PathParam("serverName") String serverName) {
		if(serverName.equals("me")) {
			return getThisServer();
		}
		
		// Fetch details of given server by sending a REST request to that server
		return new DiscoveredServersClient().getServerXML(serverName);
	}
	
	public String getThisServer() {
		ProcessResult result = new ProcessUtil().executeCommand("get-server-details.py");
		if (!result.isSuccess()) {
			// TODO:Generate error message and return
		}
		return result.getOutput();
	}

}
