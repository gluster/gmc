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
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Event.EVENT_TYPE;
import com.gluster.storage.management.core.model.GlusterDataModel;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.model.VolumeOptionInfo;

public class GlusterDataModelManager {
	private static GlusterDataModelManager instance = new GlusterDataModelManager();
	private GlusterDataModel model;
	private String securityToken;
	private List<ClusterListener> listeners = new ArrayList<ClusterListener>();
	private List<VolumeOptionInfo> volumeOptionsDefaults;
	private String clusterName;

	private GlusterDataModelManager() {
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getClusterName() {
		return clusterName;
	}

	public GlusterDataModel getModel() {
		return model;
	}

	public static GlusterDataModelManager getInstance() {
		return instance;
	}

	public void initializeModel(String securityToken, String clusterName) {
		model = new GlusterDataModel("Gluster Data Model");
		setSecurityToken(securityToken);
		setClusterName(clusterName);

		Cluster cluster = new Cluster(clusterName, model);

		initializeGlusterServers(cluster);
		initializeVolumes(cluster);

		initializeAutoDiscoveredServers(cluster);
		// initializeDisks();
		initializeTasks(cluster);
		initializeAlerts(cluster);
		initializeVolumeOptionsDefaults();

		model.addCluster(cluster);
	}

	private void initializeGlusterServers(Cluster cluster) {
		cluster.setServers(new GlusterServersClient().getServers());
	}

	private void initializeAutoDiscoveredServers(Cluster cluster) {
		cluster.setAutoDiscoveredServers(new DiscoveredServersClient(securityToken).getDiscoveredServerDetails());
	}

	private void initializeVolumes(Cluster cluster) {
		VolumesClient volumeClient = new VolumesClient();
		cluster.setVolumes(volumeClient.getAllVolumes());
	}

	private void initializeVolumeOptionsDefaults() {
		this.volumeOptionsDefaults = new VolumesClient().getVolumeOptionsDefaults();
	}

	public void initializeTasks(Cluster cluster) {
		// List<TaskInfo> taskInfoList = new TasksClient(cluster.getName()).getAllTasks();
		List<TaskInfo> taskInfoList = getDummyTasks();
		cluster.setTaskInfoList(taskInfoList);
	}

	private List<TaskInfo> getDummyTasks() {
		List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();

		// Task #1
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setType(TASK_TYPE.BRICK_MIGRATE);
		taskInfo.setName("Migrate Brick-music");
		taskInfo.setCanPause(true);
		taskInfo.setCanStop(true);
		taskInfo.setStatus(new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, "")));
		
		taskInfo.getStatus().setMessage("Migrating file xxxxx to yyyy");
		taskInfo.setDescription("Migrate Brick on volume [music] from /export/adb/music to /export/sdc/music.");
		taskInfoList.add(taskInfo);
		// Task #2
		taskInfo = new TaskInfo();
		taskInfo.setType(TASK_TYPE.DISK_FORMAT);
		taskInfo.setName("Format Disk-server1:sdc");
		taskInfo.setCanPause(false);
		taskInfo.setCanStop(false);
		taskInfo.setStatus( new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, ""))); 
		taskInfo.getStatus().setMessage("Format completes 80% ...");
		taskInfo.setDescription("Formatting disk server1:sdc.");
		taskInfoList.add(taskInfo);

		
		return taskInfoList;
	}

	public void initializeAlerts(Cluster cluster) {
		cluster.setAlerts(new AlertsClient(cluster.getName()).getAllAlerts());
	}

	public Volume addVolume(List<Volume> volumes, String name, Cluster cluster, VOLUME_TYPE volumeType,
			TRANSPORT_TYPE transportType, VOLUME_STATUS status) {
		Volume volume = new Volume(name, cluster, volumeType, transportType, status);
		volumes.add(volume);

		return volume;
	}

	/**
	 * @param serverPartition
	 *            Qualified name of the disk to be returned (serverName:diskName)
	 * @return The disk object for given qualified name
	 */
	public Disk getDisk(String serverPartition) {
		List<Disk> allDisks = getReadyDisksOfAllServers();
		String diskInfo[] = serverPartition.split(":");
		for (Disk disk : allDisks) {
			if (disk.getServerName().equals(diskInfo[0]) && disk.getName().equals(diskInfo[1])) {
				return disk;
			}
		}
		return null;
	}

	/*
	 * @param diskName (sda)
	 * 
	 * @return The disk object for given disk name
	 */
	public Disk getDiskDetails(String diskName) {
		List<Disk> allDisks = getReadyDisksOfAllServers();
		for (Disk disk : allDisks) {
			if (disk.getName().equals(diskName)) {
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
		for (Brick brick : volume.getBricks()) {
			disk = getDisk(brick.getDiskName());
			// disk = new Disk();
			// disk.setServerName(brick.getServerName());
			// disk.setName(brick.getDiskName());
			// disk.setStatus(DISK_STATUS.READY);
			// disk.setMountPoint("/export/" + disk.getName());
			// disk.setSpace(250d);
			// disk.setSpaceInUse(186.39);
			if (disk != null && disk.isReady()) {
				volumeDisks.add(disk);
			}
		}
		return volumeDisks;
	}

	public List<Brick> getOnlineBricks(Volume volume) {
		List<Brick> onlineBricks = new ArrayList<Brick>();
		for (Brick brick : volume.getBricks()) {
			if (isOnlineDisk(brick.getDiskName())) {
				onlineBricks.add(brick);
			}
		}
		return onlineBricks;
	}

	public boolean isOnlineDisk(String diskName) {
		for (Disk disk : getReadyDisksOfAllServers()) {
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
	
	public void addDiscoveredServer(Server server) {
		Cluster cluster = model.getCluster();
		cluster.addDiscoveredServer(server);
		
		for (ClusterListener listener : listeners) {
			listener.discoveredServerAdded(server);;
		}
	}

	public void removeDiscoveredServer(Server server) {
		Cluster cluster = model.getCluster();
		cluster.removeDiscoveredServer(server);

		for (ClusterListener listener : listeners) {
			listener.discoveredServerRemoved(server);
		}
	}
	
	public void removeGlusterServer(GlusterServer server) {
		Cluster cluster = model.getCluster();
		cluster.removeServer(server);
		
		for (ClusterListener listener : listeners) {
			listener.serverRemoved(server);
		}
		
		// add it to discovered servers list
		Server removedServer = new Server();
		removedServer.copyFrom(server);
		addDiscoveredServer(removedServer);
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

	public void addBricks(Volume volume, List<Brick> bricks) {
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.BRICKS_ADDED, bricks));
		}
	}
	
	public void removeBricks(Volume volume, Set<Brick> bricks) {
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.BRICKS_REMOVED, bricks));
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

	public void updateTaskStatus(TaskInfo taskInfo, Status newStatus) {
		taskInfo.getStatus().setCode(newStatus.getCode());
		taskInfo.getStatus().setMessage(newStatus.getMessage());
		for (ClusterListener listener : listeners) {
			listener.taskUpdated(taskInfo);
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

	public Server getGlusterServer(String serverName) {
		for (Server server : model.getCluster().getServers()) {
			if (server.getName().equals(serverName)) {
				return server;
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Entry<String, String> getOptionEntry(Volume volume, String optionKey) {
		for (Entry entry : volume.getOptions().entrySet()) {
			if (entry.getKey().equals(optionKey)) {
				return entry;
			}
		}
		throw new GlusterRuntimeException("Couldn't find entry for option [" + optionKey + "] on volume ["
				+ volume.getName());
	}

	private Boolean isDiskUsed(Volume volume, Disk disk) {
		for (Brick brick : volume.getBricks()) {
			if (disk.getName().equals(brick.getDiskName()) && disk.getServerName().equals(brick.getServerName())) {
				return true;
			}
		}
		return false;
	}

	public String getDiskStatus(Disk disk) {
		if (disk.getStatus() == DISK_STATUS.AVAILABLE) {
			for (Volume volume : model.getCluster().getVolumes()) {
				if (isDiskUsed(volume, disk)) {
					return "In use";
				}
			}
		}
		return disk.getStatusStr();
	}
	
	public List<String> getVolumesOfServer(String serverName) {
		List<String> volumeNames = new ArrayList<String>();
		Cluster cluster = model.getCluster();
		for (Volume volume : cluster.getVolumes()) {
			for (Brick brick : volume.getBricks()) {
				if (serverName.equals(brick.getServerName())) {
					volumeNames.add(volume.getName());
					break;
				}
			}
		}
		return volumeNames;
	}
	
}
