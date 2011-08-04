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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.gateway.data.ClusterInfo;
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
	
	public void fetchServerDetails(GlusterServer server) {
		try {
			server.setStatus(SERVER_STATUS.ONLINE);
			serverUtil.fetchServerDetails(server);
		} catch (ConnectionException e) {
			server.setStatus(SERVER_STATUS.OFFLINE);
		}
	}
	
	// TODO: Introduce logic to fetch records based on maxCount and previousServerName
	public List<GlusterServer> getGlusterServers(String clusterName, boolean fetchDetails, Integer maxCount,
			String previousServerName) {
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
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}
			server = glusterUtil.getGlusterServer(onlineServer, serverName);
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
}
