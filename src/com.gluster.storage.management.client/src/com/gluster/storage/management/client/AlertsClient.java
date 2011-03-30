package com.gluster.storage.management.client;

import java.util.List;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.AlertListResponse;

public class AlertsClient  extends AbstractClient {
	
	public AlertsClient(String securityToken) {
		super(securityToken);
	}
	
	@Override
	public String getResourceName() {
		return RESTConstants.RESOURCE_PATH_ALERTS;
	}
	
	@SuppressWarnings("rawtypes") 
	private Object fetchAllAlerts(Class responseClass) {
		return fetchResource( responseClass );
	}
	
	public List<Alert> getAllAlerts() {
		AlertListResponse response = (AlertListResponse) fetchAllAlerts(AlertListResponse.class);
		return response.getAlerts();
	}
}




