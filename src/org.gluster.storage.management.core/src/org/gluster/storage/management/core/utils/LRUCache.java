/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.core.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An LRU cache, based on <code>LinkedHashMap</code>.
 * <p>
 * This cache has a fixed maximum number of elements (<code>cacheSize</code>). If the cache is full and another entry is
 * added, the LRU (least recently used) entry is dropped.
 * 
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;
	private static final float loadFactor = 0.75f;
	private int cacheSize;

	/**
	 * Creates a new LRU cache.
	 * 
	 * @param cacheSize
	 *            the maximum number of entries that will be kept in this cache.
	 */
	public LRUCache(int cacheSize) {
		super((int) Math.ceil(cacheSize / loadFactor) + 1, loadFactor, true);
		this.cacheSize = cacheSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > cacheSize;
	}

}
