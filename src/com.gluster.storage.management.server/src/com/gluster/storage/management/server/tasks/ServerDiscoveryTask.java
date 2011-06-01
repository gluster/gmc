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
package com.gluster.storage.management.server.tasks;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.PersistenceDao;
import com.gluster.storage.management.server.resources.DiscoveredServersResource;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.sun.jersey.spi.resource.Singleton;

/**
 * Task for auto-discovery of servers eligible to be added to the Gluster cluster. This task runs periodically and keeps
 * the discovered server list at a common place. The server resource can then pick it and send to client whenever
 * demanded.
 */
@Singleton
@Component
public class ServerDiscoveryTask {
	private static final String SCRIPT_NAME_SFX = "-discover-servers.py";
	
	@Autowired
	private ServerUtil serverUtil;
	
	@Autowired
	private DiscoveredServersResource discoveredServersResource;

	@Autowired
	private String environment;
	
	@Autowired
	private PersistenceDao<ClusterInfo> clusterDao;

	public void discoverServers() {
		List<String> serverNameList = new ArrayList<String>();
		
		ProcessResult result = serverUtil.executeGlusterScript(true, environment + SCRIPT_NAME_SFX, new ArrayList<String>());
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