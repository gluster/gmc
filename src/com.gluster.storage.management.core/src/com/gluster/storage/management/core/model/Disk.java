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

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.StringUtil;

@XmlRootElement(name="disk")
public class Disk extends Device {
	private String description;
	
	// interface = pci, raid0, raid3, etc
	private String diskInterface;
	
	private Collection<Partition> partitions = new ArrayList<Partition>();
	
	// In case of a software raid, the disk will contain an array of other disks
	private Collection<Disk> raidDisks;

	public Disk() {
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@XmlElement(name="interface")
	public String getDiskInterface() {
		return diskInterface;
	}

	public void setDiskInterface(String diskInterface) {
		this.diskInterface = diskInterface;
	}

	@XmlElementWrapper(name="raidDisks")
	@XmlElement(name="disk", type=Disk.class)
	public Collection<Disk> getRaidDisks() {
		return raidDisks;
	}

	public void setRaidDisks(Collection<Disk> raidDisks) {
		this.raidDisks = raidDisks;
	}

	public void setPartitions(Collection<Partition> partitions) {
		this.partitions = partitions;
	}

	@XmlElementWrapper(name="partitions")
	@XmlElement(name="partition", type=Partition.class)
	public Collection<Partition> getPartitions() {
		return partitions;
	}
	
	public boolean hasPartitions() {
		return (partitions != null && partitions.size() > 0);
	}

	public Disk(Server server, String name, String mountPoint, Double space, Double spaceInUse, DEVICE_STATUS status) {
		super(server, name, mountPoint, space, spaceInUse, status);
	}

	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		if (StringUtil.filterString(getServerName() + getName() + getStatusStr() + getSpace() + getFreeSpace()
				+ getType() + getDescription(), filterString, caseSensitive)) {
			return true;
		}
		
		// disk doesn't match. check if any of the partitions of this disk match the filter
		for(Partition partition : getPartitions()) {
			if(partition.filter(filterString, caseSensitive)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Disk)) {
			return false;
		}
		Disk disk = (Disk)obj;
		
		if (!(super.equals(obj) && getDescription().equals(disk.getDescription()) && getDiskInterface().equals(
				disk.getDiskInterface()))) {
			return false;
		}
		
		if (raidDisks != null) {
			for (Disk raidDisk : raidDisks) {
				// check if the disk contains same raid disks
				if (!(raidDisk.equals(GlusterCoreUtil.getEntity(disk.getRaidDisks(), raidDisk.getName(), false)))) {
					return false;
				}
			}
		}
		
//		// check if the disk contains same partitions
//		if (partitions != null) {
//			for (Partition partition : partitions) {
//				if (!(partition.equals(GlusterCoreUtil.getEntity(disk.getPartitions(), partition.getName(), false)))) {
//					return false;
//				}
//			}
//		}
		return true;
	}

	public void copyFrom(Disk newDisk) {
		setName(newDisk.getName());
		setDescription(newDisk.getDescription());
		setMountPoint(newDisk.getMountPoint());
		setServerName(newDisk.getServerName());
		setStatus(newDisk.getStatus());
		setSpace(newDisk.getSpace());
		setSpaceInUse(newDisk.getSpaceInUse());
	}
	
	@Override
	public boolean isReady() {
		if (hasPartitions()) {
			for (Partition partition : getPartitions()) {
				if (partition.isReady()) {
					return true;
				}
			}
			return false;
		} else {
			return super.isReady();
		}
	}

	@Override
	public Double getSpace() {
		Double space = 0d;
		if (hasPartitions()) {
			for (Partition partition : getPartitions()) {
				if (partition.isInitialized()) {
					space += partition.getSpace();
				}
			}
			return space;
		} else {
			return super.getSpace();
		}
	}

	@Override
	public Double getSpaceInUse() {
		Double spaceInUse = 0d;
		if (hasPartitions()) {
			for (Partition partition : getPartitions()) {
				if (partition.isInitialized()) {
					spaceInUse += partition.getSpaceInUse();
				}
			}
			return spaceInUse;
		} else {
			return super.getSpaceInUse();
		}
	}
}