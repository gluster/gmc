package com.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "alert")
public class Alert {
	public enum ALERT_TYPES { CPU_ALERT, MEMORY_ALERT, DISK_ALERT };
	
	public String[] ALERT_TYPE_STR = {"High CPU Usage", "High Memory Usage", "Low Disk Space"};

	protected String id;
	protected ALERT_TYPES type;
	protected String reference;
	protected String message;
	
	public String getAlertType( ALERT_TYPES alertType) {
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
