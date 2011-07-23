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
package com.gluster.storage.management.server.services;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.utils.LRUCache;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.PersistenceDao;
import com.gluster.storage.management.server.data.ServerInfo;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.gluster.storage.management.server.utils.SshUtil;

/**
 * Service class for functionality related to clusters
 */
@Component
public class ClusterService {
	@Autowired
	private PersistenceDao<ClusterInfo> clusterDao;
	
	@Autowired
	private PersistenceDao<ServerInfo> serverDao;

	@Autowired
	private GlusterUtil glusterUtil;

	@Autowired
	private SshUtil sshUtil;
	
	@Autowired
	private ServerUtil serverUtil;

	private LRUCache<String, GlusterServer> onlineServerCache = new LRUCache<String, GlusterServer>(3);
	
	private static final Logger logger = Logger.getLogger(ClusterService.class);
	
	public void addOnlineServer(String clusterName, GlusterServer server) {
		onlineServerCache.put(clusterName, server);
	}
	
	public void removeOnlineServer(String clusterName) {
		onlineServerCache.remove(clusterName);
	}
	
	// uses cache
	public GlusterServer getOnlineServer(String clusterName, String exceptServerName) {
		GlusterServer server = onlineServerCache.get(clusterName);
		if (server != null && !server.getName().equals(exceptServerName)) {
			return server;
		}

		return getNewOnlineServer(clusterName, exceptServerName);
	}

	public GlusterServer getNewOnlineServer(String clusterName) {
		return getNewOnlineServer(clusterName, "");
	}
	
	public GlusterServer getOnlineServer(String clusterName) {
		return getOnlineServer(clusterName, "");
	}

	// Doesn't use cache
	public GlusterServer getNewOnlineServer(String clusterName, String exceptServerName) {
		ClusterInfo cluster = getCluster(clusterName);
		if (cluster == null) {
			throw new GlusterRuntimeException("Cluster [" + clusterName + "] is not found!");
		}

		for (ServerInfo serverInfo : cluster.getServers()) {
			GlusterServer server = new GlusterServer(serverInfo.getName());
			try {
				serverUtil.fetchServerDetails(server); // Online status come with server details
				// server is online. add it to cache and return
				if (server.isOnline() && !server.getName().equals(exceptServerName)) {
					addOnlineServer(clusterName, server);
					return server;
				}
			} catch (ConnectionException e) {
				// server is offline. continue checking next one.
				continue;
			}
		}

		// no online server found.
		throw new GlusterRuntimeException("No online server found in cluster [" + clusterName + "]");
	}
	
	public List<ClusterInfo> getAllClusters() {
		return clusterDao.findAll();
	}
	
	public ClusterInfo getCluster(String clusterName) {
		List<ClusterInfo> clusters = clusterDao.findBy("UPPER(name) = ?1", clusterName.toUpperCase());
		if(clusters.size() == 0) {
			return null;
		}

		return clusters.get(0);
	}
	
	public ClusterInfo getClusterForServer(String serverName) {
		List<ServerInfo> servers = serverDao.findBy("UPPER(name) = ?1", serverName.toUpperCase());
		if(servers.size() == 0) {
			return null;
		}

		return servers.get(0).getCluster();
	}
	
	public void createCluster(String clusterName) {
		EntityTransaction txn = clusterDao.startTransaction();
		ClusterInfo cluster = new ClusterInfo();
		cluster.setName(clusterName);

		try {
			clusterDao.save(cluster);
			txn.commit();
		} catch (RuntimeException e) {
			txn.rollback();
			logger.error("Exception while trying to save cluster [" + clusterName + "] : [" + e.getMessage() + "]", e);
			throw e;
		}
	}
	
	public void registerCluster(String clusterName, String knownServer) {
		EntityTransaction txn = clusterDao.startTransaction();
		ClusterInfo cluster = new ClusterInfo();
		cluster.setName(clusterName);
		
		GlusterServer server = new GlusterServer(knownServer);
		try {
			List<GlusterServer> glusterServers = glusterUtil.getGlusterServers(server);
			List<ServerInfo> servers = new ArrayList<ServerInfo>();
			for(GlusterServer glusterServer : glusterServers) {
				String serverName = glusterServer.getName();
				
				checkAndSetupPublicKey(serverName);

				ServerInfo serverInfo = new ServerInfo(serverName);
				serverInfo.setCluster(cluster);
				clusterDao.save(serverInfo);
				servers.add(serverInfo);
			}
			cluster.setServers(servers);
			clusterDao.save(cluster);
			txn.commit();
		} catch(RuntimeException e) {
			logger.error("Error in registering cluster [" + clusterName + "] : " + e.getMessage(), e);
			txn.rollback();
			logger.error("Error in registering cluster [" + clusterName + "] : " + e.getMessage(), e);
			throw e;
		}
	}
	
	private void checkAndSetupPublicKey(String serverName) {
		if(sshUtil.isPublicKeyInstalled(serverName)) {
			return;
		}
		
		if(!sshUtil.hasDefaultPassword(serverName)) {
			// public key not installed, default password doesn't work. can't install public key
			throw new GlusterRuntimeException(
					"Gluster Management Gateway uses the default password to set up keys on the server."
							+ CoreConstants.NEWLINE + "However it seems that the password on server [" + serverName
							+ "] has been changed manually." + CoreConstants.NEWLINE
							+ "Please reset it back to the standard default password and try again.");
		}
		
		// install public key (this will also disable password based ssh login)
		sshUtil.installPublicKey(serverName);
	}
	
	public void unregisterCluster(String clusterName) {
		ClusterInfo cluster = getCluster(clusterName);
		
		if (cluster == null) {
			throw new GlusterRuntimeException("Cluster [" + clusterName + "] doesn't exist!");
		}

		unregisterCluster(cluster);
	}

	public void unregisterCluster(ClusterInfo cluster) {
		EntityTransaction txn = clusterDao.startTransaction();
		try {
			for(ServerInfo server : cluster.getServers()) {
				clusterDao.delete(server);
			}
			cluster.getServers().clear();
			clusterDao.update(cluster);
			clusterDao.delete(cluster);
			txn.commit();
		} catch (RuntimeException e) {
			logger.error("Error in unregistering cluster [" + cluster.getName() + "] : " + e.getMessage(), e);
			txn.rollback();
			throw e;
		}
	}
	
	public void mapServerToCluster(String clusterName, String serverName) {
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
	
	public void unmapServerFromCluster(String clusterName, String serverName) {
		EntityTransaction txn = clusterDao.startTransaction();
		ClusterInfo cluster = getCluster(clusterName);
		List<ServerInfo> servers = cluster.getServers();
		for(ServerInfo server : servers) {
			if(server.getName().equals(serverName)) {
				servers.remove(server);
				clusterDao.delete(server);
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
}
