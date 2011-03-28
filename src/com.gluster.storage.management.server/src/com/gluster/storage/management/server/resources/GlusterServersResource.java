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

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.model.GenericResponse;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.GlusterServerListResponse;
import com.gluster.storage.management.core.model.GlusterServerResponse;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.utils.GlusterUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path("/cluster/servers")
public class GlusterServersResource extends AbstractServersResource {
	private GlusterUtil glusterUtil = new GlusterUtil();
	public static final String HOSTNAMETAG = "hostname:";

	private List<GlusterServer> getServerDetails() {
		List<GlusterServer> glusterServers = glusterUtil.getGlusterServers();
		for (GlusterServer server : glusterServers) {
			if (server.getStatus() == SERVER_STATUS.ONLINE) {
				fetchServerDetails(server);
				// server.setPreferredNetworkInterface(server.getNetworkInterfaces().get(0));
			}
		}
		return glusterServers;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public GlusterServerListResponse getGlusterServers() {
		return new GlusterServerListResponse(Status.STATUS_SUCCESS, getServerDetails());
	}

	@GET
	@Path("{serverName}")
	@Produces(MediaType.TEXT_XML)
	public GlusterServer getGlusterServer(@PathParam("serverName") String serverName) {
		GlusterServer server = new GlusterServer(serverName);
		fetchServerDetails(server);
		// server.setPreferredNetworkInterface(server.getNetworkInterfaces().get(0));
		server.setStatus(SERVER_STATUS.ONLINE);
		return server;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gluster.storage.management.server.resources.AbstractServersResource#fetchServerDetails(com.gluster.storage
	 * .management.core.model.Server)
	 */
	@Override
	protected void fetchServerDetails(Server server) {
		// fetch standard server details like cpu, disk, memory details
		super.fetchServerDetails(server);

		// TODO: Fetch gluster server details like status
	}

	@POST
	@Produces(MediaType.TEXT_XML)
	public GlusterServerResponse addServer(@FormParam("serverName") String serverName) {
		ProcessResult result = glusterUtil.addServer(serverName);

		if (!result.isSuccess()) {
			Status failure = new Status(Status.STATUS_CODE_FAILURE, "Add server [" + serverName + "] failed: [" + result.getExitValue()
					+ "][" + result.getOutput() + "]");
			return new GlusterServerResponse(failure, null);
		}
		return new GlusterServerResponse(Status.STATUS_SUCCESS, getGlusterServer(serverName));
	}

	public static void main(String[] args) {
		GlusterServersResource glusterServersResource = new GlusterServersResource();
		System.out.println(glusterServersResource.getServerDetails());

		// To add a server
		GlusterServerResponse response = glusterServersResource.addServer("my-server");
		System.out.println(response.getData().getName());
	}
}
