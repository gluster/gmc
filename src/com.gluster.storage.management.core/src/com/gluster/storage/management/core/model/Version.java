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

/**
 * 
 */
public class Version implements Comparable<Version> {
	public int major = 0;
	public int minor = 0;
	public int maintenance = 0;
	
	public Version(String version) {
		String[] versionParts = version.split(".", -1);
		major = Integer.valueOf(versionParts[0]);
		if(versionParts.length > 1) {
			minor = Integer.valueOf(versionParts[1]);
		}
		if(versionParts.length > 2) {
			maintenance = Integer.valueOf(versionParts[2]);
		}
	}

	@Override
	public int compareTo(Version newVer) {
		if(this.major < newVer.major) {
			return -1;
		} else if(this.major > newVer.major) {
			return 1;
		}
		
		// major version is same
		if(this.minor < newVer.minor) {
			return -1;
		} else if(this.minor > newVer.minor) {
			return 1;
		}
		
		// major.minor is same
		if(this.maintenance < newVer.maintenance) {
			return -1;
		} else if(this.maintenance > newVer.maintenance) {
			return 1;
		}
		
		// major.minor.maintenance is same
		return 0;
	}
}