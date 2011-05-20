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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Event.EVENT_TYPE;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.LogMessage;
import com.gluster.storage.management.core.model.NetworkInterface;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.model.VolumeOptionInfo;
import com.gluster.storage.management.core.response.RunningTaskListResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;

public class GlusterDataModelManager {
	// private Server discoveredServer1, discoveredServer2, discoveredServer3,
	// discoveredServer4, discoveredServer5;
	private GlusterServer server1, server2, server3, server4, server5;
	private Volume volume1, volume2, volume3, volume4, volume5;
	private Disk s1da, s1db, s2da, s2db, s2dc, s2dd, s3da, s4da, s5da, s5db;
	private static List<LogMessage> logMessages = new ArrayList<LogMessage>();
	private static GlusterDataModelManager instance = new GlusterDataModelManager();
	private GlusterDataModel model;
	private String securityToken;
	private String serverName;
	private List<ClusterListener> listeners = new ArrayList<ClusterListener>();
	private List<VolumeOptionInfo> volumeOptionsDefaults;

	private GlusterDataModelManager() {
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public GlusterDataModel getModel() {
		return model;
	}

	public static GlusterDataModelManager getInstance() {
		return instance;
	}

	// Renamed preferredInterfaceName to interfaceName
	private GlusterServer addGlusterServer(List<GlusterServer> servers, Entity parent, String name,
			SERVER_STATUS status, String interfaceName, int numOfCPUs, double cpuUsage, double totalMemory,
			double memoryInUse) {
		GlusterServer glusterServer = new GlusterServer(name, parent, status, numOfCPUs, cpuUsage, totalMemory,
				memoryInUse);
		NetworkInterface networkInterface = addNetworkInterface(glusterServer, interfaceName); // Renamed
																								// preferredInterfaceName
																								// to
																								// interfaceName
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
		server.addDisk(new Disk(server, "sda", "/export/md1", totalDiskSpace, diskSpaceInUse, DISK_STATUS.READY));
		addNetworkInterface(server, "eth0");

		servers.add(server);
	}

	public void initializeModel(String securityToken, String knownServer) {
		model = new GlusterDataModel("Gluster Data Model");
		setSecurityToken(securityToken);

		Cluster cluster = new Cluster("Home", model);

		initializeGlusterServers(cluster, knownServer);
		initializeVolumes(cluster);

		initializeAutoDiscoveredServers(cluster);
		initializeDisks();
		// addDisksToVolumes();
		// addVolumeOptions();

		createDummyLogMessages();

		initializeRunningTasks(cluster);
		initializeAlerts(cluster);
		initializeVolumeOptionsDefaults();

		model.addCluster(cluster);
	}

	private void initializeVolumeOptionsDefaults() {
		VolumeOptionInfoListResponse response = new VolumesClient(getSecurityToken()).getVolumeOptionsDefaults();
		if (!response.getStatus().isSuccess()) {
			throw new GlusterRuntimeException("Error fetching volume option defaults: ["
					+ response.getStatus().getMessage() + "]");
		}
		this.volumeOptionsDefaults = response.getOptions();
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

	public Volume addVolume(List<Volume> volumes, String name, Cluster cluster, VOLUME_TYPE volumeType,
			TRANSPORT_TYPE transportType, VOLUME_STATUS status) {
		Volume volume = new Volume(name, cluster, volumeType, transportType, status);
		volumes.add(volume);

		return volume;
	}

	private void initializeVolumes(Cluster cluster) {
		VolumesClient volumeClient = new VolumesClient(securityToken);
		VolumeListResponse response = volumeClient.getAllVolumes();
		if (!response.getStatus().isSuccess()) {
			throw new GlusterRuntimeException("Error fetching volume list: [" + response.getStatus() + "]");
		}
		cluster.setVolumes(response.getVolumes());
	}

	private void initializeDisks() {
		s1da = new Disk(server1, "sda", "/export/md0", 100d, 80d, DISK_STATUS.READY);
		s1db = new Disk(server1, "sdb", "/export/md1", 100d, 67.83, DISK_STATUS.READY);

		s2da = new Disk(server2, "sda", "/export/md0", 200d, 157.12, DISK_STATUS.READY);
		s2db = new Disk(server2, "sdb", "/export/md1", 200d, 182.27, DISK_STATUS.READY);
		s2dc = new Disk(server2, "sdc", "/export/md0", 200d, -1d, DISK_STATUS.UNINITIALIZED);
		s2dd = new Disk(server2, "sdd", "/export/md1", 200d, 124.89, DISK_STATUS.READY);

		// disk name unavailable since server is offline
		s3da = new Disk(server3, "NA", "NA", -1d, -1d, DISK_STATUS.OFFLINE); 

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

	private void initializeGlusterServers(Cluster cluster, String knownServer) {
		cluster.setServers(new GlusterServersClient(securityToken).getServers(knownServer));
	}

	private void initializeAutoDiscoveredServers(Cluster cluster) {
		cluster.setAutoDiscoveredServers(new DiscoveredServersClient(serverName, securityToken)
				.getDiscoveredServerDetails());
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

	public void initializeRunningTasks(Cluster cluster) {
		RunningTaskListResponse runningTaskResponse = new RunningTaskClient(securityToken).getRunningTasks();
		if (!runningTaskResponse.getStatus().isSuccess()) {
			throw new GlusterRuntimeException(runningTaskResponse.getStatus().getMessage());
		}
		cluster.setRunningTasks(runningTaskResponse.getRunningTasks());
	}

	public void initializeAlerts(Cluster cluster) {
		cluster.setAlerts(new AlertsClient(securityToken).getAllAlerts());
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
		for (Disk disk : allDisks) {
			if (disk.getServerName().equals(brickInfo[0]) && disk.getName().equals(brickInfo[1])) {
				return disk;
			}
		}
		return null;
	}

	public List<Disk> getReadyDisksOfVolume(Volume volume) {
		/*
		 * TODO: review the logic
		 * 
		 * List<Disk> disks = new ArrayList<Disk>(); for (Disk disk : volume.getDisks()) { if (disk.isReady()) {
		 * disks.add(disk); } }
		 */
		Disk disk = null;
		List<Disk> volumeDisks = new ArrayList<Disk>();
		for (String volumeDisk : volume.getDisks()) {
			disk = getVolumeDisk(volumeDisk);
			if (disk != null && disk.isReady()) {
				volumeDisks.add(disk);
			}
		}
		return volumeDisks;
	}
	
	
	public List<Brick> getOnlineBricks(Volume volume) {
		List<Brick> onlineBricks = new ArrayList<Brick>();
		for(Brick brick : volume.getBricks()) {
			if (isOnlineDisk(brick.getDiskName())) {
				onlineBricks.add(brick);
			}
		}
		return onlineBricks;
	}
	
	public boolean isOnlineDisk(String diskName) {
		for( Disk disk : getReadyDisksOfAllServers() ) {
			if (disk.getName().equals(diskName) && disk.isReady()) {
				return true;
			}
		}
		return false;
	}
	
	public List<Disk> getReadyDisksOfAllVolumes() {
		List<Disk> disks = new ArrayList<Disk>();
		for (Volume volume : model.getCluster().getVolumes()) {
			disks.addAll(getReadyDisksOfVolume(volume));
		}
		return disks;
	}

	public List<Disk> getReadyDisksOfAllServers() {
		return getReadyDisksOfAllServersExcluding(new ArrayList<Disk>());
	}

	public List<Disk> getReadyDisksOfAllServersExcluding(List<Disk> excludeDisks) {
		List<Disk> disks = new ArrayList<Disk>();

		for (Server server : model.getCluster().getServers()) {
			for (Disk disk : server.getDisks()) {
				if (disk.isReady() && !excludeDisks.contains(disk)) {
					disks.add(disk);
				}
			}
		}
		return disks;
	}

	public void addClusterListener(ClusterListener listener) {
		listeners.add(listener);
	}

	public void removeClusterListener(ClusterListener listener) {
		listeners.remove(listener);
	}

	public void addGlusterServer(GlusterServer server) {
		Cluster cluster = model.getCluster();
		cluster.addServer(server);

		for (ClusterListener listener : listeners) {
			listener.serverAdded(server);
		}
	}

	public void removeDiscoveredServer(Server server) {
		Cluster cluster = model.getCluster();
		cluster.removeDiscoveredServer(server);

		for (ClusterListener listener : listeners) {
			listener.discoveredServerRemoved(server);
		}
	}

	public void deleteVolume(Volume volume) {
		Cluster cluster = model.getCluster();
		cluster.deleteVolume(volume);

		for (ClusterListener listener : listeners) {
			listener.volumeDeleted(volume);
		}
	}

	public void updateVolumeStatus(Volume volume, VOLUME_STATUS newStatus) {
		volume.setStatus(newStatus);
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.VOLUME_STATUS_CHANGED, newStatus));
		}
	}

	public void resetVolumeOptions(Volume volume) {
		volume.getOptions().clear();
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.VOLUME_OPTIONS_RESET, null));
		}
	}

	public void setVolumeOption(Volume volume, Entry<String, String> entry) {
		volume.setOption(entry.getKey(), (String) entry.getValue());
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.VOLUME_OPTION_SET, entry));
		}
	}

	public void addVolume(Volume volume) {
		Cluster cluster = model.getCluster();
		cluster.addVolume(volume);

		for (ClusterListener listener : listeners) {
			listener.volumeCreated(volume);
		}
	}

	public List<VolumeOptionInfo> getVolumeOptionsDefaults() {
		return volumeOptionsDefaults;
	}

	public VolumeOptionInfo getVolumeOptionInfo(String optionKey) {
		for (VolumeOptionInfo info : volumeOptionsDefaults) {
			if (info.getName().equals(optionKey)) {
				return info;
			}
		}
		throw new GlusterRuntimeException("Invalid option key [" + optionKey
				+ "] passed to GlusterDataModelManager#getVolumeOptionInfo");
	}

	public String getVolumeOptionDefaultValue(String optionKey) {
		return getVolumeOptionInfo(optionKey).getDefaultValue();
	}

	public String getVolumeOptionDesc(String optionKey) {
		return getVolumeOptionInfo(optionKey).getDescription();
	}

	public void setAccessControlList(Volume volume, String accessControlList) {
		volume.setAccessControlList(accessControlList);
		setVolumeOption(volume, getOptionEntry(volume, Volume.OPTION_AUTH_ALLOW));
	}

	private Entry<String, String> getOptionEntry(Volume volume, String optionKey) {
		for (Entry entry : volume.getOptions().entrySet()) {
			if (entry.getKey().equals(optionKey)) {
				return entry;
			}
		}
		throw new GlusterRuntimeException("Couldn't find entry for option [" + optionKey + "] on volume ["
				+ volume.getName());
	}
}
