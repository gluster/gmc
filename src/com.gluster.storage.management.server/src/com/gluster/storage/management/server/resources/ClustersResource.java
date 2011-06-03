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
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GlusterServerResponse;
import com.gluster.storage.management.core.response.StringListResponse;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.PersistenceDao;
import com.gluster.storage.management.server.data.ServerInfo;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

/**
 *
 */
@Component
@Singleton
@Path(RESOURCE_PATH_CLUSTERS)
public class ClustersResource {

	@InjectParam
	private PersistenceDao clusterDao;
	
	@InjectParam
	private GlusterServersResource glusterServersResource;
	
	@InjectParam
	private GlusterUtil glusterUtil;
	
	@Autowired
	private SshUtil sshUtil;

	public void setClusterDao(PersistenceDao clusterDao) {
		this.clusterDao = clusterDao;
	}

	public PersistenceDao getClusterDao() {
		return clusterDao;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public StringListResponse getClusters() {
		List<ClusterInfo> clusters = getClusterDao().findAll();
		List<String> clusterList = new ArrayList<String>();
		for (ClusterInfo cluster : clusters) {
			clusterList.add(cluster.getName());
		}
		return new StringListResponse(clusterList);
	}

	@POST
	@Produces(MediaType.TEXT_XML)
	public Status createCluster(@FormParam(FORM_PARAM_CLUSTER_NAME) String clusterName) {
		EntityTransaction txn = clusterDao.startTransaction();
		ClusterInfo cluster = new ClusterInfo();
		cluster.setName(clusterName);

		try {
			clusterDao.save(cluster);
			txn.commit();
			return Status.STATUS_SUCCESS;
		} catch (Exception e) {
			txn.rollback();
			return new Status(Status.STATUS_CODE_FAILURE, "Exception while trying to save cluster [" + clusterName
					+ "]: [" + e.getMessage() + "]");
		}
	}
	
	
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Status registerCluster(@FormParam(FORM_PARAM_CLUSTER_NAME) String clusterName,
			@FormParam(FORM_PARAM_SERVER_NAME) String knownServer) {
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
			return Status.STATUS_SUCCESS;
		} catch(Exception e) {
			txn.rollback();
			return new Status(e);
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

	@SuppressWarnings("unchecked")
	@Path("{" + PATH_PARAM_CLUSTER_NAME + "}")
	@DELETE
	@Produces(MediaType.TEXT_XML)
	public Status deleteCluster(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		List<ClusterInfo> clusters = clusterDao.findBy("name = ?1", clusterName);
		if (clusters == null || clusters.size() == 0) {
			return new Status(Status.STATUS_CODE_FAILURE, "Cluster [" + clusterName + "] doesn't exist!");
		}

		ClusterInfo cluster = clusters.get(0);
		EntityTransaction txn = clusterDao.startTransaction();
		try {
			clusterDao.delete(cluster);
			txn.commit();
			return Status.STATUS_SUCCESS;
		} catch (Exception e) {
			txn.rollback();
			return new Status(Status.STATUS_CODE_FAILURE, "Exception while trying to delete cluster [" + clusterName
					+ "]: [" + e.getMessage() + "]");
		}
	}
}
