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

import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_SERVERS;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GlusterServerListResponse;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.gluster.storage.management.core.utils.LRUCache;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.PersistenceDao;
import com.gluster.storage.management.server.data.ServerInfo;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.spi.resource.Singleton;

@Component
@Singleton
@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_SERVERS)
public class GlusterServersResource extends AbstractServersResource {

	public static final String HOSTNAMETAG = "hostname:";
	private LRUCache<String, GlusterServer> clusterServerCache = new LRUCache<String, GlusterServer>(3);
	
	@Autowired
	private PersistenceDao<ClusterInfo> clusterDao;
	
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
	
	// uses cache
	public GlusterServer getOnlineServer(String clusterName) {
		GlusterServer server = clusterServerCache.get(clusterName);
		if(server != null) {
			return server;
		}
		
		return getNewOnlineServer(clusterName);
	}
	
	// Doesn't use cache
	public GlusterServer getNewOnlineServer(String clusterName) {
		// no known online server for this cluster. find one.
		ClusterInfo cluster = getCluster(clusterName);
		if(cluster == null) {
			return null;
		}
		
		for(ServerInfo serverInfo : cluster.getServers()) {
			GlusterServer server = new GlusterServer(serverInfo.getName());
			fetchServerDetails(server);
			if(server.isOnline()) {
				// server is online. add it to cache and return
				clusterServerCache.put(clusterName, server);
			}
			return server;
		}
		
		// no online server found.
		return null;
	}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	public GlusterServerListResponse getGlusterServers(
			@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		GlusterServer onlineServer = getOnlineServer(clusterName);
		if(onlineServer == null) {
			return new GlusterServerListResponse(Status.STATUS_SUCCESS, new ArrayList<GlusterServer>());
		}
		
		List<GlusterServer> glusterServers;
		try {
			glusterServers = glusterUtil.getGlusterServers(onlineServer);
		} catch(ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = getNewOnlineServer(clusterName);
			if(onlineServer == null) {
				return new GlusterServerListResponse(Status.STATUS_SUCCESS, new ArrayList<GlusterServer>());
			}
			
			glusterServers = glusterUtil.getGlusterServers(onlineServer);
		}
		
		int errCount = 0;
		StringBuilder errMsg = new StringBuilder("Couldn't fetch details for server(s): ");
		
		for (GlusterServer server : glusterServers) {
			if (server.getStatus() == SERVER_STATUS.ONLINE && !server.getName().equals(onlineServer.getName())) {
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
	@Path("{serverName}")
	@Produces(MediaType.TEXT_XML)
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
			status = glusterUtil.addServer(serverName, onlineServer.getName());
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
	@Produces(MediaType.TEXT_XML)
	public GlusterServerResponse addServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@FormParam(FORM_PARAM_SERVER_NAME) String serverName) {
		ClusterInfo cluster = getCluster(clusterName);
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
			addServerToCluster(clusterName, serverName);
		} catch (Exception e) {
			return new GlusterServerResponse(new Status(Status.STATUS_CODE_PART_SUCCESS, e.getMessage()), null);
		}

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

	private void addServerToCluster(String clusterName, String serverName) {
		EntityTransaction txn = clusterDao.startTransaction();
		ClusterInfo cluster = getCluster(clusterName);
		ServerInfo server = new ServerInfo(serverName);
		server.setCluster(cluster);
		try {
			clusterDao.save(server);
			cluster.addServer(server);
			clusterDao.update(cluster);
			txn.commit();
		} catch (Exception e) {
			txn.rollback();
			throw new GlusterRuntimeException("Couldn't create cluster-server mapping [" + clusterName + "]["
					+ serverName + "]! Error: " + e.getMessage(), e);
		}
	}
	
	private void removeServerFromCluster(String clusterName, String serverName) {
		EntityTransaction txn = clusterDao.startTransaction();
		ClusterInfo cluster = getCluster(clusterName);
		List<ServerInfo> servers = cluster.getServers();
		for(ServerInfo server : servers) {
			if(server.getName().equals(serverName)) {
				servers.remove(server);
				break;
			}
		}
		try {
			clusterDao.update(cluster);
			txn.commit();
		} catch(Exception e) {
			txn.rollback();
			throw new GlusterRuntimeException("Couldn't unmap server [" + serverName + "] from cluster [" + clusterName
					+ "]! Error: " + e.getMessage(), e);
		}
	}

	private ClusterInfo getCluster(String clusterName) {
		List<ClusterInfo> clusters = clusterDao.findBy("name = ?1", clusterName);
		if(clusters.size() == 0) {
			return null;
		}

		return clusters.get(0);
	}

	@DELETE
	@Produces(MediaType.TEXT_XML)
	@Path("{" + PATH_PARAM_SERVER_NAME + "}")
	public Status removeServer(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_SERVER_NAME) String serverName) {
		GlusterServer onlineServer = getOnlineServer(clusterName);
		if(onlineServer == null) {
			return new Status(Status.STATUS_CODE_FAILURE,
					"No online server found in cluster [" + clusterName + "]");
		}
		
		Status status;
		try {
			return glusterUtil.removeServer(onlineServer.getName(), serverName);
		} catch(ConnectionException e) {
			onlineServer = getNewOnlineServer(clusterName);
			if(onlineServer == null) {
				return new Status(Status.STATUS_CODE_FAILURE,
						"No online server found in cluster [" + clusterName + "]");
			}
			status = glusterUtil.removeServer(onlineServer.getName(), serverName);
		}
		
		if (status.isSuccess()) {
			try {
				removeServerFromCluster(clusterName, serverName);
			} catch (Exception e) {
				return new Status(Status.STATUS_CODE_PART_SUCCESS, e.getMessage());
			}
		}
		
		return status;
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
