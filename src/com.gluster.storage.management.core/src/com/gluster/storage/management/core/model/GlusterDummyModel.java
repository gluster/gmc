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
package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;

public class GlusterDummyModel {
	// private Server discoveredServer1, discoveredServer2, discoveredServer3, discoveredServer4, discoveredServer5;
	private GlusterServer server1, server2, server3, server4, server5;
	private Volume volume1, volume2, volume3, volume4, volume5;
	private Disk s1da, s1db, s2da, s2db, s2dc, s2dd, s3da, s4da, s5da, s5db;
	private static List<LogMessage> logMessages = new ArrayList<LogMessage>();
	private static GlusterDummyModel instance = new GlusterDummyModel();
	private GlusterDataModel model;

	private GlusterDummyModel() {
		model = initializeModel();
	}

	public GlusterDataModel getModel() {
		return model;
	}

	public static GlusterDummyModel getInstance() {
		return instance;
	}

	// Renamed preferredInterfaceName to interfaceName
	private GlusterServer addGlusterServer(List<GlusterServer> servers, Entity parent, String name,
			SERVER_STATUS status, String interfaceName, int numOfCPUs, double cpuUsage, double totalMemory,
			double memoryInUse) {
		GlusterServer glusterServer = new GlusterServer(name, parent, status, numOfCPUs, cpuUsage, totalMemory,
				memoryInUse);
		NetworkInterface networkInterface = addNetworkInterface(glusterServer, interfaceName);	// Renamed preferredInterfaceName to interfaceName
		// glusterServer.setPreferredNetworkInterface(networkInterface);

		servers.add(glusterServer);
		return glusterServer;
	}

	private NetworkInterface addNetworkInterface(Server server, String interfaceName) {
		NetworkInterface networkInterface = new NetworkInterface(interfaceName, server, "192.168.1."
				+ Math.round(Math.random() * 255), "255.255.255.0", "192.168.1.1");
		server.setNetworkInterfaces(Arrays.asList(new NetworkInterface[] { networkInterface }));
		return networkInterface;
	}

	private void addDiscoveredServer(List<Server> servers, Entity parent, String name, int numOfCPUs, double cpuUsage,
			double totalMemory, double memoryInUse, double totalDiskSpace, double diskSpaceInUse) {
		Server server = new Server(name, parent, numOfCPUs, cpuUsage, totalMemory, memoryInUse);
		server.addDisk(new Disk(server, "sda", "/export/md0", totalDiskSpace, diskSpaceInUse, DISK_STATUS.READY));
		addNetworkInterface(server, "eth0");

		servers.add(server);
	}

	private GlusterDataModel initializeModel() {
		// Create the dummy data model for demo
		GlusterDataModel model = new GlusterDataModel("Clusters");
		Cluster cluster = new Cluster("Home", model);

		initializeGlusterServers(cluster);
		initializeVolumes(cluster);
		initializeAutoDiscoveredServers(cluster);
		initializeDisks();
		addDisksToServers();
		addDisksToVolumes();
		addVolumeOptions();

		createDummyLogMessages();

		model.addCluster(cluster);
		return model;
	}

	private void addVolumeOptions() {
		for (Volume vol : new Volume[] { volume1, volume2, volume3, volume4, volume5 }) {
			for (int i = 1; i <= 5; i++) {
				String key = vol.getName() + "key" + i;
				String value = vol.getName() + "value" + i;
				vol.setOption(key, value);
			}
		}
	}

	private Volume addVolume(List<Volume> volumes, String name, Cluster cluster, VOLUME_TYPE volumeType,
			TRANSPORT_TYPE transportType, VOLUME_STATUS status) {
		Volume volume = new Volume(name, cluster, volumeType, transportType, status);
		volumes.add(volume);

		return volume;
	}

	private void initializeVolumes(Cluster cluster) {
		List<Volume> volumes = new ArrayList<Volume>();

		volume1 = addVolume(volumes, "Volume1", cluster, VOLUME_TYPE.PLAIN_DISTRIBUTE, TRANSPORT_TYPE.ETHERNET,
				VOLUME_STATUS.ONLINE);

		volume2 = addVolume(volumes, "Volume2", cluster, VOLUME_TYPE.PLAIN_DISTRIBUTE, TRANSPORT_TYPE.ETHERNET,
				VOLUME_STATUS.ONLINE);

		volume3 = addVolume(volumes, "Volume3", cluster, VOLUME_TYPE.DISTRIBUTED_MIRROR, TRANSPORT_TYPE.ETHERNET,
				VOLUME_STATUS.OFFLINE);
		volume3.setReplicaCount(2);

		volume4 = addVolume(volumes, "Volume4", cluster, VOLUME_TYPE.PLAIN_DISTRIBUTE, TRANSPORT_TYPE.ETHERNET,
				VOLUME_STATUS.ONLINE);

		volume5 = addVolume(volumes, "Volume5", cluster, VOLUME_TYPE.DISTRIBUTED_STRIPE, TRANSPORT_TYPE.INFINIBAND,
				VOLUME_STATUS.OFFLINE);
		volume5.setStripeCount(3);

		cluster.setVolumes(volumes);
	}

	private void initializeDisks() {
		s1da = new Disk(server1, "sda", "/export/md0", 100d, 80d, DISK_STATUS.READY);
		s1db = new Disk(server1, "sdb", "/export/md1", 100d, 67.83, DISK_STATUS.READY);

		s2da = new Disk(server2, "sda", "/export/md0", 200d, 157.12, DISK_STATUS.READY);
		s2db = new Disk(server2, "sdb", "/export/md1", 200d, 182.27, DISK_STATUS.READY);
		s2dc = new Disk(server2, "sdc", "/export/md0", 200d, -1d, DISK_STATUS.UNINITIALIZED);
		s2dd = new Disk(server2, "sdd", "/export/md1", 200d, 124.89, DISK_STATUS.READY);

		// disk name unavailable since server is offline
		s3da = new Disk(server3, "NA", "NA", -1d, -1d, DISK_STATUS.OFFLINE); // disk name unavailable since server is offline

		s4da = new Disk(server4, "sda", "/export/md0", 100d, 85.39, DISK_STATUS.READY);

		s5da = new Disk(server5, "sda", "/export/md1", 100d, 92.83, DISK_STATUS.READY);
		s5db = new Disk(server5, "sdb", "/export/md1", 200d, 185.69, DISK_STATUS.READY);
	}

	private void addDisksToServers() {
		server1.addDisk(s1da);
		server1.addDisk(s1db);

		server2.addDisk(s2da);
		server2.addDisk(s2db);
		server2.addDisk(s2dc);
		server2.addDisk(s2dd);

		// server3.addDisk(s3da);

		server4.addDisk(s4da);

		server5.addDisk(s5da);
		server5.addDisk(s5db);
	}

	private void addDisksToVolumes() {
		volume1.addDisk("server1:sda");

		volume2.addDisk("server2:sda");
		volume2.addDisk("server1:sdb");
		volume2.addDisk("server3:sda");
		volume2.addDisk("server4:sda");

		volume3.addDisk("server2:sdb");
		volume3.addDisk("server4:sda");
		volume3.addDisk("server5:sda");

		volume4.addDisk("server1:sda");
		volume4.addDisk("server3:sda");
		volume4.addDisk("server4:sda");
		volume4.addDisk("server5:sdb");

		volume5.addDisk("server2:sda");
		volume5.addDisk("server5:sdb");
	}

	private void initializeGlusterServers(Cluster cluster) {
		List<GlusterServer> servers = new ArrayList<GlusterServer>();
		server1 = addGlusterServer(servers, cluster, "Server1", SERVER_STATUS.ONLINE, "eth0", 4, 56.3, 16, 8.4);
		server2 = addGlusterServer(servers, cluster, "Server2", SERVER_STATUS.ONLINE, "eth1", 8, 41.92, 32, 18.76);
		server3 = addGlusterServer(servers, cluster, "Server3", SERVER_STATUS.OFFLINE, "eth0", -1, -1, -1, -1);
		server4 = addGlusterServer(servers, cluster, "Server4", SERVER_STATUS.ONLINE, "eth0", 1, 92.83, 4, 3.18);
		server5 = addGlusterServer(servers, cluster, "Server5", SERVER_STATUS.ONLINE, "inf0", 2, 87.24, 8, 7.23);

		cluster.setServers(servers);
	}

	private void initializeAutoDiscoveredServers(Cluster cluster) {
		List<Server> servers = new ArrayList<Server>();
		addDiscoveredServer(servers, cluster, "ADServer1", 4, 56.3, 16, 8.4, 200, 147.83);
		addDiscoveredServer(servers, cluster, "ADServer2", 8, 41.92, 32, 18.76, 800, 464.28);
		addDiscoveredServer(servers, cluster, "ADServer3", 2, 84.28, 2, 1.41, 120, 69.93);
		addDiscoveredServer(servers, cluster, "ADServer4", 1, 92.83, 4, 3.18, 100, 85.39);
		addDiscoveredServer(servers, cluster, "ADServer5", 2, 87.24, 8, 7.23, 250, 238.52);
		cluster.setAutoDiscoveredServers(servers);
	}

	private void addMessages(List<LogMessage> messages, Disk disk, String severity, int count) {
		for (int i = 1; i <= count; i++) {
			String message = severity + "message" + i;
			messages.add(new LogMessage(new Date(), disk.getQualifiedName(), severity, message));
		}
	}

	private void addMessagesForDisk(List<LogMessage> logMessages, Disk disk) {
		addMessages(logMessages, disk, "SEVERE", 5);
		addMessages(logMessages, disk, "WARNING", 5);
		addMessages(logMessages, disk, "DEBUG", 5);
		addMessages(logMessages, disk, "INFO", 5);
	}

	public List<LogMessage> createDummyLogMessages() {
		addMessagesForDisk(logMessages, s1da);
		addMessagesForDisk(logMessages, s1db);
		addMessagesForDisk(logMessages, s2da);
		addMessagesForDisk(logMessages, s2db);
		addMessagesForDisk(logMessages, s2dc);
		addMessagesForDisk(logMessages, s2dd);
		addMessagesForDisk(logMessages, s4da);
		addMessagesForDisk(logMessages, s5da);
		addMessagesForDisk(logMessages, s5db);
		return logMessages;
	}

	public static List<LogMessage> getDummyLogMessages() {
		return logMessages;
	}
	
	public Disk getVolumeDisk(String volumeDisk) {
		List<Disk> allDisks = getReadyDisksOfAllServers();
		String brickInfo[] = volumeDisk.split(":");
		for( Disk disk: allDisks) {
			if (disk.getServerName() == brickInfo[0] && disk.getName() == brickInfo[1]) {
				return disk;
			}
		}
		return null;
	}
	
	public List<Disk> getReadyDisksOfVolume(Volume volume) {
//		List<Disk> disks = new ArrayList<Disk>();
//		for (Disk disk : volume.getDisks()) {
//			if (disk.isReady()) {
//				disks.add(disk);
//			}
//		}
//		return disks;
		Disk disk = null;
		List<Disk> volumeDisks = new ArrayList<Disk>();
		for (String volumeDisk : volume.getDisks() ) {
			disk = getVolumeDisk(volumeDisk);
			if (disk != null && disk.isReady()) {
				volumeDisks.add(disk);
			}
		}
		return volumeDisks;
	}

	public List<Disk> getReadyDisksOfAllVolumes() {
		List<Disk> disks = new ArrayList<Disk>();
		for (Volume volume : ((Cluster) model.getChildren().get(0)).getVolumes()) {
			disks.addAll(getReadyDisksOfVolume(volume));
		}
		return disks;
	}

	public List<Disk> getReadyDisksOfAllServers() {
		return getReadyDisksOfAllServersExcluding(new ArrayList<Disk>());
	}

	public List<Disk> getReadyDisksOfAllServersExcluding(List<Disk> excludeDisks) {
		List<Disk> disks = new ArrayList<Disk>();
		
		for (Server server : ((Cluster) model.getChildren().get(0)).getServers()) {
			for (Disk disk : server.getDisks()) {
				if (disk.isReady() && !excludeDisks.contains(disk)) {
					disks.add(disk);
				}
			}
		}
		return disks;
	}
}
