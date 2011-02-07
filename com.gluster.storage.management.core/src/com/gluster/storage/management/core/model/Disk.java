package com.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.utils.StringUtils;

@XmlRootElement(name="Disk")
public class Disk extends Entity {
	public enum DISK_STATUS {
		READY, UNINITIALIZED, INITIALIZING, OFFLINE
	};

	private String[] DISK_STATUS_STR = { "Ready", "Uninitialized", "Initializing", "Offline" };

	private Server server;
	private Double space;
	private Double spaceInUse;
	private DISK_STATUS status;

	public Disk() {
		
	}
	
	public Double getSpace() {
		return space;
	}

	public void setSpace(Double space) {
		this.space = space;
	}
	
	public boolean isUninitialized() {
		return getStatus() == DISK_STATUS.UNINITIALIZED;
	}
	
	public boolean isOffline() {
		return getStatus() == DISK_STATUS.OFFLINE;
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

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Disk(Server server, String name, Double space, Double spaceInUse, DISK_STATUS status) {
		super(name, server);
		setServer(server);
		setSpace(space);
		setSpaceInUse(spaceInUse);
		setStatus(status);
	}

	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtils.filterString(getServer().getName() + getName() + getStatusStr(), filterString, caseSensitive);
	}
	
	public String getQualifiedName() {
		return getServer().getName() + ":" + getName();
	}
}
