package com.gluster.storage.management.core.model;

import java.util.List;

public class EntityGroup<T> extends Entity {
	private Class<? extends Entity> type;
	
	public EntityGroup(String name, Class<? extends Entity> type, Cluster cluster) {
		this(name, type, cluster, null);
	}

	public EntityGroup(String name, Class<? extends Entity> type, Cluster cluster, List<T> entities) {
		super(name, cluster, (List<Entity>)entities);
		this.type = type;
	}

	public List<? extends Entity> getEntities() {
		return children;
	}

	public void setEntities(List<T> entities) {
		children = (List<Entity>)entities;
	}
	
	public Class<? extends Entity> getEntityType() {
		return type;
	}
}
