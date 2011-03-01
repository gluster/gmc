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

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.server.resources.DiscoveredServersResource;
import com.sun.jersey.spi.resource.Singleton;

/**
 * Task for auto-discovery of servers eligible to be added to the Gluster cluster. This task runs periodically and keeps
 * the discovered server list at a common place. The server resource can then pick it and send to client whenever
 * demanded.
 */
@Singleton
@Component
public class ServerDiscoveryTask {
	private static final String ENV_AWS = "aws";
	private static final String ENV_VMWARE = "vmware";
	private static final String ENV_PHYCAL = "physical";
	
	
	@Autowired
	private ServletContext servletContext;

	@Autowired
	private DiscoveredServersResource discoveredServersResource;

	@Autowired
	private String environment;

	public void discoverServers() {
		System.out.println("Starting discovery in [" + environment + "] environment");

		/**
		 * TODO: Flow should be as follows <br>
		 * 1) Get the discovery policy specific for the environment <br>
		 * 2) Execute discovery to get list of auto-discovered server <br>
		 * 3) Set the discovered servers list in the discovered servers resource <br>
		 */

		List<String> discoveredServers = new ArrayList<String>();
		discoveredServers.add("yserver1");

		discoveredServersResource.setDiscoveredServerNames(discoveredServers);
	}
}