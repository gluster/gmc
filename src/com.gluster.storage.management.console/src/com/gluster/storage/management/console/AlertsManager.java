/**
 * AlertsManager.java
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
package com.gluster.storage.management.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import com.gluster.storage.management.console.preferences.PreferenceConstants;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Alert.ALERT_TYPES;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Partition;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.utils.NumberUtil;

public class AlertsManager {
	private List<Alert> alerts = new ArrayList<Alert>();
	private Cluster cluster;

	private Double CPU_USAGE_THRESHOLD;
	private Double MEMORY_USAGE_THRESHOLD;
	private Double DISK_SPACE_USAGE_THRESHOLD;

	public AlertsManager(Cluster cluster) {
		this.cluster = cluster;

		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		CPU_USAGE_THRESHOLD = preferenceStore.getDouble(PreferenceConstants.P_SERVER_CPU_CRITICAL_THRESHOLD);
		MEMORY_USAGE_THRESHOLD = preferenceStore.getDouble(PreferenceConstants.P_SERVER_MEMORY_USAGE_THRESHOLD);
		DISK_SPACE_USAGE_THRESHOLD = preferenceStore.getDouble(PreferenceConstants.P_DISK_SPACE_USAGE_THRESHOLD);
	}

	public List<Alert> getAlerts() {
		return alerts;
	}

	public Alert getAlert(String id) {
		for (Alert alert : getAlerts()) {
			if (alert.getId().equals(id)) {
				return alert;
			}
		}
		return null;
	}

	public void addAlert(Alert alert) {
		alerts.add(alert);
	}

	public void addAlerts(List<Alert> alerts) {
		this.alerts.addAll(alerts);
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

	public Boolean removeAlert(String id) {
		for (int i = 0; i < alerts.size(); i++) {
			if (alerts.get(i).getId().equals(id)) {
				return (alerts.remove(i) != null);
			}
		}
		return false;
	}

	public void clearAll() {
		this.alerts.clear();
	}

	public void buildAlerts() {
		clearAll();
		addAlerts(getServerAlerts());
		addAlerts(getVolumeAlerts());
	}

	private List<Alert> getServerAlerts() {
		List<Alert> serverAlerts = new ArrayList<Alert>();
		Alert offlineServerAlert = getOfflineServerAlerts();
		if (offlineServerAlert != null) {
			serverAlerts.add(offlineServerAlert); // Single alert for offline servers
		}

		for (GlusterServer server : cluster.getServers()) {
			// To check off line servers
			// if (server.getStatus() == SERVER_STATUS.OFFLINE) {
			// serverAlerts.add(new Alert(ALERT_TYPES.OFFLINE_SERVERS_ALERT, server.getName(), "Server ["
			// + server.getName() + "] is Offline"));
			// continue; // If the server is Offline skip other Alert builds
			// }

			// To check High CPU usage
			if (server.getCpuUsage() >= CPU_USAGE_THRESHOLD) {
				serverAlerts.add(new Alert(ALERT_TYPES.CPU_USAGE_ALERT, server.getName(),
						Alert.ALERT_TYPE_STR[ALERT_TYPES.CPU_USAGE_ALERT.ordinal()] + " ["
								+ NumberUtil.formatNumber(server.getCpuUsage()) + "] in server [" + server.getName()
								+ "]"));
			}

			// To check High Memory usage
			Double memoryUtilized = server.getMemoryInUse() / server.getTotalMemory() * 100d;
			if (memoryUtilized >= MEMORY_USAGE_THRESHOLD) {
				serverAlerts.add(new Alert(ALERT_TYPES.MEMORY_USAGE_ALERT, server.getName(),
						Alert.ALERT_TYPE_STR[ALERT_TYPES.MEMORY_USAGE_ALERT.ordinal()] + " ["
								+ NumberUtil.formatNumber(memoryUtilized) + "%] in server [" + server.getName()
								+ "]"));
			}

			// To Check low disk space
			serverAlerts.addAll(getLowDiskAlerts(server));
		}
		return serverAlerts;
	}

	private Alert getOfflineServerAlerts() {
		List<String> offlineServers = new ArrayList<String>();
		for (GlusterServer server : cluster.getServers()) {
			if (server.getStatus() == SERVER_STATUS.OFFLINE) {
				offlineServers.add(server.getName());
			}
		}
		if (offlineServers.size() > 0) {
			return new Alert(ALERT_TYPES.OFFLINE_SERVERS_ALERT, "Server",
					Alert.ALERT_TYPE_STR[ALERT_TYPES.OFFLINE_SERVERS_ALERT.ordinal()] + "(s) "
							+ offlineServers.toString());
		}
		return null;
	}

	private List<Alert> getLowDiskAlerts(GlusterServer server) {
		List<Alert> diskAlerts = new ArrayList<Alert>();
		boolean hasPartition;
		Double deviceSpaceUsed;
		for (Disk disk : server.getDisks()) {
			hasPartition = false;
			for (Partition partition : disk.getPartitions()) {
				hasPartition = true;
				deviceSpaceUsed = partition.getSpaceInUse() / partition.getSpace() * 100d;
				if (deviceSpaceUsed >= DISK_SPACE_USAGE_THRESHOLD) {
					diskAlerts.add(new Alert(ALERT_TYPES.DISK_USAGE_ALERT, partition.getQualifiedName(),
							Alert.ALERT_TYPE_STR[ALERT_TYPES.DISK_USAGE_ALERT.ordinal()] + " ["
									+ NumberUtil.formatNumber(deviceSpaceUsed) + "% used] in disk ["
									+ partition.getQualifiedName() + "]"));
				}
			}
			if (hasPartition) {
				continue; // Do not check disk usage
			}

			// If it is disk
			deviceSpaceUsed = disk.getSpaceInUse() / disk.getSpace() * 100d;
			if (deviceSpaceUsed >= DISK_SPACE_USAGE_THRESHOLD) {
				diskAlerts.add(new Alert(ALERT_TYPES.DISK_USAGE_ALERT, disk.getQualifiedName(),
						Alert.ALERT_TYPE_STR[ALERT_TYPES.DISK_USAGE_ALERT.ordinal()] + " ["
								+ NumberUtil.formatNumber(deviceSpaceUsed) + "% used] in ["
								+ disk.getQualifiedName() + "]"));
			}
		}
		return diskAlerts;
	}

	private List<Alert> getVolumeAlerts() {
		List<Alert> volumeAlerts = new ArrayList<Alert>();
		List<String> offlineBricks = new ArrayList<String>();

		for (Volume volume : cluster.getVolumes()) {
			if (volume.getStatus() == VOLUME_STATUS.OFFLINE) {
				volumeAlerts.add(new Alert(ALERT_TYPES.OFFLINE_VOLUME_ALERT, volume.getName(),
						Alert.ALERT_TYPE_STR[ALERT_TYPES.OFFLINE_VOLUME_ALERT.ordinal()] + " [" + volume.getName()
								+ "]"));
				continue;
			}
			
			// To check off line bricks
			offlineBricks = new ArrayList<String>();
			for (Brick brick : volume.getBricks()) {
				if (brick.getStatus() == BRICK_STATUS.OFFLINE) {
					offlineBricks.add(brick.getQualifiedName());
				}
			}
			// One offline brick alert per volume
			if (offlineBricks.size() > 0) {
				volumeAlerts.add(new Alert(ALERT_TYPES.OFFLINE_VOLUME_BRICKS_ALERT, volume.getName(),
						Alert.ALERT_TYPE_STR[ALERT_TYPES.OFFLINE_VOLUME_BRICKS_ALERT.ordinal()] + " "
								+ offlineBricks.toString() + " in volume " + volume.getName()));
			}
		}
		return volumeAlerts;
	}
}
