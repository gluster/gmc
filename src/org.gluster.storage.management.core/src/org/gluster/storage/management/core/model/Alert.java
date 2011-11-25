package org.gluster.storage.management.core.model;

import org.eclipse.osgi.internal.signedcontent.Base64;

public class Alert extends Entity {

	public enum ALERT_TYPES {
		CPU_USAGE_ALERT, MEMORY_USAGE_ALERT, DISK_USAGE_ALERT, OFFLINE_VOLUME_BRICKS_ALERT, OFFLINE_SERVERS_ALERT, OFFLINE_VOLUME_ALERT
	};

	public static final String[] ALERT_TYPE_STR = { "High CPU Usage", "High Memory Usage", "Low Disk Space",
			"Offline Brick", "Offline Server", "Offline Volume" };

	//  protected String id;
	protected ALERT_TYPES type;
	protected String reference; // [for server- "Server", for Disk- "Server:disk", for volume- "Volume:Server:disk"]
	protected String message;

	public String getAlertType() {
		return ALERT_TYPE_STR[type.ordinal()];
	}

	public Alert() {
	}

	public Alert(ALERT_TYPES type, String reference, String Message) {
		setType(type);
		setReference(reference);
		setMessage(Message);
		setId(buildAlertId());
	}

	public String buildAlertId() {
		return Base64.encode((getAlertType() + "-" + getReference()).getBytes()).toString();
	}

	public String getId() {
		return getName();
	}

	public void setId(String id) {
		setName(id);
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

	public void copyFrom(Alert alert) {
		this.setId(alert.getId());
		this.setReference(alert.getReference());
		this.setType(alert.getType());
		this.setMessage(alert.getMessage());
	}
}
