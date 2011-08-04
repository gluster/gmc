/**
 * ServerDiscoveryTask.java
 *
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
 */
package com.gluster.storage.management.gateway.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.gateway.data.ClusterInfo;
import com.gluster.storage.management.gateway.data.PersistenceDao;
import com.gluster.storage.management.gateway.data.ServerInfo;
import com.gluster.storage.management.gateway.resources.v1_0.DiscoveredServersResource;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.services.GlusterServerService;
import com.gluster.storage.management.gateway.utils.ServerUtil;

/**
 * Task for syncing server details. This performs two things: <br>
 * 1. Auto-discovery of servers eligible to be added to the Gluster cluster. <br>
 * 2. Syncing of cluster-server mapping with actual servers of the cluster. This mapping can go out of sync if user
 * adds/removes servers manually using the CLI.
 */
@Component
public class ServerSyncTask {
	private static final String SCRIPT_NAME_SFX = "-discover-servers.py";
	
	@Autowired
	private ServerUtil serverUtil;
	
	@Autowired
	private DiscoveredServersResource discoveredServersResource;
	
	@Autowired
	private GlusterServerService glusterServerService;

	@Autowired
	private String discoveryMechanism;
	
	@Autowired
	private ClusterService clusterService;
	
	@Autowired
	private PersistenceDao<ClusterInfo> clusterDao;
	
	private static final Logger logger = Logger.getLogger(ServerSyncTask.class);

	public void perform() {
		discoverServers();
		syncClusterServerMapping();
	}
	
	private void syncClusterServerMapping() {
		List<ClusterInfo> clusters = clusterService.getAllClusters();
		for(ClusterInfo cluster : clusters) {
			try {
				List<ServerInfo> servers = cluster.getServers();
				List<GlusterServer> actualServers = glusterServerService.getGlusterServers(cluster.getName(), false,
						null, null);
				updateRemovedServers(cluster, servers, actualServers);
				updateAddedServers(cluster, servers, actualServers);
			} catch(Exception e) {
				// log error and continue with next cluster
				logger.error("Couldn't sync cluster-server mapping for cluster [" + cluster.getName() + "]!", e);
				continue;
			}
		}
	}

	private void updateAddedServers(ClusterInfo cluster, List<ServerInfo> servers, List<GlusterServer> actualServers) {
		List<String> addedServers = findAddedServers(cluster.getName(), servers, actualServers);
		for(String addedServer : addedServers) {
			clusterService.mapServerToCluster(cluster.getName(), addedServer);
		}
	}

	private void updateRemovedServers(ClusterInfo cluster, List<ServerInfo> servers, List<GlusterServer> actualServers) {
		List<String> removedServers = findRemovedServers(servers, actualServers);
		for(String removedServer : removedServers) {
			clusterService.unmapServerFromCluster(cluster.getName(), removedServer);
		}
	}
	
	private List<String> findRemovedServers(List<ServerInfo> servers, List<GlusterServer> actualServers) {
		List<String> removedServers = new ArrayList<String>();
		
		for(ServerInfo server : servers) {
			if (!GlusterCoreUtil.containsEntityWithName(actualServers, server.getName(), true)) {
				removedServers.add(server.getName());
			}
		}
		return removedServers;
	}
	
	private List<String> findAddedServers(String clusterName, List<ServerInfo> servers, List<GlusterServer> actualServers) {
		List<String> addedServers = new ArrayList<String>();
		for(GlusterServer actualServer : actualServers) {
			if(!serverExists(servers, actualServer.getName())) {
				addedServers.add(actualServer.getName());
			}
		}
		return addedServers;
	}

	private boolean serverExists(List<ServerInfo> servers, String name) {
		for(ServerInfo server : servers) {
			if(server.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private void discoverServers() {
		if(discoveryMechanism.equals(GlusterConstants.NONE)) {
			return;
		}
		
		List<String> serverNameList = new ArrayList<String>();
		
		ProcessResult result = serverUtil.executeGlusterScript(true, discoveryMechanism + SCRIPT_NAME_SFX, new ArrayList<String>());
		if(result.isSuccess()) {
			List<String> existingServers = clusterDao.findBySQL("select name from server_info");
			String serverNames = result.getOutput();
			String[] parts = serverNames.split(CoreConstants.NEWLINE);
			for(String serverName : parts) {
				// The server discovery mechanism will return every server that has not been "peer probed". However we
				// need to filter out those servers that are the "first" server of a new cluster, and hence are still
				// not peer probed.
				if(!existingServers.contains(serverName)) {
					serverNameList.add(serverName);
				}
			}
		}
		
		discoveredServersResource.setDiscoveredServerNames(serverNameList);
	}
}