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

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_DISK_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_SERVERS;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.gluster.storage.management.core.response.TaskResponse;
import com.gluster.storage.management.core.utils.LRUCache;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.ServerInfo;
import com.gluster.storage.management.server.services.ClusterService;
import com.gluster.storage.management.server.tasks.InitializeDiskTask;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_SERVERS)
public class GlusterServersResource extends AbstractServersResource {

	public static final String HOSTNAMETAG = "hostname:";
	private LRUCache<String, GlusterServer> clusterServerCache = new LRUCache<String, GlusterServer>(3);
	
	@InjectParam
	private DiscoveredServersResource discoveredServersResource;
	
	@InjectParam
	private TasksResource taskResource; 
	
	@Autowired
	private ClusterService clusterService;
	
	@Autowired
	private SshUtil sshUtil;
	
	protected void fetchServerDetails(GlusterServer server) {
		try {
			server.setStatus(SERVER_STATUS.ONLINE);
			super.fetchServerDetails(server);
		} catch(ConnectionException e) {
			server.setStatus(SERVER_STATUS.OFFLINE);
		}
	}
	
	public GlusterServer getOnlineServer(String clusterName) {
		return getOnlineServer(clusterName, "");
	}
	
	// uses cache
	public GlusterServer getOnlineServer(String clusterName, String exceptServerName) {
		GlusterServer server = clusterServerCache.get(clusterName);
		if(server != null && !server.getName().equals(exceptServerName)) {
			return server;
		}
		
		return getNewOnlineServer(clusterName, exceptServerName);
	}

	public GlusterServer getNewOnlineServer(String clusterName) {
		return getNewOnlineServer(clusterName, "");
	}

	// Doesn't use cache
	public GlusterServer getNewOnlineServer(String clusterName, String exceptServerName) {
		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if(cluster == null) {
			return null;
		}
		
		for(ServerInfo serverInfo : cluster.getServers()) {
			GlusterServer server = new GlusterServer(serverInfo.getName());
			fetchServerDetails(server);
			if(server.isOnline() && !server.getName().equals(exceptServerName)) {
				// server is online. add it to cache and return
				clusterServerCache.put(clusterName, server);
				return server;
			}
		}
		
		// no online server found.
		return null;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGlusterServersJSON(
			@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		return getGlusterServers(clusterName, MediaType.APPLICATION_JSON);
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getGlusterServersXML(
			@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		return getGlusterServers(clusterName, MediaType.APPLICATION_XML);
	}

	public Response getGlusterServers(String clusterName, String mediaType) {
		List<GlusterServer> glusterServers = new ArrayList<GlusterServer>();
		
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_CLUSTER_NAME + "] is missing in request!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			return badRequestResponse("Cluster [" + clusterName + "] not found!");
		}

		if(cluster.getServers().size() == 0) {
			return okResponse(new GlusterServerListResponse(glusterServers), mediaType);
		}
		
		GlusterServer onlineServer = getOnlineServer(clusterName);
		if (onlineServer == null) {
			return errorResponse("No online servers found in cluster [" + clusterName + "]");
		}
		
		try {
			glusterServers = getGlusterServers(clusterName, onlineServer);
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}
		
		String errMsg = fetDetailsOfServers(glusterServers, onlineServer);
		if(!errMsg.isEmpty()) {
			return errorResponse("Couldn't fetch details for server(s): " + errMsg);
		}
		
		return okResponse(new GlusterServerListResponse(glusterServers), mediaType);
	}

	public String fetDetailsOfServers(List<GlusterServer> glusterServers, GlusterServer onlineServer) {
		String errMsg = "";
		
		for (GlusterServer server : glusterServers) {
			if (server.getStatus() == SERVER_STATUS.ONLINE && !server.getName().equals(onlineServer.getName())) {
				try {
					fetchServerDetails(server);
				} catch (Exception e) {
					errMsg += CoreConstants.NEWLINE + server.getName() + " : [" + e.getMessage() + "]";
				}
			}
		}
		return errMsg;
	}

	public List<GlusterServer> getGlusterServers(String clusterName, GlusterServer onlineServer) {
		List<GlusterServer> glusterServers;
		try {
			glusterServers = glusterUtil.getGlusterServers(onlineServer);
		} catch(ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = getNewOnlineServer(clusterName);
			if(onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}
			
			glusterServers = glusterUtil.getGlusterServers(onlineServer);
		}
		return glusterServers;
	}

	@GET
	@Path("{serverName}")
	@Produces(MediaType.APPLICATION_XML)
	public GlusterServerResponse getGlusterServer(
			@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		GlusterServer server = glusterUtil.getGlusterServer(getOnlineServer(clusterName), serverName);
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

	private Status performAddServer(String clusterName, String serverName) {
		GlusterServer onlineServer = getOnlineServer(clusterName);
		if(onlineServer == null) {
			return new Status(Status.STATUS_CODE_FAILURE,
					"No online server found in cluster [" + clusterName + "]");
		}
		
		Status status;
		try {
			status = glusterUtil.addServer(onlineServer.getName(), serverName);
			if(status.isSuccess()) {
				// other peer probe to ensure that host names appear in peer probe on both sides
				status = glusterUtil.addServer(serverName, onlineServer.getName());
			}
		} catch(ConnectionException e) {
			onlineServer = getNewOnlineServer(clusterName);
			if(onlineServer == null) {
				return new Status(Status.STATUS_CODE_FAILURE,
						"No online server found in cluster [" + clusterName + "]");
			}
			status = glusterUtil.addServer(serverName, onlineServer.getName());
		}

		return status;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public GlusterServerResponse addServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@FormParam(FORM_PARAM_SERVER_NAME) String serverName) {
		if(clusterName.isEmpty()) {
			return new GlusterServerResponse(
					new Status(Status.STATUS_CODE_FAILURE, "Cluster name should not be empty!"), null);
		}
		
		if(serverName == null || serverName.isEmpty()) {
			return new GlusterServerResponse(new Status(Status.STATUS_CODE_FAILURE, "Form parameter ["
					+ FORM_PARAM_SERVER_NAME + "] is mandatory!"), null);
		}
		
		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if(cluster == null) {
			return new GlusterServerResponse(new Status(Status.STATUS_CODE_FAILURE, "Cluster [" + clusterName
					+ "] doesn't exist!"), null);
		}
		
		boolean publicKeyInstalled = sshUtil.isPublicKeyInstalled(serverName);
		if(!publicKeyInstalled && !sshUtil.hasDefaultPassword(serverName)) {
			// public key not installed, default password doesn't work. return with error.
			return new GlusterServerResponse(new Status(Status.STATUS_CODE_FAILURE,
					"Gluster Management Gateway uses the default password to set up keys on the server."
							+ CoreConstants.NEWLINE + "However it seems that the password on server [" + serverName
							+ "] has been changed manually." + CoreConstants.NEWLINE
							+ "Please reset it back to the standard default password and try again."), null);
		}
		
		List<ServerInfo> servers = cluster.getServers();
		if(servers != null && !servers.isEmpty()) {
			Status status = performAddServer(clusterName, serverName);
			if(!status.isSuccess()) {
				return new GlusterServerResponse(status, null);
			}
		} else {
			// this is the first server to be added to the cluster, which means no
			// gluster CLI operation required. just add it to the cluster-server mapping
		}
		
		try {
			// add the cluster-server mapping
			clusterService.mapServerToCluster(clusterName, serverName);
		} catch (Exception e) {
			return new GlusterServerResponse(new Status(Status.STATUS_CODE_PART_SUCCESS, e.getMessage()), null);
		}
		
		// since the server is added to a cluster, it should not more be considered as a
		// discovered server available to other clusters
		discoveredServersResource.removeDiscoveredServer(serverName);

		// fetch server details
		GlusterServerResponse serverResponse = getGlusterServer(clusterName, serverName);
		
		if (!publicKeyInstalled) {
			try {
				// install public key (this will also disable password based ssh login)
				sshUtil.installPublicKey(serverName);
			} catch (Exception e) {
				return new GlusterServerResponse(new Status(Status.STATUS_CODE_PART_SUCCESS,
						"Public key could not be installed! Error: [" + e.getMessage() + "]"),
						serverResponse.getGlusterServer());
			}
		}

		return serverResponse;
	}

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	public Status removeServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		if (clusterName.isEmpty()) {
			return new Status(Status.STATUS_CODE_FAILURE, "Cluster name should not be empty!");
		}
		
		if(serverName == null || serverName.isEmpty()) {
			return new Status(Status.STATUS_CODE_FAILURE, "Form parameter [" + FORM_PARAM_SERVER_NAME
					+ "] is mandatory!");
		}
		
		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if(cluster == null) {
			return new Status(Status.STATUS_CODE_FAILURE, "Cluster [" + clusterName + "] doesn't exist!");
		}

		List<ServerInfo> servers = cluster.getServers();
		if(servers == null || servers.isEmpty()) {
			return new Status(Status.STATUS_CODE_FAILURE, "Server [" + serverName + "] is not attached to cluster ["
					+ clusterName + "]!");
		}
		
		Status status = Status.STATUS_SUCCESS;
		if(servers.size() == 1) {
			// Only one server mapped to the cluster, no "peer detach" required.
			// remove the cached online server for this cluster if present
			clusterServerCache.remove(clusterName);
		} else {
			// get an online server that is not same as the server being removed
			GlusterServer onlineServer = getOnlineServer(clusterName, serverName);
			if (onlineServer == null) {
				return new Status(Status.STATUS_CODE_FAILURE, "No online server found in cluster [" + clusterName + "]");
			}

			try {
				status = glusterUtil.removeServer(onlineServer.getName(), serverName);
			} catch (ConnectionException e) {
				// online server has gone offline! try with a different one.
				onlineServer = getNewOnlineServer(clusterName, serverName);
				if (onlineServer == null) {
					return new Status(Status.STATUS_CODE_FAILURE, "No online server found in cluster [" + clusterName
							+ "]");
				}
				status = glusterUtil.removeServer(onlineServer.getName(), serverName);
				if(!status.isSuccess()) {
					return status;
				}
			}
			
			if(onlineServer.getName().equals(serverName)) {
				// since the cached server has been removed from the cluster, remove it from the cache
				clusterServerCache.remove(clusterName);
			}
		}
		
		
		try {
			clusterService.unmapServerFromCluster(clusterName, serverName);
		} catch (Exception e) {
			return new Status(Status.STATUS_CODE_PART_SUCCESS, e.getMessage());
		}
		
		// since the server is removed from the cluster, it is now available to be added to other clusters. 
		// Hence add it back to the discovered servers list.
		discoveredServersResource.addDiscoveredServer(serverName);
		
		return status;
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	public TaskResponse initializeDisk(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName, 
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName, @PathParam(PATH_PARAM_DISK_NAME) String diskName) {
		
		TaskResponse taskResponse = new TaskResponse();
		InitializeDiskTask initializeTask = new InitializeDiskTask(diskName, serverName);
		TaskInfo taskInfo = initializeTask.start();
		if (taskInfo.isSuccess()) {
			taskResource.addTask(initializeTask);
		}
		taskResponse.setData(taskInfo);
		taskResponse.setStatus(new Status(Status.STATUS_CODE_SUCCESS, ""));
		
		return taskResponse;
	}
	

	private void setGlusterUtil(GlusterUtil glusterUtil) {
		this.glusterUtil = glusterUtil;
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
