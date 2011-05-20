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
import java.util.List;

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
	
	// Convert from Disk list to Qualified bricks list 
	public static final List<String> getQualifiedBrickNames(List<Disk> diskList) {
		List<String> qualifiedBrickNames = new ArrayList<String>();
		for (Disk disk : diskList) {
			qualifiedBrickNames.add(disk.getQualifiedBrickName());
		}
		return qualifiedBrickNames;
	}
	
	public List<String> getQualifiedBrickList(List<Brick> bricks) {
		List<String> qualifiedBricks = new ArrayList<String>();
		for (Brick brick : bricks) {
			qualifiedBricks.add(brick.getQualifiedBrickName());
		}
		return qualifiedBricks;
	}
}
