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
