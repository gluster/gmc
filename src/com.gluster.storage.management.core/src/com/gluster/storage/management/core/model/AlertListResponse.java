package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
public class AlertListResponse  extends AbstractResponse {
	private List<Alert> alerts = new ArrayList<Alert>();

	public AlertListResponse() {
	
	}
	
	public AlertListResponse(List<Alert> alerts) {
		setAlerts(alerts);
	}
	
	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}
	
	@XmlElementWrapper(name = "alerts")
	@XmlElement(name = "alert", type=Alert.class)
	public List<Alert> getAlerts() {
		return this.alerts;
	}

	@Override
	public Object getData() {
		return getAlerts();
	}
}
