package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

public class Cluster extends Entity {
	private List<IClusterListener> listeners = new ArrayList<IClusterListener>();
	List<GlusterServer> servers = new ArrayList<GlusterServer>();
	List<Server> autoDiscoveredServers = new ArrayList<Server>();
	List<Volume> volumes = new ArrayList<Volume>();

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