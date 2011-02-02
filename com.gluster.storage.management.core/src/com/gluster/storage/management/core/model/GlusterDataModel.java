package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

public class GlusterDataModel extends Entity {
	public GlusterDataModel(String name, List<Cluster> clusters) {
		super(name, null);
		children.addAll(clusters);
	}

	public GlusterDataModel(String name) {
		this(name, new ArrayList<Cluster>());
	}

	public void setClusters(List<Cluster> clusters) {
		children.clear();
		children.addAll(clusters);
	}

	public void addCluster(Cluster cluster) {
		children.add(cluster);
	}
}
