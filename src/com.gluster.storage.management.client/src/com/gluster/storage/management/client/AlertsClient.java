package com.gluster.storage.management.client;

import java.util.List;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.AlertListResponse;

public class AlertsClient  extends AbstractClient {
	
	private static final String RESOURCE_NAME = RESTConstants.RESOURCE_PATH_ALERTS;
	
	public AlertsClient(String securityToken) {
		super(securityToken);
	}
	
	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}
	
	@SuppressWarnings("rawtypes") 
	private Object fetchAlerts(Class responseClass) {
		return fetchResource( responseClass );
	}
	
	public List<Alert> getAlerts() {
		AlertListResponse response = (AlertListResponse) fetchAlerts(AlertListResponse.class);
		return response.getAlerts();
	}
}




