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
package org.gluster.storage.management.client;

import static org.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DETAILS;
import static org.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static org.gluster.storage.management.core.constants.RESTConstants.RESOURCE_SERVERS;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.gluster.storage.management.core.constants.RESTConstants;
import org.gluster.storage.management.core.model.GlusterServer;
import org.gluster.storage.management.core.model.ServerStats;
import org.gluster.storage.management.core.response.FsTypeListResponse;
import org.gluster.storage.management.core.response.GlusterServerListResponse;
import org.gluster.storage.management.core.utils.GlusterCoreUtil;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class GlusterServersClient extends AbstractClient {
	
	public GlusterServersClient() {
		super();
	}
	
	public GlusterServersClient(String clusterName) {
		super(clusterName);
	}
	
	public GlusterServersClient(String securityToken, String clusterName) {
		super(securityToken, clusterName);
	}

	@Override
	public String getResourcePath() {
		return RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_SERVERS;
	}

	public List<GlusterServer> getServers() {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.putSingle(QUERY_PARAM_DETAILS, "true");
		List<GlusterServer>  servers = ((GlusterServerListResponse) fetchResource(queryParams, GlusterServerListResponse.class)).getServers();
		for(GlusterServer server : servers) {
			GlusterCoreUtil.updateServerNameOnDevices(server);
		}
		return servers;
	}

	public GlusterServer getGlusterServer(String serverName) {
		GlusterServer server = (GlusterServer) fetchSubResource(serverName, GlusterServer.class);
		GlusterCoreUtil.updateServerNameOnDevices(server);
		return server;
	}

	public URI addServer(String serverName) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_SERVER_NAME, serverName);
		return postRequest(form);
	}
	
	public List<String> getFSTypes(String serverName) {
		FsTypeListResponse fsTypeListResponse = ((FsTypeListResponse) fetchSubResource(serverName + "/" + RESTConstants.RESOURCE_FSTYPES,
				FsTypeListResponse.class));
		return fsTypeListResponse.getFsTypes();
	}

	public URI initializeDisk(String serverName, String diskName, String fsType, String mountPoint) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_FSTYPE, fsType);
		form.add(RESTConstants.FORM_PARAM_MOUNTPOINT, fsType);
		return putRequestURI(serverName + "/" + RESTConstants.RESOURCE_DISKS + "/" + diskName, form);
	}
	
	public void removeServer(String serverName) {
		deleteSubResource(serverName);
	}
	
	public ServerStats getCpuStats(String serverName, String period) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_TYPE, RESTConstants.STATISTICS_TYPE_CPU);
		queryParams.add(RESTConstants.QUERY_PARAM_PERIOD, period);
		return fetchSubResource(serverName + "/" + RESTConstants.RESOURCE_STATISTICS, queryParams, ServerStats.class);
	}
	
	public ServerStats getMemoryStats(String serverName, String period) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_TYPE, RESTConstants.STATISTICS_TYPE_MEMORY);
		queryParams.add(RESTConstants.QUERY_PARAM_PERIOD, period);
		return fetchSubResource(serverName + "/" + RESTConstants.RESOURCE_STATISTICS, queryParams, ServerStats.class);
	}

	public ServerStats getNetworkStats(String serverName, String networkInterface, String period) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_TYPE, RESTConstants.STATISTICS_TYPE_NETWORK);
		queryParams.add(RESTConstants.QUERY_PARAM_PERIOD, period);
		queryParams.add(RESTConstants.QUERY_PARAM_INTERFACE, networkInterface);
		return fetchSubResource(serverName + "/" + RESTConstants.RESOURCE_STATISTICS, queryParams, ServerStats.class);
	}

	public ServerStats getAggregatedCpuStats(String period) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_TYPE, RESTConstants.STATISTICS_TYPE_CPU);
		queryParams.add(RESTConstants.QUERY_PARAM_PERIOD, period);
		return fetchSubResource(RESTConstants.RESOURCE_STATISTICS, queryParams, ServerStats.class);
	}
	
	public ServerStats getAggregatedNetworkStats(String period) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_TYPE, RESTConstants.STATISTICS_TYPE_NETWORK);
		queryParams.add(RESTConstants.QUERY_PARAM_PERIOD, period);
		return fetchSubResource(RESTConstants.RESOURCE_STATISTICS, queryParams, ServerStats.class);
	}
	
	public GlusterServer getGlusterServer(URI uri) {
		GlusterServer server = fetchResource(uri, GlusterServer.class);
		GlusterCoreUtil.updateServerNameOnDevices(server);
		return server;
	}
}
