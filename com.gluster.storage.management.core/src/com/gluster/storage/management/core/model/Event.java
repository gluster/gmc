package com.gluster.storage.management.core.model;

public class Event {
	public enum EVENT_TYPE {
		DISK_ADDED,
		DISK_REMOVED,
		NETWORK_INTERFACE_ADDED,
		NETWORK_INTERFACE_REMOVED
	}
	
	private EVENT_TYPE eventType;
	private Object eventData;
	
	public Event(EVENT_TYPE eventType, Object eventData) {
		this.eventType = eventType;
		this.eventData = eventData;
	}
}
