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
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.gluster.storage.management.server.filters.GlusterResourceFilterFactory;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path("/cluster/servers")
public class GlusterServersResource extends AbstractServersResource {
	@InjectParam
	private GlusterUtil glusterUtil;

	@InjectParam
	private static ServerUtil serverUtil;

	public static final String HOSTNAMETAG = "hostname:";

	public void setGlusterUtil(GlusterUtil glusterUtil) {
		this.glusterUtil = glusterUtil;
	}

	public GlusterUtil getGlusterUtil() {
		return glusterUtil;
	}

	private GlusterServerListResponse getServerDetails(String knownServer) {
		List<GlusterServer> glusterServers = glusterUtil.getGlusterServers(knownServer);
		GenericResponse<Server> serverResponse;
		for (GlusterServer server : glusterServers) {
			if (server.getStatus() == SERVER_STATUS.ONLINE) {
				serverResponse = fetchServerDetails(server);
				if (!serverResponse.getStatus().isSuccess()) {
					return new GlusterServerListResponse(serverResponse.getStatus(), glusterServers);
				}
			}
		}
		return new GlusterServerListResponse(Status.STATUS_SUCCESS, glusterServers);
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public GlusterServerListResponse getGlusterServers(
			@QueryParam(RESTConstants.QUERY_PARAM_KNOWN_SERVER) String knownServer) {
		return getServerDetails(knownServer);
	}

	@GET
	@Path("{serverName}")
	@Produces(MediaType.TEXT_XML)
	public GlusterServerResponse getGlusterServer(@PathParam("serverName") String serverName) {
		// TODO: Implement logic to fetch details of a single gluster server (peer)
		GlusterServer server = new GlusterServer(serverName);
		GenericResponse<Server> serverResponse = fetchServerDetails(server);
		return new GlusterServerResponse(serverResponse.getStatus(), (GlusterServer) serverResponse.getData());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gluster.storage.management.server.resources.AbstractServersResource#fetchServerDetails(com.gluster.storage
	 * .management.core.model.Server)
	 */
	protected GenericResponse<Server> fetchServerDetails(Server server) {
		// fetch standard server details like cpu, disk, memory details
		Object response = serverUtil.executeOnServer(true, server.getName(), "get_server_details.py", Server.class);
		if (response instanceof Status) {
			return new GenericResponse<Server>((Status) response, server);
		}
		server.copyFrom((Server) response); // Update the details in <Server> object
		return new GenericResponse<Server>(Status.STATUS_SUCCESS, (Server) response);
	}

	@POST
	@Produces(MediaType.TEXT_XML)
	public GlusterServerResponse addServer(@FormParam("serverName") String serverName,
			@FormParam("existingServer") String existingServer) {
		Status status = glusterUtil.addServer(serverName, existingServer);

		if (!status.isSuccess()) {
			return new GlusterServerResponse(status, null);
		}
		return new GlusterServerResponse(Status.STATUS_SUCCESS, getGlusterServer(serverName).getGlusterServer());
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
		// System.out.println(glusterServersResource.getServerDetails("127.0.0.1").size());

		// To add a server
		// GlusterServerResponse response = glusterServersResource.addServer("my-server");
		// System.out.println(response.getData().getName());
	}
}
