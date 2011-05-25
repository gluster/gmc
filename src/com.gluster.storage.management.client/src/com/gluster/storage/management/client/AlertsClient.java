package com.gluster.storage.management.client;

import java.util.List;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.AlertListResponse;

public class AlertsClient  extends AbstractClient {
	
	public AlertsClient(String clusterName) {
		super(clusterName);
	}
	
	public AlertsClient(String securityToken,String clusterName) {
		super(securityToken, clusterName);
	}
	
	@Override
	public String getResourcePath() {
		return RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESTConstants.RESOURCE_ALERTS;
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
