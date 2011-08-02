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

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.gluster.storage.management.core.utils.StringUtil;


/**
 *
 */
public class Device extends Entity {
	public enum DEVICE_STATUS {
		//TODO: Status "READY" to be removed after python script is changed accordingly
		INITIALIZED, UNINITIALIZED, INITIALIZING, IO_ERROR, UNKNOWN
	};
	
	public enum DEVICE_TYPE {
		DATA, BOOT, SWAP, UNKNOWN
	};

	private static final String[] DEVICE_STATUS_STR = { "Initialized", "Uninitialized", "Initializing", "I/O Error", "Unknown" };
	private static final String[] DEVICE_TYPE_STR = { "Data", "Boot", "Swap", "Unknown" };

	// type = data, boot, other
	private DEVICE_TYPE type;
	
	private String fsType;
	private String fsVersion;
	
	private String serverName;
	private String mountPoint;
	
	private Double space = 0.0;
	private Double spaceInUse = 0.0;
	private DEVICE_STATUS status;

	public Device() {
	}
	
	public Device(Server server, String name, String mountPoint, Double space, Double spaceInUse, DEVICE_STATUS status) {
		super(name, server);
		setServerName(server != null ? server.getName() : "");
		setMountPoint(mountPoint);
		setSpace(space);
		setSpaceInUse(spaceInUse);
		setStatus(status);
	}
	
	@XmlElement(name="size")
	public Double getSpace() {
		return space;
	}
	
	public Double getFreeSpace() {
		return (getSpace() - getSpaceInUse());
	}

	public void setSpace(Double space) {
		this.space = space;
	}
	
	public boolean isUninitialized() {
		return getStatus() == DEVICE_STATUS.UNINITIALIZED;
	}
	
	public boolean hasErrors() {
		return getStatus() == DEVICE_STATUS.IO_ERROR;
	}
	
	public boolean isInitialized() {
		return getStatus() == DEVICE_STATUS.INITIALIZED;
	}
	
	public boolean isReady() {
		return (getStatus() == DEVICE_STATUS.INITIALIZED && getType() == DEVICE_TYPE.DATA);
	}
	
	public DEVICE_STATUS getStatus() {
		return status;
	}

	public String getStatusStr() {
		if (getStatus() == null) {
			// Return as Unknown
			return DEVICE_STATUS_STR[DEVICE_STATUS.UNKNOWN.ordinal()]; 
		}

		if(isReady()) {
			return "Available";
		}
		return DEVICE_STATUS_STR[getStatus().ordinal()];
	}

	public void setStatus(DEVICE_STATUS status) {
		this.status = status;
	}

	public Double getSpaceInUse() {
		return spaceInUse;
	}

	public void setSpaceInUse(Double spaceInUse) {
		this.spaceInUse = spaceInUse;
	}

	@XmlTransient
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public void setMountPoint(String mountPoint) {
		this.mountPoint = mountPoint;
	}

	public String getMountPoint() {
		return mountPoint;
	}
	
	public DEVICE_TYPE getType() {
		return type;
	}
	
	public String getTypeStr() {
		return DEVICE_TYPE_STR[type.ordinal()];
	}

	public void setType(DEVICE_TYPE diskType) {
		this.type = diskType;
	}
	
	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getFsVersion() {
		return fsVersion;
	}

	public void setFsVersion(String fsVersion) {
		this.fsVersion = fsVersion;
	}
	
	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtil.filterString(getServerName() + getName() + getStatusStr() + getSpace() + getFreeSpace()
				+ getType(), filterString, caseSensitive);
	}
	
	public String getQualifiedName() {
		return getServerName() + ":" + getName();
	}
	
	public String getQualifiedBrickName(String volumeName) {
		return getServerName() + ":" + getMountPoint() + "/" + volumeName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof Device)) {
			return false;
		}
		
		Device device = (Device)obj;
		
		String oldMountPoint = (getMountPoint() == null ? "" : getMountPoint());
		String oldFsType = (getFsType() == null ? "" : getFsType());
		String oldFsVersion = (getFsVersion() == null ? "" : getFsVersion()); 
		
		String newMountPoint = (device.getMountPoint() == null ? "" : getMountPoint());
		String newFsType = (device.getFsType() == null ? "" : getFsType());
		String newFsVersion = (device.getFsVersion() == null ? "" : getFsVersion()); 

		if (getName().equals(device.getName()) && getServerName().equals(device.getServerName())
				&& oldMountPoint.equals(newMountPoint) && getStatus() == device.getStatus()
				&& getSpace().equals(device.getSpace()) && getSpaceInUse().equals(device.getSpaceInUse())
				&& oldFsType.equals(newFsType) && oldFsVersion.equals(newFsVersion)
				&& getType() == device.getType()) {
			return true;
		}
		
		return false;
	}

	public void copyFrom(Device newDevice) {
		setName(newDevice.getName());
		setMountPoint(newDevice.getMountPoint());
		setServerName(newDevice.getServerName());
		setStatus(newDevice.getStatus());
		setSpace(newDevice.getSpace());
		setSpaceInUse(newDevice.getSpaceInUse());
	}
}
