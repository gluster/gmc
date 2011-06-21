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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Disk;


public class GlusterCoreUtil {
	// Convert from Disk list to Qualified disk name list 
	public static final List<String> getQualifiedDiskNames(List<Disk> diskList) {
		List<String> qualifiedDiskNames = new ArrayList<String>();
		for (Disk disk : diskList) {
			qualifiedDiskNames.add(disk.getQualifiedName());
		}
		return qualifiedDiskNames;
	}
	
	public static final List<String> getQualifiedBrickList(List<Brick> bricks) {
		List<String> qualifiedBricks = new ArrayList<String>();
		for (Brick brick : bricks) {
			qualifiedBricks.add(brick.getQualifiedName());
		}
		return qualifiedBricks;
	}
	
	/**
	 * Extracts a list from a string by splitting it on given delimiter 
	 * @param input the input string
	 * @return A {@link List} of extracted tokens
	 */
	public List<String> extractList(String input, String delim) {
		String[] arr = input.split(delim);
		List<String> output = new ArrayList<String>();
		for(String str : arr) {
			String brick = str.trim();
			if(!brick.isEmpty()) {
				output.add(brick);
			}
		}
		return null;
	}

	/**
	 * Extracts a map from a string by splitting it on the given primary and secondary delimiter. e.g. The input string
	 * <i>k1=v1,k2=v2,k3=v3</i> will yield the following map:<br>
	 * k1 -> v1<br>
	 * k2 -> v2<br>
	 * k3 -> v3<br>
	 * where <b>,</b> is the primary delimiter and <b>=</b> is the secondary delimiter.
	 * 
	 * @param input
	 * @param majorDelim
	 * @param minorDelim
	 * @return Map of key value pairs
	 */
	public Map<String, String> extractMap(String input, String majorDelim, String minorDelim) {
		String[] arr = input.split(majorDelim);
		Map<String, String> output = new LinkedHashMap<String, String>();
		for(String str : arr) {
			String[] elements = str.split(minorDelim);
			if(elements.length == 2) {
				String key = elements[0].trim();
				String value = elements[1].trim();
				if(!key.isEmpty() && !value.isEmpty()) {
					output.put(key, value);
				}
			}
		}
		return output;
	}
}
