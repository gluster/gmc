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

import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.utils.StringUtil;

@XmlRootElement(name="Disk")
public class Disk extends Entity {
	public enum DISK_STATUS {
		READY, UNINITIALIZED, INITIALIZING, IO_ERROR
	};

	private String[] DISK_STATUS_STR = { "Ready", "Uninitialized", "Initializing", "I/O Error" };

	private String serverName;
	private String mountPoint;
	private String description;
	private Double space;
	private Double spaceInUse;
	private DISK_STATUS status;

	public Disk() {
		
	}
	
	public Double getSpace() {
		return space;
	}
	
	public Double getFreeSpace() {
		return getSpace() - getSpaceInUse();
	}

	public void setSpace(Double space) {
		this.space = space;
	}
	
	public boolean isUninitialized() {
		return getStatus() == DISK_STATUS.UNINITIALIZED;
	}
	
	public boolean hasErrors() {
		return getStatus() == DISK_STATUS.IO_ERROR;
	}
	
	public boolean isReady() {
		return getStatus() == DISK_STATUS.READY;
	}
	
	public DISK_STATUS getStatus() {
		return status;
	}

	public String getStatusStr() {
		return DISK_STATUS_STR[getStatus().ordinal()];
	}

	public void setStatus(DISK_STATUS status) {
		this.status = status;
	}

	public Double getSpaceInUse() {
		return spaceInUse;
	}

	public void setSpaceInUse(Double spaceInUse) {
		this.spaceInUse = spaceInUse;
	}

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

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public Disk(Server server, String name, String mountPoint, Double space, Double spaceInUse, DISK_STATUS status) {
		super(name, server);
		setServerName(server != null ? server.getName() : "");
		setMountPoint(mountPoint);
		setSpace(space);
		setSpaceInUse(spaceInUse);
		setStatus(status);
	}

	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtil.filterString(getServerName() + getName() + getStatusStr(), filterString, caseSensitive);
	}
	
	public String getQualifiedName() {
		return getServerName() + ":" + getName();
	}
	
	public String getQualifiedBrickName(String volumeName) {
		return getServerName() + ":" + getMountPoint() + File.separator + volumeName;
	}
}
