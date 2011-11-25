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
package org.gluster.storage.management.gateway.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.model.Server;
import org.gluster.storage.management.core.utils.ProcessUtil;
import org.gluster.storage.management.gateway.utils.ServerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 *
 */
@Component
public class DiscoveredServerService {
	@Autowired
	protected ServerUtil serverUtil;

	private List<String> discoveredServerNames = new ArrayList<String>();
	private static final Logger logger = Logger.getLogger(DiscoveredServerService.class);

	public List<Server> getDiscoveredServerDetails() {
		try {
			List<Server> discoveredServers = Collections.synchronizedList(new ArrayList<Server>());
			List<Thread> threads = createThreads(discoveredServers);
			ProcessUtil.waitForThreads(threads);
			return discoveredServers;
		} catch (Exception e) {
			String errMsg = "Exception while fetching details of discovered servers! Error: [" + e.getMessage() + "]";
			logger.error(errMsg, e);
			throw new GlusterRuntimeException(errMsg, e);
		}
	}

	/**
	 * Creates threads that will run in parallel and fetch details of all discovered servers
	 * @param discoveredServers The list to be populated with details of discovered servers
	 * @return
	 * @throws InterruptedException
	 */
	private List<Thread> createThreads(List<Server> discoveredServers) throws InterruptedException {
		List<String> discoveredServerNames = getDiscoveredServerNames();
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = discoveredServerNames.size() - 1; i >= 0; i--) {
			Thread thread = new DiscoveredServerDetailsThread(discoveredServers, discoveredServerNames.get(i));
			threads.add(thread);
			thread.start();
			if (i >= 5 && i % 5 == 0) {
				// After every 5 servers, wait for 1 second so that we don't end up with too many running threads
				Thread.sleep(1000);
			}
		}
		return threads;
	}
	
	public List<String> getDiscoveredServerNames() {
		return discoveredServerNames;
	}
	
	public void setDiscoveredServerNames(List<String> discoveredServerNames) {
		synchronized (discoveredServerNames) {
			this.discoveredServerNames = discoveredServerNames;
		}
	}
	
	public void removeDiscoveredServer(String serverName) {
		discoveredServerNames.remove(serverName);
	}
	
	public void addDiscoveredServer(String serverName) {
		discoveredServerNames.add(serverName);
	}

	public Server getDiscoveredServer(String serverName) {
		Server server = new Server(serverName);
		serverUtil.fetchServerDetails(server);
		return server;
	}
	
	public class DiscoveredServerDetailsThread extends Thread {
		private List<Server> servers;
		private String serverName;
		private final Logger logger = Logger.getLogger(DiscoveredServerDetailsThread.class);

		/**
		 * Private constructor called on each thread
		 * @param servers The list to be populated with fetched server details
		 * @param serverName Name of the server whose details should be fetched by this thread
		 */
		private DiscoveredServerDetailsThread(List<Server> servers, String serverName) {
			this.servers = servers;
			this.serverName = serverName;
		}

		@Override
		public void run() {
			try {
				logger.info("fetching details of discovered server [" + serverName + "] - start");
				servers.add(getDiscoveredServer(serverName));
				logger.info("fetching details of discovered server [" + serverName + "] - end");
			} catch(Exception e) {
				logger.warn("fetching details of discovered server [" + serverName + "] - error", e);
				// eat the exception as we can't consider this server as a discovered server any more
			}
		}
	}
}
