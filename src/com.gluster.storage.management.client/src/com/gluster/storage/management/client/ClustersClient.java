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
package com.gluster.storage.management.client;

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_SERVER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;

import java.util.List;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.ClusterNameListResponse;
import com.sun.jersey.api.representation.Form;

/**
 *
 */
public class ClustersClient extends AbstractClient {
	public ClustersClient() {
		super();
	}
	
	public ClustersClient(String securityToken) {
		super();
		setSecurityToken(securityToken);
	}
	
	/* (non-Javadoc)
	 * @see com.gluster.storage.management.client.AbstractClient#getResourcePath()
	 */
	@Override
	public String getResourcePath() {
		return RESOURCE_PATH_CLUSTERS;
	}
	
	public List<String> getClusterNames() {
		return ((ClusterNameListResponse)fetchResource(ClusterNameListResponse.class)).getClusterNames();
	}
	
	public void createCluster(String clusterName) {
		Form form = new Form();
		form.add(FORM_PARAM_CLUSTER_NAME, clusterName);

		postRequest(form);
	}
	
	public void registerCluster(String clusterName, String knownServer) {
		Form form = new Form();
		form.add(FORM_PARAM_CLUSTER_NAME, clusterName);
		form.add(FORM_PARAM_SERVER_NAME, knownServer);
		putRequest(form);
	}
	
	public void deleteCluster(String clusterName) {
		deleteSubResource(clusterName);
	}
	
	public static void main(String args[]) {
		UsersClient usersClient = new UsersClient();
		Status authStatus = usersClient.authenticate("gluster", "gluster");
		if (authStatus.isSuccess()) {
			ClustersClient client = new ClustersClient();
			client.setSecurityToken(usersClient.getSecurityToken());
			System.out.println(client.getClusterNames());
			try {
				client.createCluster("test1");
			} catch(GlusterRuntimeException e) {
				System.out.println(e.getMessage());
			}
			
			System.out.println(client.getClusterNames());
			
			try {
				client.deleteCluster("test1");
			} catch (GlusterRuntimeException e) {
				System.out.println(e.getMessage());
			}
			System.out.println(client.getClusterNames());
		} else {
			System.out.println("authentication failed: " + authStatus);
		}
	}
}
