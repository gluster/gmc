/**
 * AbstractServersResource.java
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
package com.gluster.storage.management.server.resources;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.NetworkInterface;
import com.gluster.storage.management.core.model.Server;

/**
 * Abstract resource class for servers. Abstracts basic server related functionality like "get server details".
 */
public class AbstractServersResource {
	// TODO: Used for generating dummy ip address. To be removed after implementing actual logic for fetching server
	// details
	private static int ipCount = 1;

	/**
	 * Fetch details of the given server. The server name must be populated in the object before calling this method.
	 * 
	 * @param server
	 *            Server whose details are to be fetched
	 */
	protected void fetchServerDetails(Server server) {
		String serverName = server.getName();

		// TODO: Fetch the server details and populate in the object.
		// For now, populating dummy data.
		populateDummyData(server);
	}

	/**
	 * @param server
	 */
	private void populateDummyData(Server server) {
		server.setNumOfCPUs((int) (Math.ceil(Math.random() * 8)));
		server.setCpuUsage(Math.random() * 100);
		server.setTotalMemory(Math.ceil(Math.random() * 8));
		server.setMemoryInUse(Math.random() * server.getTotalMemory());
		addDummyDisks(server);
		addDummyNetworkInterfaces(server, (int) Math.ceil(Math.random() * 4));
	}

	private void addDummyNetworkInterfaces(Server server, int interfaceCount) {
		for (int i = 0; i < interfaceCount; i++) {
			server.addNetworkInterface(new NetworkInterface("eth" + i, server, "192.168.1." + ipCount++,
					"255.255.255.0", "192.168.1.1"));
		}
	}

	/**
	 * @param server
	 */
	private void addDummyDisks(Server server) {
		double dummyDiskSpace = Math.random() * 500;
		server.addDisk(new Disk(server, "sda", "/export/sda", dummyDiskSpace, Math.random() * dummyDiskSpace, Disk.DISK_STATUS.READY));
		dummyDiskSpace = Math.random() * 500;
		server.addDisk(new Disk(server, "sdb", "/export/sdb", dummyDiskSpace, Math.random() * dummyDiskSpace, Disk.DISK_STATUS.READY));
		dummyDiskSpace = Math.random() * 500;
		server.addDisk(new Disk(server, "sdc", "/export/sdc", dummyDiskSpace, Math.random() * dummyDiskSpace, Disk.DISK_STATUS.READY));
	}
}
