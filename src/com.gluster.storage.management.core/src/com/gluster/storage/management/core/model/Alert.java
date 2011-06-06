package com.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "alert")
public class Alert {

	public enum ALERT_TYPES {
		CPU_USAGE_ALERT, MEMORY_USAGE_ALERT, DISK_USAGE_ALERT, OFFLINE_VOLUME_BRICKS_ALERT, OFFLINE_SERVERS_ALERT
	};

	public static final String[] ALERT_TYPE_STR = { "High CPU Usage", "High Memory Usage", "Low Disk Space",
			"Offline Brick", "Offline Server" };

	protected String id;
	protected ALERT_TYPES type;
	protected String reference; // [for server- "Server", for Disk- "Server:disk", for volume- "Volume:Server:disk"]
	protected String message;

	public String getAlertType(ALERT_TYPES alertType) {
		return ALERT_TYPE_STR[alertType.ordinal()];
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ALERT_TYPES getType() {
		return type;
	}

	public void setType(ALERT_TYPES type) {
		this.type = type;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
