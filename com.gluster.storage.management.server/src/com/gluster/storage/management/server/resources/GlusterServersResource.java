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

import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.ServerListResponse;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.utils.GlusterUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path("/cluster/servers")
public class GlusterServersResource {
	private GlusterUtil glusterUtil = new GlusterUtil();
	public static final String HOSTNAMETAG = "hostname:";

	private List<Server> getServerDetails() {
		List<Server> glusterServers = new ArrayList<Server>();
		List<String> serverNames = glusterUtil.getGlusterServerNames();
		for (String serverName : serverNames) {
			// TODO: With the new design of dedicated management server, this logic has to change.
			// GlusterServersClient client = new GlusterServersClient(serverName);
			// Server server = client.getServer("me");
			// glusterServers.add(server);
		}
		return glusterServers;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public ServerListResponse<Server> getServers() {
		ServerListResponse<Server> response = new ServerListResponse<Server>();
		response.setServers(getServerDetails());
		response.setStatus(Status.STATUS_SUCCESS);
		return response;
	}

	@GET
	@Path("{serverName}")
	@Produces(MediaType.TEXT_XML)
	public String getGlusterServer(@PathParam("serverName") String serverName) {
		// TODO: With new design of dedicated management server, this concept won't work. Need to change.
		if (serverName.equals("me")) {
			return getThisServer();
		}

		// TODO: With the new design of dedicated management server, this logic has to change.
		// Fetch details of given server by sending a REST request to that server
		// return new GlusterServersClient(serverName).getServerXML("me");
		return null;
	}

	public String getThisServer() {
		ProcessResult result = new ProcessUtil().executeCommand("get-server-details.py");
		if (!result.isSuccess()) {
			// TODO:Generate error message and return
		}
		return result.getOutput();
	}

	public static void main(String[] args) {
		GlusterServersResource glusterServersResource = new GlusterServersResource();
		System.out.println(glusterServersResource.getServerDetails());
	}
}
