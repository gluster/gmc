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

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;

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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.response.ClusterNameListResponse;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.services.ClusterService;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

/**
 *
 */
@Component
@Singleton
@Path(RESOURCE_PATH_CLUSTERS)
public class ClustersResource extends AbstractResource {
	@InjectParam
	private ClusterService clusterService;
	private static final Logger logger = Logger.getLogger(ClustersResource.class);
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public ClusterNameListResponse getClusters() {
		List<ClusterInfo> clusters = clusterService.getAllClusters();
		List<String> clusterList = new ArrayList<String>();
		for (ClusterInfo cluster : clusters) {
			clusterList.add(cluster.getName());
		}
		return new ClusterNameListResponse(clusterList);
	}

	@POST
	public Response createCluster(@FormParam(FORM_PARAM_CLUSTER_NAME) String clusterName) {
		if(clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Parameter [" + FORM_PARAM_CLUSTER_NAME + "] is missing in request!");
		}
		
		if(clusterService.getCluster(clusterName) != null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] already exists!");
		}
		
		clusterService.createCluster(clusterName);
		return createdResponse(clusterName);
	}
	
	@PUT
	public Response registerCluster(@FormParam(FORM_PARAM_CLUSTER_NAME) String clusterName,
			@FormParam(FORM_PARAM_SERVER_NAME) String knownServer) {
		if(clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Parameter [" + FORM_PARAM_CLUSTER_NAME + "] is missing in request!");
		}
		
		if(knownServer == null || knownServer.isEmpty()) {
			throw new GlusterValidationException("Parameter [" + FORM_PARAM_SERVER_NAME + "] is missing in request!");
		}
		
		if(clusterService.getCluster(clusterName) != null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] already exists!");
		}
		
		ClusterInfo mappedCluster = clusterService.getClusterForServer(knownServer);
		if(mappedCluster != null) {
			throw new GlusterValidationException("Server [" + knownServer + "] is already present in cluster ["
					+ mappedCluster.getName() + "]!");
		}
		
		clusterService.registerCluster(clusterName, knownServer);
		return noContentResponse(clusterName);
	}

	@Path("{" + PATH_PARAM_CLUSTER_NAME + "}")
	@DELETE
	public Response unregisterCluster(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		if(clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Parameter [" + FORM_PARAM_CLUSTER_NAME + "] is missing in request!");
		}
		
		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if(cluster == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] does not exist!");
		}
		
		clusterService.unregisterCluster(cluster);
		return noContentResponse();
	}
}
