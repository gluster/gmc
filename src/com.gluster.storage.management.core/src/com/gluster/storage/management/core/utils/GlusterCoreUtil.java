/**
 * GlusterCoreUtil.java
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
package com.gluster.storage.management.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Partition;
import com.gluster.storage.management.core.model.Server;


public class GlusterCoreUtil {
	// Convert from Disk list to Qualified disk name list 
	public static final List<String> getQualifiedDiskNames(List<Disk> diskList) {
		List<String> qualifiedDiskNames = new ArrayList<String>();
		for (Disk disk : diskList) {
			qualifiedDiskNames.add(disk.getQualifiedName());
		}
		return qualifiedDiskNames;
	}
	
	public static final List<String> getQualifiedBrickList(Set<Brick> bricks) {
		List<String> qualifiedBricks = new ArrayList<String>();
		for (Brick brick : bricks) {
			qualifiedBricks.add(brick.getQualifiedName());
		}
		return qualifiedBricks;
	}
	
	/**
	 * Compares the two entity lists and returns the list of entities from first list that have been modified in the second
	 * 
	 * @param oldEntities
	 * @param newEntities
	 * @return List of entities that have been modified
	 */
	public static <T extends Entity> Map<T, T> getModifiedEntities(List<T> oldEntities, List<T> newEntities) {
		Map<T, T> modifiedEntities = new HashMap<T, T>();
		for (T oldEntity : oldEntities) {
			T newEntity = getEntity(newEntities, oldEntity.getName(), false);
			if(newEntity != null && !oldEntity.equals(newEntity)) {
				// old and new entities differ. mark it as modified.
				modifiedEntities.put(oldEntity, newEntity);
			}
		}
		return modifiedEntities;
	}

	/**
	 * Compares the two entity lists and returns the list of entities present only in the second argument
	 * <code>newEntities</code>
	 * 
	 * @param oldEntities
	 * @param newEntities
	 * @param caseInsensitive If true, the entity name comparison will be done in case insensitive manner
	 * @return List of entities that are present only in the second argument <code>newEntities</code>
	 */
	public static <T extends Entity> Set<T> getAddedEntities(List<T> oldEntities, List<T> newEntities,
			boolean caseInsensitive) {
		Set<T> addedEntities = new HashSet<T>();
		for (T newEntity : newEntities) {
			if (!containsEntity(oldEntities, newEntity, caseInsensitive)) {
				// old entity list doesn't contain this entity. mark it as new.
				addedEntities.add(newEntity);
			}
		}
		return addedEntities;
	}

	public static <T extends Entity> boolean containsEntity(List<T> entityList, Entity searchEntity,
			boolean caseInsensitive) {
		return getEntity(entityList, searchEntity.getName(), caseInsensitive) != null;
	}
	
	public static <T extends Entity> boolean containsEntityWithName(List<T> entityList, String searchName,
			boolean caseInsensitive) {
		return getEntity(entityList, searchName, caseInsensitive) != null;
	}

	public static <T extends Entity> T getEntity(Collection<T> entityList, String searchEntityName, boolean caseInsensitive) {
		for (T entity : entityList) {
			String nextEntityName = entity.getName();
			if (nextEntityName.equals(searchEntityName)
					|| (caseInsensitive && nextEntityName.equalsIgnoreCase(searchEntityName))) {
				return entity;
			}
		}

		return null;
	}
	
	public static void updateServerNameOnDevices(Server server) {
		String serverName = server.getName();
		for(Disk disk : server.getDisks()) {
			disk.setServerName(serverName);
			
			if (disk.getRaidDisks() != null) {
				for (Disk raidDisk : disk.getRaidDisks()) {
					raidDisk.setServerName(serverName);
				}
			}
			
			if (disk.getPartitions() != null) {
				for (Partition partition : disk.getPartitions()) {
					partition.setServerName(serverName);
				}
			}
		}
	}

	/**
	 * Skips (removes) the entities from given list till (including) the given {@code tillServer}, upto a maximum of
	 * {@code maxCount} entities<br>
	 * 
	 * @param entities
	 *            List of entities to be pruned
	 * @param maxCount
	 *            Maximum number of entities to be returned
	 * @param tillEntity
	 *            A list of entities after skipping the ones appearing before, and including {@code tillEntity}. If the
	 *            resulting list is bigger than {@code maxCount}, then the first {@code maxCount} number of entities
	 *            will be returned.
	 */
	public static <T extends Entity> List<T> skipEntities(List<T> entities, Integer maxCount, String tillEntity) {
		List<T> servers = skipEntitiesTill(entities, tillEntity);
		return skipEntitiesByMaxCount(servers, maxCount);
	}

	/**
	 * Removes extra entities from given list to return the first (or maximum of) {@code maxCount} entities<br>
	 * 
	 * @param entities
	 *            List of entities to be pruned
	 * @param maxCount
	 *            Maximum number of entities to be returned
	 * @param tillEntity
	 *            the first (or a maximum of) {@code maxCount} entities from the given list
	 */
	public static <T extends Entity> List<T> skipEntitiesByMaxCount(List<T> entities, Integer maxCount) {
		if(maxCount == null || maxCount <= 0 || maxCount > entities.size()) {
			return entities;
		}
		
		return entities.subList(0, maxCount - 1);
	}

	/**
	 * Skips (removes) the entities from given list till (including) the given entity. <br>
	 * 
	 * @param entities
	 *            List of entities to be pruned
	 * @param tillEntity
	 *            A list of entities after skipping the ones appearing before, and including {@code tillEntity}
	 */
	public static <T extends Entity> List<T> skipEntitiesTill(List<T> entities, String tillEntity) {
		if(tillEntity == null) {
			return entities;
		}
		
		int index = indexOfEntity(entities, tillEntity);
		if(index != -1) {
			return entities.subList(index + 1, entities.size()-1);
		}
		
		// given entity not found. return the input list as it is.
		return entities;
	}

	public static <T extends Entity> int indexOfEntity(List<T> entities, String entityName) {
		int index = -1;
		for(int i = 0; i < entities.size(); i++) {
			if(entities.get(i).getName().equalsIgnoreCase(entityName)) {
				index = i;
				break;
			}
		}
		return index;
	}
}
