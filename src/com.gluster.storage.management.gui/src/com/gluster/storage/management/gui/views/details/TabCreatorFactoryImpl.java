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
package com.gluster.storage.management.gui.views.details;

import java.util.HashMap;
import java.util.Map;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.EntityGroup;

public class TabCreatorFactoryImpl implements TabCreatorFactory {

	private Map<String, TabCreator> tabCreatorCache = new HashMap<String, TabCreator>();

	/**
	 * Returns tab creator for given entity. The logic is as follows: <br>
	 * 1) Check if an tab creator is already created for the "class" of the entity. In case of {@link EntityGroup},
	 * append the class name with entity type <br>
	 * 2) If the tab creator is found in the cache, return it <br>
	 * 3) If not found, create one by instantiating the class "<current package>.tabcreators.<class name>TabCreator".
	 * Again, "class name" includes "entity type" in case of {@link EntityGroup} <br>
	 * 4) Add the newly created tab creator to the cache and return it
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public TabCreator getTabCreator(Entity entity) {
		Class entityClass = entity.getClass();
		String key = entityClass.getSimpleName();
		if (entityClass == EntityGroup.class) {
			// If it's an entity group, add the entity type to the key
			key += ((EntityGroup) entity).getEntityType().getSimpleName();
		}

		TabCreator tabCreator = tabCreatorCache.get(key);
		if (tabCreator == null) {
			// Not created yet. Create one and add to the cache
			String className = getClass().getPackage().getName() + ".tabcreators." + key + "TabCreator";
			try {
				Class<TabCreator> creatorFactoryClass = (Class<TabCreator>) Class.forName(className);
				tabCreator = creatorFactoryClass.newInstance();
				tabCreatorCache.put(key, tabCreator);
			} catch (ClassNotFoundException e) {
				throw new GlusterRuntimeException("Could not load creator factory class [" + className + "]", e);
			} catch (InstantiationException e) {
				throw new GlusterRuntimeException("Could not create instance of creator factory class [" + className
						+ "]", e);
			} catch (IllegalAccessException e) {
				throw new GlusterRuntimeException("Could not create instance of creator factory class [" + className
						+ "]", e);
			}
		}

		return tabCreator;
	}
}
