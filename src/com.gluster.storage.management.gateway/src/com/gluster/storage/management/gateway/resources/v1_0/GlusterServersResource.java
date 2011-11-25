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
package com.gluster.storage.management.gateway.resources.v1_0;

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_FSTYPE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_MOUNTPOINT;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_DISK_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DETAILS;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_INTERFACE;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_MAX_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_NEXT_TO;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_PERIOD;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_TYPE;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_DISKS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_FSTYPES;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_SERVERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_STATISTICS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_TASKS;
import static com.gluster.storage.management.core.constants.RESTConstants.STATISTICS_TYPE_CPU;
import static com.gluster.storage.management.core.constants.RESTConstants.STATISTICS_TYPE_MEMORY;
import static com.gluster.storage.management.core.constants.RESTConstants.STATISTICS_TYPE_NETWORK;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.response.FsTypeListResponse;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.ServerNameListResponse;
import com.gluster.storage.management.gateway.data.ClusterInfo;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.services.GlusterServerService;
import com.gluster.storage.management.gateway.tasks.InitializeDiskTask;
import com.gluster.storage.management.gateway.utils.CpuStatsFactory;
import com.gluster.storage.management.gateway.utils.MemoryStatsFactory;
import com.gluster.storage.management.gateway.utils.NetworkStatsFactory;
import com.gluster.storage.management.gateway.utils.StatsFactory;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_SERVERS)
public class GlusterServersResource extends AbstractResource {

	public static final String HOSTNAMETAG = "hostname:";

	@InjectParam
	private TasksResource taskResource;

	@InjectParam
	private ClusterService clusterService;

	@InjectParam
	private CpuStatsFactory cpuStatsFactory;

	@InjectParam
	private MemoryStatsFactory memoryStatsFactory;
	
	@InjectParam
	private NetworkStatsFactory networkStatsFactory;
	
	@InjectParam
	private GlusterServerService glusterServerService;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGlusterServersJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_DETAILS) Boolean details, @QueryParam(QUERY_PARAM_MAX_COUNT) Integer maxCount,
			@QueryParam(QUERY_PARAM_NEXT_TO) String previousServerName) {
		return getGlusterServers(clusterName, MediaType.APPLICATION_JSON, details, maxCount, previousServerName);
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getGlusterServersXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_DETAILS) Boolean details, @QueryParam(QUERY_PARAM_MAX_COUNT) Integer maxCount,
			@QueryParam(QUERY_PARAM_NEXT_TO) String previousServerName) {
		return getGlusterServers(clusterName, MediaType.APPLICATION_XML, details, maxCount, previousServerName);
	}
	
	private Response getGlusterServers(String clusterName, String mediaType, Boolean fetchDetails, Integer maxCount,
			String previousServerName) {
		if(fetchDetails == null) {
			// by default, fetch the server list
			fetchDetails = false;
		}
		
		List<GlusterServer> glusterServers = new ArrayList<GlusterServer>();

		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}

		if (cluster.getServers().size() == 0) {
			return okResponse(new GlusterServerListResponse(glusterServers), mediaType);
		}

		try {
			glusterServers = glusterServerService.getGlusterServers(clusterName, fetchDetails, maxCount, previousServerName);
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}
		
		if(fetchDetails) {
			return okResponse(new GlusterServerListResponse(glusterServers), mediaType);
		} else {
			// no details to be fetched. Return list of server names.
			return okResponse(new ServerNameListResponse(getServerNames(glusterServers)), mediaType);
		}
	}

	private List<String> getServerNames(List<GlusterServer> glusterServers) {
		List<String> serverNames = new ArrayList<String>();
		for(GlusterServer server : glusterServers) {
			serverNames.add(server.getName());
		}
		return serverNames;
	}

	@GET
	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGlusterServerXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		return getGlusterServerResponse(clusterName, serverName, MediaType.APPLICATION_XML);
	}

	@GET
	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGlusterServerJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		return getGlusterServerResponse(clusterName, serverName, MediaType.APPLICATION_JSON);
	}

	private Response getGlusterServerResponse(String clusterName, String serverName, String mediaType) {
		try {
			return okResponse(glusterServerService.getGlusterServer(clusterName, serverName, true), mediaType);
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}
	}

	@POST
	public Response addServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@FormParam(FORM_PARAM_SERVER_NAME) String serverName) {
		return createdResponse(glusterServerService.addServerToCluster(clusterName, serverName));
	}

	@DELETE
	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	public Response removeServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		glusterServerService.removeServerFromCluster(clusterName, serverName);
		return noContentResponse();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}/" + RESOURCE_FSTYPES)
	public FsTypeListResponse getFsTypes(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName, @PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		List<String> fsTypes = glusterServerService.getFsTypes(clusterName, serverName);
		return new FsTypeListResponse(fsTypes);
	}

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}/" + RESOURCE_DISKS + "/{" + PATH_PARAM_DISK_NAME + "}")
	public Response initializeDisk(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName, @PathParam(PATH_PARAM_DISK_NAME) String diskName,
			@FormParam(FORM_PARAM_FSTYPE) String fsType, @FormParam(FORM_PARAM_MOUNTPOINT) String mountPoint) {

		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		if (serverName == null || serverName.isEmpty()) {
			return badRequestResponse("Server name must not be empty!");
		}

		if (diskName == null || diskName.isEmpty()) {
			return badRequestResponse("Disk name must not be empty!");
		}
		
		if (fsType == null || fsType.isEmpty()) {
			fsType = GlusterConstants.FSTYPE_DEFAULT;
			// return badRequestResponse("Parameter [" + FORM_PARAM_FSTYPE + "] is missing in request!");
		}

		InitializeDiskTask initializeTask = new InitializeDiskTask(clusterService, clusterName, serverName, diskName, fsType, mountPoint);
		try {
			initializeTask.start();
			// Check the initialize disk status
			TaskStatus taskStatus = initializeTask.checkStatus();
			initializeTask.getTaskInfo().setStatus(taskStatus);			
			taskResource.addTask(clusterName, initializeTask);

			return acceptedResponse(RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_TASKS + "/"
					+ initializeTask.getId());
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path(RESOURCE_STATISTICS)
	public Response getAggregatedPerformanceDataXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_TYPE) String type, @QueryParam(QUERY_PARAM_PERIOD) String period) {
		return getAggregaredPerformanceData(clusterName, type, period, MediaType.APPLICATION_XML);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(RESOURCE_STATISTICS)
	public Response getAggregaredPerformanceDataJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_TYPE) String type, @QueryParam(QUERY_PARAM_PERIOD) String period) {
		return getAggregaredPerformanceData(clusterName, type, period, MediaType.APPLICATION_JSON);
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}/" + RESOURCE_STATISTICS)
	public Response getPerformanceDataXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName, @PathParam(PATH_PARAM_SERVER_NAME) String serverName,
			@QueryParam(QUERY_PARAM_TYPE) String type, @QueryParam(QUERY_PARAM_PERIOD) String period,
			@QueryParam(QUERY_PARAM_INTERFACE) String networkInterface) {
		return getPerformanceData(clusterName, serverName, type, period, networkInterface, MediaType.APPLICATION_XML);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}/" + RESOURCE_STATISTICS)
	public Response getPerformanceDataJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName, @PathParam(PATH_PARAM_SERVER_NAME) String serverName,
			@QueryParam(QUERY_PARAM_TYPE) String type, @QueryParam(QUERY_PARAM_PERIOD) String period,
			@QueryParam(QUERY_PARAM_INTERFACE) String networkInterface) {
		return getPerformanceData(clusterName, serverName, type, period, networkInterface, MediaType.APPLICATION_JSON);
	}

	private Response getAggregaredPerformanceData(String clusterName, String type, String period, String mediaType) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		if (type == null || type.isEmpty()) {
			throw new GlusterValidationException("Statistics type name must not be empty!");
		}

		if (period == null || period.isEmpty()) {
			throw new GlusterValidationException("Statistics period name must not be empty! Valid values are 1d/1w/1m/1y");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}

		if (cluster.getServers().isEmpty()) {
			// cluster is empty. return empty stats.
			return okResponse(new ServerStats(), mediaType);
		}

		List<String> serverNames = getServerNames(glusterServerService.getGlusterServers(clusterName, false, null, null));
		return okResponse(getStatsFactory(type).fetchAggregatedStats(serverNames, period), mediaType);
	}

	private Response getPerformanceData(String clusterName, String serverName, String type, String period, String networkInterface, String mediaType) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		if (serverName == null || serverName.isEmpty()) {
			throw new GlusterValidationException("Server name must not be empty!");
		}

		if (type == null || type.isEmpty()) {
			throw new GlusterValidationException("Statistics type name must not be empty!");
		}

		if (period == null || period.isEmpty()) {
			throw new GlusterValidationException("Statistics period name must not be empty! Valid values are 1d/1w/1m/1y");
		}

		return okResponse(getStatsFactory(type).fetchStats(serverName, period, networkInterface), mediaType);
	}
	
	private StatsFactory getStatsFactory(String type) {
		if(type.equals(STATISTICS_TYPE_CPU)) {
			return cpuStatsFactory;
		} else if(type.equals(STATISTICS_TYPE_MEMORY)) {
			return memoryStatsFactory;
		} else if(type.equals(STATISTICS_TYPE_NETWORK)) {
			return networkStatsFactory;
		} else {
			throw new GlusterValidationException("Invalid server statistics type [" + type + "]. Valid values are ["
					+ STATISTICS_TYPE_CPU + ", " + STATISTICS_TYPE_NETWORK + ", " + STATISTICS_TYPE_MEMORY + "]");
		}
	}
}
