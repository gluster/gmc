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
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="cluster")
public class Cluster extends Entity {
	List<GlusterServer> servers = new ArrayList<GlusterServer>();
	List<Server> discoveredServers = new ArrayList<Server>();
	List<Volume> volumes = new ArrayList<Volume>();
	List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();
	List<Alert> alerts = new ArrayList<Alert>();

	public Cluster() {
	}
	
	public List<GlusterServer> getServers() {
		return servers;
	}
	
	public void addServer(GlusterServer server) {
		servers.add(server);
	}

	public void removeServer(GlusterServer server) {
		servers.remove(server);
	}

	public void addDiscoveredServer(Server server) {
		discoveredServers.add(server);
	}

	public void removeDiscoveredServer(Server server) {
		discoveredServers.remove(server);
	}

	public void deleteVolume(Volume volume) {
		volumes.remove(volume);
	}
	
	public void setServers(List<GlusterServer> servers) {
		this.servers = servers;
		children.add(new EntityGroup<GlusterServer>("Servers", GlusterServer.class, this, servers));
	}
	
	public List<Server> getAutoDiscoveredServers() {
		return discoveredServers;
	}

	public void setAutoDiscoveredServers(List<Server> autoDiscoveredServers) {
		this.discoveredServers = autoDiscoveredServers;
		children.add(new EntityGroup<Server>("Discovered Servers", Server.class, this, autoDiscoveredServers));
	}
	
	public EntityGroup<Server> getAutoDiscoveredServersEntityGroup() {
		for(Entity entity : getChildren()) {
			if(entity instanceof EntityGroup && ((EntityGroup)entity).getEntityType() == Server.class) {
				return (EntityGroup<Server>)entity;
			}
		}
		return null;
	}

	public List<Volume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
		children.add(new EntityGroup<Volume>("Volumes", Volume.class, this, volumes));
	}
	
	public void addVolume(Volume volume) {
		this.volumes.add(volume);
	}
	
	public void updateVolume(String volumeName, List<Brick> bricks) {
		Volume volume = getVolume(volumeName);
		volume.setBricks(bricks);
	}

	public Cluster(String name, Entity parent) {
		super(name, parent);
	}
	
	public Cluster(String name, Entity parent, List<GlusterServer> servers, List<Volume> volumes) {
		super(name, parent);
		setServers(servers);
		setVolumes(volumes);
	}

	public List<TaskInfo> getTaskInfoList() {
		return taskInfoList;
	}

	public void setTaskInfoList(List<TaskInfo> taskInfoList) {
		this.taskInfoList = taskInfoList;
	}
	
	public void addTaskInfo(TaskInfo taskInfo) {
		this.taskInfoList.add(taskInfo);
	}
	
	public void removeTaskInfo(TaskInfo taskInfo) {
		this.taskInfoList.remove(taskInfo);
	}

	public List<Alert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}
	
	public void addAlert(Alert alert) {
		this.alerts.add(alert);
	}
	
	public double getTotalDiskSpace() {
		double totalDiskSpace = 0;
		for(GlusterServer server : getServers()) {
			totalDiskSpace += server.getTotalDiskSpace();
		}
		return totalDiskSpace;
	}
	
	public double getDiskSpaceInUse() {
		double diskSpaceInUse = 0;
		for(GlusterServer server : getServers()) {
			diskSpaceInUse += server.getDiskSpaceInUse();
		}
		return diskSpaceInUse;
	}
	
	public GlusterServer getServer(String serverName) {
		for(GlusterServer server : servers) {
			if (server.getName().equals(serverName)) {
				return server;
			}
		}
		return null;
	}
	
	public Volume getVolume(String volumeName) {
		for (Volume volume : getVolumes() ) {
			if (volume.getName().equals(volumeName)) {
				return volume;
			}
		}
		return null;
	}
}