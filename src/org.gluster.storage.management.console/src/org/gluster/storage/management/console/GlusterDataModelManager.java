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
package org.gluster.storage.management.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.gluster.storage.management.client.DiscoveredServersClient;
import org.gluster.storage.management.client.GlusterServersClient;
import org.gluster.storage.management.client.TasksClient;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.preferences.PreferenceConstants;
import org.gluster.storage.management.console.utils.GlusterLogger;
import org.gluster.storage.management.core.constants.GlusterConstants;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.model.Alert;
import org.gluster.storage.management.core.model.Alert.ALERT_TYPES;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import org.gluster.storage.management.core.model.Cluster;
import org.gluster.storage.management.core.model.ClusterListener;
import org.gluster.storage.management.core.model.Device;
import org.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import org.gluster.storage.management.core.model.Device.DEVICE_TYPE;
import org.gluster.storage.management.core.model.Disk;
import org.gluster.storage.management.core.model.Event;
import org.gluster.storage.management.core.model.Event.EVENT_TYPE;
import org.gluster.storage.management.core.model.GlusterDataModel;
import org.gluster.storage.management.core.model.GlusterServer;
import org.gluster.storage.management.core.model.Partition;
import org.gluster.storage.management.core.model.Server;
import org.gluster.storage.management.core.model.TaskInfo;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import org.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import org.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import org.gluster.storage.management.core.model.VolumeOptionInfo;
import org.gluster.storage.management.core.utils.GlusterCoreUtil;


public class GlusterDataModelManager {
	private static GlusterDataModelManager instance = new GlusterDataModelManager();
	private GlusterDataModel model;
	private String securityToken;
	private List<ClusterListener> listeners = new ArrayList<ClusterListener>();
	private List<VolumeOptionInfo> volumeOptionsInfo;
	private String clusterName;
 	private static Boolean syncInProgress = false;
 	private static final GlusterLogger logger = GlusterLogger.getInstance();;

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

	public void initializeModel(String clusterName, IProgressMonitor monitor) {
		setClusterName(clusterName);

		model = fetchData(monitor);
	}

	private GlusterDataModel fetchData(IProgressMonitor monitor) {
		GlusterDataModel model = fetchModel(monitor);
		
		initializeAlerts(model.getCluster());
		initializeVolumeOptionsInfo(model.getCluster());

		return model;
	}
	
	public void refreshVolumeData(Volume oldVolume) {
		VolumesClient volumeClient = new VolumesClient();
		Volume newVolume = volumeClient.getVolume(oldVolume.getName());
		if(!oldVolume.equals(newVolume)) {
			volumeChanged(oldVolume, newVolume);
		}
	}

	private boolean isCancelled(IProgressMonitor monitor) {
		if(monitor.isCanceled()) {
			monitor.setTaskName("Data sync cancelled!");
			monitor.done();
			return true;
		} else {
			return false;
		}
	}
	
	public GlusterDataModel fetchModel(IProgressMonitor monitor) {
		synchronized (syncInProgress) {
			if(syncInProgress) {
				logger.info("Previous data sync is still running. Skipping this one.");
				return null;
			}
			syncInProgress = true;
		}

		try {
			logger.info("Starting data sync");
			GlusterDataModel model = new GlusterDataModel("Gluster Data Model");
			Cluster cluster = new Cluster(clusterName, model);
			model.addCluster(cluster);

			monitor.beginTask("Data Sync", 6);

			monitor.setTaskName("Syncing servers...");
			initializeGlusterServers(cluster);
			monitor.worked(1);
			if(isCancelled(monitor)) {
				return model;
			}

			monitor.setTaskName("Syncing volumes...");
			initializeVolumes(cluster);
			monitor.worked(1);
			if(isCancelled(monitor)) {
				return model;
			}

			monitor.setTaskName("Syncing discovered servers...");
			initializeAutoDiscoveredServers(cluster);
			monitor.worked(1);
			if(isCancelled(monitor)) {
				return model;
			}

			monitor.setTaskName("Syncing tasks...");
			initializeTasks(cluster);
			monitor.worked(1);
			if(isCancelled(monitor)) {
				return model;
			}
			
			monitor.setTaskName("Syncing aggregated CPU stats...");
			initializeAggregatedCpuStats(cluster);
			monitor.worked(1);
			if(isCancelled(monitor)) {
				return model;
			}

			monitor.setTaskName("Syncing aggregated Network stats...");
			initializeAggregatedNetworkStats(cluster);
			monitor.worked(1);

			monitor.done();
			return model;
		} finally {
			syncInProgress = false;
		}
	}

	public void updateModel(GlusterDataModel model) {
		updateVolumes(model);
		updateGlusterServers(model);
		updateDiscoveredServers(model);
		updateTasks(model);
		updateAlerts(model);
		updateServerStatistics(model);
	}
	
	private void updateServerStatistics(GlusterDataModel newModel) {
		model.getCluster().setAggregatedCpuStats(newModel.getCluster().getAggregatedCpuStats());
		model.getCluster().setAggregatedNetworkStats(newModel.getCluster().getAggregatedNetworkStats());
		for(ClusterListener listener : listeners) {
			listener.aggregatedStatsChanged();
		}
	}

	private void updateAlerts(GlusterDataModel newModel) {
		model.getCluster().getAlerts().clear();
		
		// generate alerts for "newModel"
		initializeAlerts(newModel.getCluster());
		
		// set the new alerts on "model"
		model.getCluster().setAlerts(newModel.getCluster().getAlerts());
		
		// fire event "alertsGenerated"
		alertsGenerated();
	}
	
	private void updateTasks(GlusterDataModel newModel) {
		List<TaskInfo> oldTasks = model.getCluster().getTaskInfoList();
		List<TaskInfo> newTasks = newModel.getCluster().getTaskInfoList();
		
		Set<TaskInfo> addedTasks = GlusterCoreUtil.getAddedEntities(oldTasks, newTasks, true);
		for(TaskInfo task : addedTasks) {
			addTask(task);
		}
		
		Set<TaskInfo> removedTasks = GlusterCoreUtil.getAddedEntities(newTasks, oldTasks, true);
		for(TaskInfo task : removedTasks) {
			removeTask(task);
		}
		
		Map<TaskInfo, TaskInfo> modifiedTasks = GlusterCoreUtil.getModifiedEntities(oldTasks, newTasks);
		for(Entry<TaskInfo, TaskInfo> entry : modifiedTasks.entrySet()) {
			TaskInfo modifiedTask = entry.getKey();
			modifiedTask.copyFrom(entry.getValue());
			updateTask(modifiedTask);
		}
	}

	private void updateDiscoveredServers(GlusterDataModel newModel) {
		List<Server> oldServers = model.getCluster().getAutoDiscoveredServers();
		List<Server> newServers = newModel.getCluster().getAutoDiscoveredServers();
		
		Set<Server> addedServers = GlusterCoreUtil.getAddedEntities(oldServers, newServers, true);
		for (Server addedServer : addedServers) {
			addDiscoveredServer(addedServer);
		}

		Set<Server> removedServers = GlusterCoreUtil.getAddedEntities(newServers, oldServers, true);
		for (Server removedServer : removedServers) {
			removeDiscoveredServer(removedServer);
		}
		
		Map<Server, Server> modifiedServers = GlusterCoreUtil.getModifiedEntities(oldServers, newServers);
		for(Entry<Server, Server> entry : modifiedServers.entrySet()) {
			discoveredServerChanged(entry.getKey(), entry.getValue());
		}
	}

	private void updateGlusterServers(GlusterDataModel newModel) {
		List<GlusterServer> oldServers = model.getCluster().getServers();
		List<GlusterServer> newServers = newModel.getCluster().getServers();
		
		Set<GlusterServer> addedServers = GlusterCoreUtil.getAddedEntities(oldServers, newServers, true);
		for (GlusterServer addedServer : addedServers) {
			addGlusterServer(addedServer);
		}

		Set<GlusterServer> removedServers = GlusterCoreUtil.getAddedEntities(newServers, oldServers, true);
		for (GlusterServer removedServer : removedServers) {
			removeGlusterServer(removedServer);
		}
		
		Map<GlusterServer, GlusterServer> modifiedServers = GlusterCoreUtil.getModifiedEntities(oldServers, newServers);
		for(Entry<GlusterServer, GlusterServer> entry : modifiedServers.entrySet()) {
			glusterServerChanged(entry.getKey(), entry.getValue());
		}
	}
	
	public void glusterServerChanged(GlusterServer oldServer, GlusterServer newServer) {
		oldServer.copyFrom(newServer);
		for (ClusterListener listener : listeners) {
			listener.serverChanged(oldServer, new Event(EVENT_TYPE.GLUSTER_SERVER_CHANGED, newServer));
		}
		
		updateDisks(oldServer, oldServer.getDisks(), newServer.getDisks());
	}

	private void updateDisks(Server server, List<Disk> oldDisks, List<Disk> newDisks) {
		Set<Disk> addedDisks = GlusterCoreUtil.getAddedEntities(oldDisks, newDisks, false);
		addDisks(server, addedDisks);

		Set<Disk> removedDisks = GlusterCoreUtil.getAddedEntities(newDisks, oldDisks, false);
		removeDisks(server, removedDisks);
		
		Map<Disk, Disk> modifiedDisks = GlusterCoreUtil.getModifiedEntities(oldDisks, newDisks);
		disksChanged(server, modifiedDisks);
	}
	
	private void disksChanged(Server server, Map<Disk, Disk> modifiedDisks) {
		if(modifiedDisks.size() == 0) {
			return;
		}
		
		for (Entry<Disk, Disk> entry : modifiedDisks.entrySet()) {
			entry.getKey().copyFrom(entry.getValue());
		}
		for (ClusterListener listener : listeners) {
			if (server instanceof GlusterServer) {
				listener.serverChanged((GlusterServer) server, new Event(EVENT_TYPE.DEVICES_CHANGED, modifiedDisks));
			} else {
				listener.discoveredServerChanged(server, new Event(EVENT_TYPE.DEVICES_CHANGED, modifiedDisks));
			}
		}
	}
	
	public void updateDeviceStatus(String serverName, String deviceName, DEVICE_STATUS status) {
		GlusterServer server = model.getCluster().getServer(serverName);
		Device device = getDeviceDetails(server, deviceName);
		if (device != null) {
			device.setStatus(status);
			device.setType(DEVICE_TYPE.DATA);
			for (ClusterListener listener : listeners) {
				listener.serverChanged(server, new Event(EVENT_TYPE.DEVICES_CHANGED, device));
			}
		}
	}
	
	private Device getDeviceDetails(GlusterServer server, String deviceName) {
		for (Disk disk : server.getDisks()) {
			if (disk.hasPartitions()) {
				for (Partition partition : disk.getPartitions()) {
					if (partition.getName().equals(deviceName)) {
						return partition;
					}
				}
			} else {
				if (disk.getName().equals(deviceName)) {
					return (Device) disk;
				}
			}
		}
		return null;
	}
	
	public void addDisks(Server server, Set<Disk> disks) {
		if(disks.size() == 0) {
			return;
		}
		
		server.addDisks(disks);
		for (ClusterListener listener : listeners) {
			if(server instanceof GlusterServer) {
				listener.serverChanged((GlusterServer)server, new Event(EVENT_TYPE.DEVICES_ADDED, disks));
			} else {
				listener.discoveredServerChanged(server, new Event(EVENT_TYPE.DEVICES_ADDED, disks));
			}
		}
	}

	public void removeDisks(Server server, Set<Disk> disks) {
		if(disks.size() == 0) {
			return;
		}
		
		for(Disk disk : disks) {
			server.removeDisk(disk);
		}
		
		for (ClusterListener listener : listeners) {
			if(server instanceof GlusterServer) {
				listener.serverChanged((GlusterServer)server, new Event(EVENT_TYPE.DEVICES_REMOVED, disks));
			} else {
				listener.discoveredServerChanged(server, new Event(EVENT_TYPE.DEVICES_REMOVED, disks));
			}
		}
	}

	private void updateVolumes(GlusterDataModel newModel) {
		List<Volume> oldVolumes = model.getCluster().getVolumes();
		List<Volume> newVolumes = newModel.getCluster().getVolumes();
		
		Set<Volume> addedVolumes = GlusterCoreUtil.getAddedEntities(oldVolumes, newVolumes, false);
		for (Volume addedVolume : addedVolumes) {
			addVolume(addedVolume);
		}
		
		Set<Volume> removedVolumes = GlusterCoreUtil.getAddedEntities(newVolumes, oldVolumes, false);
		for (Volume removedVolume : removedVolumes) {
			deleteVolume(removedVolume);
		}
		
		Map<Volume, Volume> modifiedVolumes = GlusterCoreUtil.getModifiedEntities(oldVolumes, newVolumes);
		for(Entry<Volume, Volume> entry : modifiedVolumes.entrySet()) {
			volumeChanged(entry.getKey(), entry.getValue());
		}
	}
	
	public void volumeChanged(Volume oldVolume, Volume newVolume) {
		oldVolume.copyFrom(newVolume);
		
		if (oldVolume.getStatus() != newVolume.getStatus()) {
			updateVolumeStatusAlert(newVolume, newVolume.getStatus());
		}
		
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(oldVolume, new Event(EVENT_TYPE.VOLUME_CHANGED, newVolume));
		}
		updateBricks(oldVolume, oldVolume.getBricks(), newVolume.getBricks());
	}

	private void updateBricks(Volume volume, List<Brick> oldBricks, List<Brick> newBricks) {
		Set<Brick> addedBricks = GlusterCoreUtil.getAddedEntities(oldBricks, newBricks, false);
		addBricks(volume, addedBricks);

		Set<Brick> removedBricks = GlusterCoreUtil.getAddedEntities(newBricks, oldBricks, false);
		removeBricks(volume, removedBricks);
		
		Map<Brick, Brick> modifiedBricks = GlusterCoreUtil.getModifiedEntities(oldBricks, newBricks);
		bricksChanged(volume, modifiedBricks);
	}

	public void bricksChanged(Volume volume, Map<Brick, Brick> modifiedBricks) {
		if(modifiedBricks.size() == 0) {
			return;
		}
		
		for(Entry<Brick, Brick> entry : modifiedBricks.entrySet()) {
			entry.getKey().copyFrom(entry.getValue());
		}
		
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.BRICKS_CHANGED, modifiedBricks));
		}
	}

	private void initializeGlusterServers(Cluster cluster) {
		cluster.setServers(new GlusterServersClient(cluster.getName()).getServers());
	}

	private void initializeAutoDiscoveredServers(Cluster cluster) {
		cluster.setAutoDiscoveredServers(new DiscoveredServersClient(cluster.getName()).getDiscoveredServerDetails());
	}

	private void initializeVolumes(Cluster cluster) {
		VolumesClient volumeClient = new VolumesClient(cluster.getName());
		cluster.setVolumes(volumeClient.getAllVolumes());
	}

	private void initializeVolumeOptionsInfo(Cluster cluster) {
		if(cluster.getServers().isEmpty()) {
			// cluster is empty. we won't be able to fetch the volume options information.
			return;
		}
		this.volumeOptionsInfo = new VolumesClient(clusterName).getVolumeOptionsInfo();
	}

	private void initializeTasks(Cluster cluster) {
		List<TaskInfo> taskInfoList = new TasksClient(cluster.getName()).getAllTasks();
		//List<TaskInfo> taskInfoList = getDummyTasks();
		cluster.setTaskInfoList(taskInfoList);
	}

	public void initializeAggregatedCpuStats(Cluster cluster) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String cpuStatsPeriod = preferenceStore.getString(PreferenceConstants.P_CPU_AGGREGATED_CHART_PERIOD);
		
		cluster.setAggregatedCpuStats(new GlusterServersClient().getAggregatedCpuStats(cpuStatsPeriod));
	}
	
	public void initializeAggregatedNetworkStats(Cluster cluster) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String networkStatsPeriod = preferenceStore.getString(PreferenceConstants.P_NETWORK_AGGREGATED_CHART_PERIOD);
		
		cluster.setAggregatedNetworkStats(new GlusterServersClient().getAggregatedNetworkStats(networkStatsPeriod));
	}

	public void initializeAlerts(Cluster cluster) {
		AlertsManager alertsManager = new AlertsManager(cluster);
		alertsManager.buildAlerts();
		cluster.setAlerts( alertsManager.getAlerts() );
		//cluster.setAlerts( getDummyAlerts(cluster) );
	}
	
	public Volume addVolume(List<Volume> volumes, String name, Cluster cluster, VOLUME_TYPE volumeType,
			TRANSPORT_TYPE transportType, VOLUME_STATUS status) {
		Volume volume = new Volume(name, cluster, volumeType, transportType, status);
		volumes.add(volume);

		return volume;
	}

//	private Device getDevice(String serverName, String deviceName) {
//		List<Device> allDevices = getReadyDevicesOfAllServers();
//		for (Device device : allDevices) {
//			if (device.getServerName().equals(serverName) && device.getName().equals(deviceName)) {
//				return device;
//			}
//		}
//		return null;
//	}

	/*
	 * @param diskName (sda)
	 * 
	 * @return The device object for given device name
	 */
	public Device getDeviceDetails(String deviceName) {
		List<Device> allDevices = getReadyDevicesOfAllServers();
		for (Device device : allDevices) {
			if (device.getName().equals(deviceName)) {
				return device;
			}
		}
		return null;
	}


	public Device getDeviceForBrickDir(Brick brick) {
		Device brickDevice = null;
		for (Device device :   getReadyDevicesOfServer(brick.getServerName(), new ArrayList<Device>())) {
			if (brick.getBrickDirectory().startsWith( device.getMountPoint() )) {
				if (brickDevice == null || device.getMountPoint().length() > brickDevice.getMountPoint().length()) {
					brickDevice = device;
				}
			}
		}
		return brickDevice;
	}
	
	public List<Device> getDevicesOfVolume(Volume volume) {
		Device device = null;
		List<Device> volumeDevices = new ArrayList<Device>();
		for (Brick brick : volume.getBricks()) {
			// device = getDevice(brick.getServerName(), brick.getDeviceName());
			device = getDeviceForBrickDir(brick);
			if (device != null) {
				volumeDevices.add(device);
			}
		}
		return volumeDevices;
	}

	public List<Device> getReadyDevicesOfAllServers() {
		return getReadyDevicesOfAllServersExcluding(new ArrayList<Device>());
	}

	public List<Device> getReadyDevicesOfAllServersExcluding(List<Device> excludeDevices) {
		List<Device> devices = new ArrayList<Device>();

		for (Server server : model.getCluster().getServers()) {
			devices.addAll( getReadyDevicesOfServer(server.getName(), excludeDevices) );
		}
		return devices;
	}
	
	public List<Device> getReadyDevicesOfServer(String serverName, List<Device> excludeDevices) {
		List<Device> devices = new ArrayList<Device>();
		GlusterServer server = model.getCluster().getServer(serverName);
		if (server == null || !server.isOnline()) {
			return devices;
		}
		for (Disk disk : server.getDisks()) {
			if (disk.hasPartitions()) {
				for (Partition partition : disk.getPartitions()) {
					if (partition.isReady() && !excludeDevices.contains(partition)) {
						devices.add(partition);
					}
				}
			} else if (disk.isReady() && !excludeDevices.contains(disk)) {
				devices.add(disk);
			}
		}
		return devices;
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
		
		removeDiscoveredServer(server.getName());
	}
	
	public void addDiscoveredServer(Server server) {
		Cluster cluster = model.getCluster();
		cluster.addDiscoveredServer(server);
		
		for (ClusterListener listener : listeners) {
			listener.discoveredServerAdded(server);
		}
	}
	
	public void discoveredServerChanged(Server oldServer, Server newServer) {
		oldServer.copyFrom(newServer);
		for (ClusterListener listener : listeners) {
			listener.discoveredServerChanged(oldServer, new Event(EVENT_TYPE.DISCOVERED_SERVER_CHANGED, newServer));
		}
		updateDisks(oldServer, oldServer.getDisks(), newServer.getDisks());
	}
	
	public void removeDiscoveredServer(String serverName) {
		Cluster cluster = model.getCluster();
		// TODO: Move auto-discovered servers outside the cluster
		for(Server server : cluster.getAutoDiscoveredServers()) {
			if(server.getName().toUpperCase().equals(serverName.toUpperCase())) {
				removeDiscoveredServer(server);
				return;
			}
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
		
		// can't use an iterator here. The method AbstractList.Itr#next checks for concurrent modification.
		// Since listeners can end up creating new views, which add themselves as listeners, the listeners
		// list can be concurrently modified which can result in an exception while using iterator. 
		// Hence we use List#get instead of the iterator
		for(int i = 0; i < listeners.size(); i++) {
			ClusterListener listener = listeners.get(i);
			listener.serverRemoved(server);
		}
		
		// add it to discovered servers list if it is online server
		if (server.isOnline()) {
			Server removedServer = new Server();
			removedServer.copyFrom(server);
			removedServer.addDisks(server.getDisks());
			addDiscoveredServer(removedServer);
		}
	}

	public void deleteVolume(Volume volume) {
		Cluster cluster = model.getCluster();
		cluster.deleteVolume(volume);

		// can't use an iterator here. The method AbstractList.Itr#next checks for concurrent modification.
		// Since listeners can end up creating new views, which add themselves as listeners, the listeners
		// list can be concurrently modified which can result in an exception while using iterator. 
		// Hence we use List#get instead of the iterator
		for(int i = 0; i < listeners.size(); i++) {
			ClusterListener listener = listeners.get(i);
			listener.volumeDeleted(volume);
		}
	}

	public void updateVolumeStatus(Volume volume, VOLUME_STATUS newStatus) {
		volume.setStatus(newStatus);
		updateVolumeStatusAlert(volume, newStatus);
		
		if(newStatus == VOLUME_STATUS.OFFLINE) {
			// mark as bricks also as offline
			for(Brick brick : volume.getBricks()) {
				brick.setStatus(BRICK_STATUS.OFFLINE);
			}
			for (ClusterListener listener : listeners) {
				listener.volumeChanged(volume, new Event(EVENT_TYPE.BRICKS_CHANGED, volume.getBricks()));
			}
		} else {
			Volume newVolume = new VolumesClient().getVolume(volume.getName()); //Getting latest brick info
			updateBricks(volume, volume.getBricks(), newVolume.getBricks());
		}
		
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.VOLUME_STATUS_CHANGED, newStatus));
		}
	}
	
	private void updateVolumeStatusAlert(Volume volume, VOLUME_STATUS newStatus) {
		Alert alert = null;
		if (newStatus == VOLUME_STATUS.OFFLINE) {
			alert = createOfflineVolumeAlert(volume);
			for (ClusterListener listener : listeners) {
				listener.alertCreated(alert);
			}
		} else {
			alert = removeOfflineVolumeAlert(volume);
			for (ClusterListener listener : listeners) {
				listener.alertRemoved(alert);
			}
		}
	}
	
	private Alert createOfflineVolumeAlert(Volume volume) {
		Alert alert = new Alert(ALERT_TYPES.OFFLINE_VOLUME_ALERT, volume.getName(),
				Alert.ALERT_TYPE_STR[ALERT_TYPES.OFFLINE_VOLUME_ALERT.ordinal()] + " [" + volume.getName() + "]");
		getModel().getCluster().addAlert(alert);
		return alert;
	}

	private Alert removeOfflineVolumeAlert(Volume volume) {
		List<Alert> clusterAlerts = getModel().getCluster().getAlerts();
		Alert removedAlert = null;
		for (Alert alert : clusterAlerts) {
			if (alert.getType().equals(ALERT_TYPES.OFFLINE_VOLUME_ALERT)
					&& alert.getReference().equals(volume.getName())) {
				removedAlert = alert;
				clusterAlerts.remove(alert);
				break;
			}
		}
		return removedAlert;
	}

	public void resetVolumeOptions(Volume volume) {
		volume.getOptions().clear();
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.VOLUME_OPTIONS_RESET, null));
		}
	}

	public void addBricks(Volume volume, Set<Brick> bricks) {
		if(bricks.size() == 0) {
			return;
		}
		
		volume.addBricks(bricks);
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.BRICKS_ADDED, bricks));
		}
		updateVolumeTypeByBricks(volume);
	}
	
	public void removeBricks(Volume volume, Set<Brick> bricks) {
		if(bricks.size() == 0) {
			return;
		}
		
		// Remove the bricks from the volume object
		for (Brick brick : bricks) {
			volume.removeBrick(brick);
		}

		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.BRICKS_REMOVED, bricks));
		}
		updateVolumeTypeByBricks(volume);
	}

	private void updateVolumeTypeByBricks(Volume volume) {
		VOLUME_TYPE volumeType = volume.getVolumeType();
		if (volumeType.equals(VOLUME_TYPE.REPLICATE) || volumeType.equals(VOLUME_TYPE.DISTRIBUTED_REPLICATE)) {
			if (volume.getBricks().size() > volume.getReplicaCount()) {
				volume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_REPLICATE);
			} else {
				volume.setVolumeType(VOLUME_TYPE.REPLICATE);
			}
		} else if (volumeType.equals(VOLUME_TYPE.STRIPE) || volumeType.equals(VOLUME_TYPE.DISTRIBUTED_STRIPE)) {
			if (volume.getBricks().size() > volume.getStripeCount()) {
				volume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_STRIPE);
			} else {
				volume.setVolumeType(VOLUME_TYPE.STRIPE);
			}
		}
	}
	
	public void setVolumeOption(Volume volume, String optionKey, String optionValue) {
		volume.setOption(optionKey, optionValue);
		for (ClusterListener listener : listeners) {
			listener.volumeChanged(volume, new Event(EVENT_TYPE.VOLUME_OPTION_SET, optionKey));
		}
	}

	public void addVolume(Volume volume) {
		Cluster cluster = model.getCluster();
		cluster.addVolume(volume);

		for (ClusterListener listener : listeners) {
			listener.volumeCreated(volume);
		}
	}
	
	public void addTask(TaskInfo taskInfo) {
		Cluster cluster = model.getCluster();
		// To avoid duplicate task, Remove if already exist
		TaskInfo existingTaskInfo = getTask(taskInfo.getName());
		if (getTask(taskInfo.getName()) != null) {
			removeTask(existingTaskInfo);
		}
		cluster.addTaskInfo(taskInfo);
		for (ClusterListener listener : listeners) {
			listener.taskAdded(taskInfo);
		}
	}
	
	public TaskInfo getTask(String taskId) {
		for (TaskInfo taskInfo: model.getCluster().getTaskInfoList()) {
			if (taskInfo.getName().equals(taskId)) {
				return taskInfo;
			}
		}
		return null;
	}
	
	public TaskInfo getTaskByReference(String reference) {
		for (TaskInfo taskInfo: model.getCluster().getTaskInfoList()) {
			if (taskInfo.getReference().equals(reference)) {
				return taskInfo;
			}
		}
		return null;
	}
	
	// Updating the Task
	public void updateTask(TaskInfo taskInfo) {
		for (ClusterListener listener : listeners) {
			listener.taskUpdated(taskInfo);
		}
	}
	
	public void removeTask(TaskInfo taskInfo) {
		model.getCluster().removeTaskInfo(taskInfo);
		for (ClusterListener listener : listeners) {
			listener.taskRemoved(taskInfo);
		}
	}

	public void alertsGenerated() {
		for (ClusterListener listener : listeners) {
			listener.alertsGenerated();
		}
	}
	
	public List<VolumeOptionInfo> getVolumeOptionsInfo() {
		if(volumeOptionsInfo == null || volumeOptionsInfo.isEmpty()) {
			initializeVolumeOptionsInfo(getModel().getCluster());
		}
		return volumeOptionsInfo;
	}

	public VolumeOptionInfo getVolumeOptionInfo(String optionKey) {
		for (VolumeOptionInfo info : volumeOptionsInfo) {
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
		setVolumeOption(volume, Volume.OPTION_AUTH_ALLOW, accessControlList);
	}
	
	public void setNfsEnabled(Volume volume, boolean enabled) {
		setVolumeOption(volume, Volume.OPTION_NFS_DISABLE, (enabled) ? GlusterConstants.OFF : GlusterConstants.ON);
	}
	
	public void setCifsConfig(Volume volume, boolean enabled, List<String> cifsUsers) {
		if (enabled) {
			volume.enableCifs();
			volume.setCifsUsers(cifsUsers);
		} else {
			volume.disableCifs();
		}
	}
	
	public Server getGlusterServer(String serverName) {
		for (Server server : model.getCluster().getServers()) {
			if (server.getName().equalsIgnoreCase(serverName)) {
				return server;
			}
		}
		return null;
	}

	private Boolean isDeviceUsed(Volume volume, Device device) {
		Device brickDevice = null;
		for (Brick brick : volume.getBricks()) {
			brickDevice = getDeviceForBrickDir(brick);
			if (brickDevice != null && device.getName().equals(brickDevice.getName())
					&& device.getServerName().equalsIgnoreCase(brick.getServerName())) {
				return true;
			}
		}
		return false;
	}

	public boolean isDeviceUsed(Device device) {
		if (device.getStatus() == DEVICE_STATUS.INITIALIZED) {
			for (Volume volume : model.getCluster().getVolumes()) {
				if (isDeviceUsed(volume, device)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<String> getVolumesOfServer(String serverName) {
		List<String> volumeNames = new ArrayList<String>();
		Cluster cluster = model.getCluster();
		for (Volume volume : cluster.getVolumes()) {
			for (Brick brick : volume.getBricks()) {
				if (serverName.equalsIgnoreCase(brick.getServerName())) {
					volumeNames.add(volume.getName());
					break;
				}
			}
		}
		return volumeNames;
	}
	
	public List<String> getOfflineServers() {
		List<String> offlineServers = new ArrayList<String>();
		for(GlusterServer server : model.getCluster().getServers()) {
			if (!server.isOnline()) {
				offlineServers.add(server.getName());
			}
		}
		return offlineServers;
	}
	
	public List<String> getCifsEnabledVolumeNames(List<Volume> selectedVolumes) {
		List<String> cifsVolumes = new ArrayList<String>();
		for(Volume volume : selectedVolumes) {
			if (volume.isCifsEnable()) {
				cifsVolumes.add(volume.getName());
			}
		}
		return cifsVolumes;
	}
}
