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

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path("/cluster/servers")
public class GlusterServersResource extends AbstractServersResource {

	public static final String HOSTNAMETAG = "hostname:";

	private GlusterServerListResponse getServerDetails(String knownServer) {
		List<GlusterServer> glusterServers = getGlusterUtil().getGlusterServers(knownServer);
		int errCount = 0;
		StringBuilder errMsg = new StringBuilder("Couldn't fetch details for server(s): ");
		
		for (GlusterServer server : glusterServers) {
			if (server.getStatus() == SERVER_STATUS.ONLINE) {
				try {
				fetchServerDetails(server);
				} catch (Exception e) {
					errMsg.append(CoreConstants.NEWLINE + server.getName() + " : [" + e.getMessage() + "]");
					errCount++;
				}
			}
		}
		Status status;
		if (errCount==0) {
			status = new Status(Status.STATUS_CODE_SUCCESS, "Success");
		} else if(errCount == glusterServers.size()) {
			status = new Status(Status.STATUS_CODE_FAILURE, errMsg.toString());
		} else {
			status = new Status(Status.STATUS_CODE_PART_SUCCESS, errMsg.toString());
		}
		return new GlusterServerListResponse(status, glusterServers);
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
	public GlusterServerResponse getGlusterServer(
			@QueryParam(RESTConstants.QUERY_PARAM_KNOWN_SERVER) String knownServer,
			@PathParam("serverName") String serverName) {
		GlusterServer server = getGlusterUtil().getGlusterServer(knownServer, serverName);
		Status status = Status.STATUS_SUCCESS;
		if(server.isOnline()) {
			try {
				fetchServerDetails(server);
			} catch (Exception e) {
				status.setCode(Status.STATUS_CODE_FAILURE); 
			}
		}
		return new GlusterServerResponse(status, server);
	}

	@POST
	@Produces(MediaType.TEXT_XML)
	public GlusterServerResponse addServer(@FormParam("serverName") String serverName,
			@FormParam("existingServer") String existingServer) {
		Status status = getGlusterUtil().addServer(serverName, existingServer);

		if (!status.isSuccess()) {
			return new GlusterServerResponse(status, null);
		}
		return new GlusterServerResponse(Status.STATUS_SUCCESS, getGlusterServer(existingServer, serverName)
				.getGlusterServer());
	}

	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Status removeServer(@QueryParam("serverName") String serverName) {
		return getGlusterUtil().removeServer(serverName);
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
