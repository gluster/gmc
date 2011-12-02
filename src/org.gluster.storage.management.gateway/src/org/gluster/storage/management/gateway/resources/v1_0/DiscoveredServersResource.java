/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Gateway.
 *
 * Gluster Management Gateway is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Gateway is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.gateway.resources.v1_0;

import static org.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_SERVER_NAME;
import static org.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DETAILS;
import static org.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_DISCOVERED_SERVERS;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gluster.storage.management.core.model.Server;
import org.gluster.storage.management.core.response.ServerListResponse;
import org.gluster.storage.management.core.response.ServerNameListResponse;
import org.gluster.storage.management.gateway.services.DiscoveredServerService;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path(RESOURCE_PATH_DISCOVERED_SERVERS)
public class DiscoveredServersResource extends AbstractResource {
	@InjectParam
	private DiscoveredServerService discoveredServerService;
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getDiscoveredServersXML(@QueryParam(QUERY_PARAM_DETAILS) Boolean details) {
		return getDiscoveredServersResponse(details, MediaType.APPLICATION_XML);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDiscoveredServersJSON(@QueryParam(QUERY_PARAM_DETAILS) Boolean details) {
		return getDiscoveredServersResponse(details, MediaType.APPLICATION_JSON);
	}

	private Response getDiscoveredServersResponse(Boolean details, String mediaType) {
		if(details != null && details == true) {
			try {
				List<Server> discoveredServers = discoveredServerService.getDiscoveredServerDetails();
				return okResponse(new ServerListResponse(discoveredServers), mediaType);
			} catch(Exception e) {
				return errorResponse(e.getMessage());
			}
		} else {		
			return okResponse(new ServerNameListResponse(discoveredServerService.getDiscoveredServerNames()), mediaType);
		}
	}

	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getDiscoveredServerXML(@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		return getDiscoveredServerResponse(serverName, MediaType.APPLICATION_XML);
	}

	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDiscoveredServerJSON(@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		return getDiscoveredServerResponse(serverName, MediaType.APPLICATION_JSON);
	}

	private Response getDiscoveredServerResponse(String serverName, String mediaType) {
		if(serverName == null || serverName.isEmpty()) {
			return badRequestResponse("Server name must not be empty!");
		}
		try {
			return okResponse(discoveredServerService.getDiscoveredServer(serverName), mediaType);
		} catch (Exception e) {
			// TODO: Log the exception
			return errorResponse(e.getMessage());
		}
	}
}
