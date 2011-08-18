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
package com.gluster.storage.management.gateway.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.gateway.data.ClusterInfo;
import com.gluster.storage.management.gateway.data.ServerInfo;
import com.gluster.storage.management.gateway.resources.v1_0.DiscoveredServersResource;
import com.gluster.storage.management.gateway.utils.GlusterUtil;
import com.gluster.storage.management.gateway.utils.ServerUtil;

/**
 *
 */
@Component
public class GlusterServerService {
	@Autowired
	protected ServerUtil serverUtil;
	
	@Autowired
	private ClusterService clusterService;

	@Autowired
	private GlusterUtil glusterUtil;
	
	@Autowired
	private VolumeService volumeService;
	
	// TODO: create a discovered servers "service" instead of injecting the resource directly
	@Autowired
	private DiscoveredServersResource discoveredServersResource;
	
	public void fetchServerDetails(GlusterServer server) {
		try {
			server.setStatus(SERVER_STATUS.ONLINE);
			serverUtil.fetchServerDetails(server);
		} catch (ConnectionException e) {
			server.setStatus(SERVER_STATUS.OFFLINE);
		}
	}
	
	public List<GlusterServer> getGlusterServers(String clusterName, boolean fetchDetails, Integer maxCount,
			String previousServerName) {
		List<GlusterServer> glusterServers;
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			glusterServers = getGlusterServers(clusterName, onlineServer, fetchDetails, maxCount, previousServerName);
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				glusterServers = getGlusterServers(clusterName, onlineServer, fetchDetails, maxCount, previousServerName);
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
			
		}
		return glusterServers;
	}
	
	private List<GlusterServer> getGlusterServers(String clusterName, GlusterServer onlineServer, boolean fetchDetails,
			Integer maxCount, String previousServerName) {
		List<GlusterServer> glusterServers;
		try {
			glusterServers = glusterUtil.getGlusterServers(onlineServer);
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				glusterServers = glusterUtil.getGlusterServers(onlineServer);
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
		
		// skip the servers by maxCount / previousServerName
		glusterServers = GlusterCoreUtil.skipEntities(glusterServers, maxCount, previousServerName);
		
		if (fetchDetails) {
			String errMsg = fetchDetailsOfServers(glusterServers);
			if (!errMsg.isEmpty()) {
				throw new GlusterRuntimeException("Couldn't fetch details for server(s): " + errMsg);
			}
		}
		return glusterServers;
	}

	private String fetchDetailsOfServers(List<GlusterServer> glusterServers) {
		try {
			return new ServerDetailsThread(glusterServers).fetchDetails();
		} catch(InterruptedException e) {
			throw new GlusterRuntimeException("Exception while fetching details of servers! Error: [" + e.getMessage()
					+ "]", e);
		}
//		String errMsg = "";
//
//		for (GlusterServer server : glusterServers) {
//			try {
//				fetchServerDetails(server);
//			} catch (Exception e) {
//				errMsg += CoreConstants.NEWLINE + server.getName() + " : [" + e.getMessage() + "]";
//			}
//		}
//		return errMsg;
	}
	
	public class ServerDetailsThread extends Thread {
		private List<String> errors;
		private List<GlusterServer> glusterServers;
		private GlusterServer server;
		private final Logger logger = Logger.getLogger(ServerDetailsThread.class);

		/**
		 * This constructor should be called by clients that need to fetch details of the servers in parallel
		 * @param glusterServers
		 */
		public ServerDetailsThread(List<GlusterServer> glusterServers) {
			// create a synchronized "copy" so that the original list remains untouched
			this(Collections.synchronizedList(new ArrayList<GlusterServer>(glusterServers)), Collections
					.synchronizedList(new ArrayList<String>()), null);
		}
		
		/**
		 * Private constructor called on each thread
		 * @param glusterServers
		 * @param errors
		 */
		private ServerDetailsThread(List<GlusterServer> glusterServers, List<String> errors, GlusterServer server) {
			this.glusterServers = glusterServers;
			this.errors = errors;
			this.server = server;
		}

		/**
		 * Call this method to fetch details of all the servers passed in the constructor. Internally creates one thread
		 * for each server
		 * 
		 * @param glusterServers
		 * @return
		 */
		public String fetchDetails() throws InterruptedException {
			for (int i = glusterServers.size()-1; i >= 0 ; i--) {
				new ServerDetailsThread(glusterServers, errors, glusterServers.get(i)).start();
				if(i >= 5 && i % 5 == 0) {
					// After every 5 servers, wait for 1 second so that we don't end up with too many running threads
					Thread.sleep(1000);
				}
			}
			
			// Wait till all servers have been processed
			while (!glusterServers.isEmpty()) {
				Thread.sleep(500);
			}
			
			String errMsg = "";
			for(String error : errors) {
				if(!errMsg.isEmpty()) {
					errMsg += CoreConstants.NEWLINE;
				}
				errMsg +=  error;
			}
			
			return errMsg;
		}
		
		@Override
		public void run() {
			try {
				logger.info("fetching details of server [" + server.getName() + "] - start");
				fetchServerDetails(server);
				logger.info("fetching details of server [" + server.getName() + "] - end");
			} catch (Exception e) {
				logger.error("fetching details of server [" + server.getName() + "] - error", e);
				errors.add(server.getName() + " : [" + e.getMessage() + "]");
			}
			glusterServers.remove(server);
		}
	}
	
	public GlusterServer getGlusterServer(String clusterName, String serverName, Boolean fetchDetails) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}

		if (serverName == null || serverName.isEmpty()) {
			throw new GlusterValidationException("Server name must not be empty!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			throw new GlusterRuntimeException("Cluster [" + clusterName + "] not found!");
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		return getGlusterServer(clusterName, serverName, onlineServer, fetchDetails);
	}
	
	private GlusterServer getGlusterServer(String clusterName, String serverName, GlusterServer onlineServer,
			Boolean fetchDetails) {
		GlusterServer server = null;
		try {
			server = glusterUtil.getGlusterServer(onlineServer, serverName);
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				server = glusterUtil.getGlusterServer(onlineServer, serverName);
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}

		if (fetchDetails && server.isOnline()) {
			fetchServerDetails(server);
		}
		return server;
	}
	
	public boolean isValidServer(String clusterName, String serverName) {
		try {
			GlusterServer server = getGlusterServer(clusterName, serverName, false);
			return server != null;
		} catch(Exception e) {
			return false;
		}
	}
	
	public void removeServerFromCluster(String clusterName, String serverName) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}

		if (serverName == null || serverName.isEmpty()) {
			throw new GlusterValidationException("Server name must not be empty!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}

		List<ServerInfo> servers = cluster.getServers();
		if (servers == null || servers.isEmpty() || !containsServer(servers, serverName)) {
			throw new GlusterValidationException("Server [" + serverName + "] is not attached to cluster ["
					+ clusterName + "]!");
		}

		if (servers.size() == 1) {
			// Only one server mapped to the cluster, no "peer detach" required.
			// remove the cached online server for this cluster if present
			clusterService.removeOnlineServer(clusterName);
		} else {
			// get an online server that is not same as the server being removed
			GlusterServer onlineServer = clusterService.getOnlineServer(clusterName, serverName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online server found in cluster [" + clusterName + "]");
			}

			try {
				glusterUtil.removeServer(onlineServer.getName(), serverName);
			} catch (Exception e) {
				// check if online server has gone offline. If yes, try again one more time.
				if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
					// online server has gone offline! try with a different one.
					onlineServer = clusterService.getNewOnlineServer(clusterName, serverName);
					if (onlineServer == null) {
						throw new GlusterRuntimeException("No online server found in cluster [" + clusterName + "]");
					}
					glusterUtil.removeServer(onlineServer.getName(), serverName);
				} else {
					throw new GlusterRuntimeException(e.getMessage());
				}
			}
			
			try {
				if (serverUtil.isServerOnline(new Server(serverName))) {
					volumeService.clearCifsConfiguration(clusterName, onlineServer.getName(), serverName);
				}
			} catch (Exception e1) {
				throw new GlusterRuntimeException(
						"Server removed from cluster, however deleting cifs configuration failed ! [ "
								+ e1.getMessage() + "]");
			}
			if (onlineServer.getName().equals(serverName)) {
				// since the cached server has been removed from the cluster, remove it from the cache
				clusterService.removeOnlineServer(clusterName);
			}

			// since the server is removed from the cluster, it is now available to be added to other clusters.
			// Hence add it back to the discovered servers list.
			discoveredServersResource.addDiscoveredServer(serverName);
		}

		clusterService.unmapServerFromCluster(clusterName, serverName);
	}

	private boolean containsServer(List<ServerInfo> servers, String serverName) {
		for (ServerInfo server : servers) {
			if (server.getName().toUpperCase().equals(serverName.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
}
