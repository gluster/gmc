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
package com.gluster.storage.management.server.resources.v1_0;

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_FSTYPE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_DISK_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DETAILS;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_INTERFACE;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_PERIOD;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_TYPE;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_DISKS;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.ServerNameListResponse;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.ServerInfo;
import com.gluster.storage.management.server.services.ClusterService;
import com.gluster.storage.management.server.tasks.InitializeDiskTask;
import com.gluster.storage.management.server.utils.CpuStatsFactory;
import com.gluster.storage.management.server.utils.MemoryStatsFactory;
import com.gluster.storage.management.server.utils.NetworkStatsFactory;
import com.gluster.storage.management.server.utils.SshUtil;
import com.gluster.storage.management.server.utils.StatsFactory;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_SERVERS)
public class GlusterServersResource extends AbstractServersResource {

	public static final String HOSTNAMETAG = "hostname:";

	@InjectParam
	private DiscoveredServersResource discoveredServersResource;

	@InjectParam
	private TasksResource taskResource;

	@Autowired
	private ClusterService clusterService;

	@Autowired
	private SshUtil sshUtil;
	
	@Autowired
	private CpuStatsFactory cpuStatsFactory;

	@Autowired
	private MemoryStatsFactory memoryStatsFactory;
	
	@Autowired
	private NetworkStatsFactory networkStatsFactory;
	
	protected void fetchServerDetails(GlusterServer server) {
		try {
			server.setStatus(SERVER_STATUS.ONLINE);
			serverUtil.fetchServerDetails(server);
		} catch (ConnectionException e) {
			server.setStatus(SERVER_STATUS.OFFLINE);
		}
	}


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGlusterServersJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_DETAILS) Boolean details) {
		return getGlusterServers(clusterName, MediaType.APPLICATION_JSON, details);
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getGlusterServersXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_DETAILS) Boolean details) {
		return getGlusterServers(clusterName, MediaType.APPLICATION_XML, details);
	}
	
	private List<GlusterServer> getGlusterServers(String clusterName, boolean fetchDetails) {
		List<GlusterServer> glusterServers;
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			glusterServers = getGlusterServers(clusterName, onlineServer, fetchDetails);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}
			glusterServers = getGlusterServers(clusterName, onlineServer, fetchDetails);
		}
		return glusterServers;
	}

	private Response getGlusterServers(String clusterName, String mediaType, Boolean fetchDetails) {
		if(fetchDetails == null) {
			// by default, fetch the server details
			fetchDetails = true;
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
			glusterServers = getGlusterServers(clusterName, fetchDetails);
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

	private String fetchDetailsOfServers(List<GlusterServer> glusterServers, GlusterServer onlineServer) {
		String errMsg = "";

		for (GlusterServer server : glusterServers) {
			try {
				fetchServerDetails(server);
			} catch (Exception e) {
				errMsg += CoreConstants.NEWLINE + server.getName() + " : [" + e.getMessage() + "]";
			}
		}
		return errMsg;
	}

	private List<GlusterServer> getGlusterServers(String clusterName, GlusterServer onlineServer, boolean fetchDetails) {
		List<GlusterServer> glusterServers;
		try {
			glusterServers = glusterUtil.getGlusterServers(onlineServer);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}

			glusterServers = glusterUtil.getGlusterServers(onlineServer);
		}
		
		if (fetchDetails) {
			String errMsg = fetchDetailsOfServers(glusterServers, onlineServer);
			if (!errMsg.isEmpty()) {
				throw new GlusterRuntimeException("Couldn't fetch details for server(s): " + errMsg);
			}
		}
		return glusterServers;
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
			return okResponse(getGlusterServer(clusterName, serverName), mediaType);
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}
	}

	private GlusterServer getGlusterServer(String clusterName, String serverName) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterRuntimeException("Cluster name must not be empty!");
		}

		if (serverName == null || serverName.isEmpty()) {
			throw new GlusterRuntimeException("Server name must not be empty!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			throw new GlusterRuntimeException("Cluster [" + clusterName + "] not found!");
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		GlusterServer server = null;
		try {
			server = glusterUtil.getGlusterServer(onlineServer, serverName);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}
			server = glusterUtil.getGlusterServer(onlineServer, serverName);
		}

		if (server.isOnline()) {
			fetchServerDetails(server);
		}
		return server;
	}

	private void performAddServer(String clusterName, String serverName) {
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online server found in cluster [" + clusterName + "]");
		}

		try {
			glusterUtil.addServer(onlineServer.getName(), serverName);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online server found in cluster [" + clusterName + "]");
			}

			glusterUtil.addServer(serverName, onlineServer.getName());
		}
	}

	@POST
	public Response addServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@FormParam(FORM_PARAM_SERVER_NAME) String serverName) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		if (serverName == null || serverName.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_SERVER_NAME + "] is missing in request!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}

		boolean publicKeyInstalled = sshUtil.isPublicKeyInstalled(serverName);
		if (!publicKeyInstalled && !sshUtil.hasDefaultPassword(serverName)) {
			// public key not installed, default password doesn't work. return with error.
			return errorResponse("Gluster Management Gateway uses the default password to set up keys on the server."
					+ CoreConstants.NEWLINE + "However it seems that the password on server [" + serverName
					+ "] has been changed manually." + CoreConstants.NEWLINE
					+ "Please reset it back to the standard default password and try again.");
		}

		String hostName = serverUtil.fetchHostName(serverName);
		List<ServerInfo> servers = cluster.getServers();
		if (servers != null && !servers.isEmpty()) {
			// cluster has at least one existing server, so that peer probe can be performed
			try {
				performAddServer(clusterName, hostName);
			} catch (Exception e) {
				return errorResponse(e.getMessage());
			}
		} else {
			// this is the first server to be added to the cluster, which means no
			// gluster CLI operation required. just add it to the cluster-server mapping
		}

		try {
			// add the cluster-server mapping
			clusterService.mapServerToCluster(clusterName, serverName);
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}

		// since the server is added to a cluster, it should not more be considered as a
		// discovered server available to other clusters
		discoveredServersResource.removeDiscoveredServer(serverName);

		if (!publicKeyInstalled) {
			try {
				// install public key (this will also disable password based ssh login)
				sshUtil.installPublicKey(serverName);
			} catch (Exception e) {
				return errorResponse("Public key could not be installed on [" + serverName + "]! Error: ["
						+ e.getMessage() + "]");
			}
		}

		return createdResponse(serverName);
	}

	@DELETE
	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	public Response removeServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		if (serverName == null || serverName.isEmpty()) {
			return badRequestResponse("Server name must not be empty!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}

		List<ServerInfo> servers = cluster.getServers();
		if (servers == null || servers.isEmpty() || !containsServer(servers, serverName)) {
			return badRequestResponse("Server [" + serverName + "] is not attached to cluster [" + clusterName + "]!");
		}

		if (servers.size() == 1) {
			// Only one server mapped to the cluster, no "peer detach" required.
			// remove the cached online server for this cluster if present
			clusterService.removeOnlineServer(clusterName);
		} else {
			try {
				removeServerFromCluster(clusterName, serverName);
			} catch (Exception e) {
				return errorResponse(e.getMessage());
			}
		}
		clusterService.unmapServerFromCluster(clusterName, serverName);

		return noContentResponse();
	}

	private void removeServerFromCluster(String clusterName, String serverName) {
		// get an online server that is not same as the server being removed
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName, serverName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online server found in cluster [" + clusterName + "]");
		}

		try {
			glusterUtil.removeServer(onlineServer.getName(), serverName);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName, serverName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online server found in cluster [" + clusterName + "]");
			}
			glusterUtil.removeServer(onlineServer.getName(), serverName);
		}

		if (onlineServer.getName().equals(serverName)) {
			// since the cached server has been removed from the cluster, remove it from the cache
			clusterService.removeOnlineServer(clusterName);
		}

		// since the server is removed from the cluster, it is now available to be added to other clusters.
		// Hence add it back to the discovered servers list.
		discoveredServersResource.addDiscoveredServer(serverName);
	}

	private boolean containsServer(List<ServerInfo> servers, String serverName) {
		for (ServerInfo server : servers) {
			if (server.getName().toUpperCase().equals(serverName.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}/" + RESOURCE_DISKS + "/{" + PATH_PARAM_DISK_NAME + "}")
	public Response initializeDisk(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName, @PathParam(PATH_PARAM_DISK_NAME) String diskName,
			@FormParam(FORM_PARAM_FSTYPE) String fsType) {

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
			return badRequestResponse("Parameter [" + FORM_PARAM_FSTYPE + "] is missing in request!");
		}

		InitializeDiskTask initializeTask = new InitializeDiskTask(clusterService, clusterName, serverName, diskName, fsType);
		try {
			initializeTask.start();
			// Check the initialize disk status
			TaskStatus taskStatus = initializeTask.checkStatus();
			initializeTask.getTaskInfo().setStatus(taskStatus);			
			taskResource.addTask(initializeTask);

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

		List<String> serverNames = getServerNames(getGlusterServers(clusterName, false));
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
