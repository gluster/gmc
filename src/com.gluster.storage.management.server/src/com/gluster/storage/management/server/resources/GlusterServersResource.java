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

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path("/cluster/servers")
public class GlusterServersResource extends AbstractServersResource {
	@InjectParam
	private GlusterUtil glusterUtil;
	
	public static final String HOSTNAMETAG = "hostname:";

	public void setGlusterUtil(GlusterUtil glusterUtil) {
		this.glusterUtil = glusterUtil;
	}

	public GlusterUtil getGlusterUtil() {
		return glusterUtil;
	}

	private List<GlusterServer> getServerDetails(String knownServer) {
		List<GlusterServer> glusterServers = glusterUtil.getGlusterServers(knownServer);
		for (GlusterServer server : glusterServers) {
			if (server.getStatus() == SERVER_STATUS.ONLINE) {
				fetchServerDetails(server);
			}
		}
		return glusterServers;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public GlusterServerListResponse getGlusterServers(@QueryParam(RESTConstants.QUERY_PARAM_KNOWN_SERVER) String knownServer) {
		return new GlusterServerListResponse(Status.STATUS_SUCCESS, getServerDetails(knownServer));
	}

	@GET
	@Path("{serverName}")
	@Produces(MediaType.TEXT_XML)
	public GlusterServer getGlusterServer(@PathParam("serverName") String serverName) {
		// TODO: Implement logic to fetch details of a single gluster server (peer)
		GlusterServer server = new GlusterServer(serverName);
		fetchServerDetails(server);
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
	public GlusterServerResponse addServer(@FormParam("serverName") String serverName, @FormParam("existingServer") String existingServer) {
		Status status = glusterUtil.addServer(serverName, existingServer);

		if (!status.isSuccess()) {
			return new GlusterServerResponse(status, null);
		}
		return new GlusterServerResponse(Status.STATUS_SUCCESS, getGlusterServer(serverName));
	}
	
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Status removeServer(@QueryParam("serverName") String serverName) {
		return glusterUtil.removeServer(serverName);
	}

	public static void main(String[] args) {
		GlusterServersResource glusterServersResource = new GlusterServersResource();
		GlusterUtil glusterUtil = new GlusterUtil();
		glusterUtil.setSshUtil(new SshUtil());
		glusterServersResource.setGlusterUtil(glusterUtil);
		System.out.println(glusterServersResource.getServerDetails("127.0.0.1").size());

		// To add a server
//		GlusterServerResponse response = glusterServersResource.addServer("my-server");
//		System.out.println(response.getData().getName());
	}
}
