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
	private List<IClusterListener> listeners = new ArrayList<IClusterListener>();
	List<GlusterServer> servers = new ArrayList<GlusterServer>();
	List<Server> autoDiscoveredServers = new ArrayList<Server>();
	List<Volume> volumes = new ArrayList<Volume>();

	public Cluster() {
	}
	
	public List<GlusterServer> getServers() {
		return servers;
	}

	public void setServers(List<GlusterServer> servers) {
		this.servers = servers;
		children.add(new EntityGroup<GlusterServer>("Servers", GlusterServer.class, this, servers));
	}
	
	public List<Server> getAutoDiscoveredServers() {
		return autoDiscoveredServers;
	}

	public void setAutoDiscoveredServers(List<Server> autoDiscoveredServers) {
		this.autoDiscoveredServers = autoDiscoveredServers;
		children.add(new EntityGroup<Server>("Discovered Servers", Server.class, this, autoDiscoveredServers));
	}

	public List<Volume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
		children.add(new EntityGroup<Volume>("Volumes", Volume.class, this, volumes));
	}

	public Cluster(String name, Entity parent) {
		super(name, parent);
	}
	
	public Cluster(String name, Entity parent, List<GlusterServer> servers, List<Volume> volumes) {
		super(name, parent);
		setServers(servers);
		setVolumes(volumes);
	}
	
	public void addClusterListener(IClusterListener listener) {
		listeners.add(listener);
	}
}