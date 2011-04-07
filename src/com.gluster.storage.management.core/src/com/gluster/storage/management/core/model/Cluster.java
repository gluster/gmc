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
	List<RunningTask> runningTasks = new ArrayList<RunningTask>();
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

	public Cluster(String name, Entity parent) {
		super(name, parent);
	}
	
	public Cluster(String name, Entity parent, List<GlusterServer> servers, List<Volume> volumes) {
		super(name, parent);
		setServers(servers);
		setVolumes(volumes);
	}

	public List<RunningTask> getRunningTasks() {
		return runningTasks;
	}

	public void setRunningTasks(List<RunningTask> runningTasks) {
		this.runningTasks = runningTasks;
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
}