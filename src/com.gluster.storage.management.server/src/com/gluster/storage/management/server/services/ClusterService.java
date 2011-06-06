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

import java.util.List;

import javax.persistence.EntityTransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.PersistenceDao;
import com.gluster.storage.management.server.data.ServerInfo;

/**
 * Service class for functionality related to clusters
 */
@Component
public class ClusterService {
	@Autowired
	private PersistenceDao<ClusterInfo> clusterDao;
	
	public ClusterInfo getCluster(String clusterName) {
		List<ClusterInfo> clusters = clusterDao.findBy("name = ?1", clusterName);
		if(clusters.size() == 0) {
			return null;
		}

		return clusters.get(0);
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
