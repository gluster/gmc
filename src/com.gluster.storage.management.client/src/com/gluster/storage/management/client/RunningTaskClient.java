package com.gluster.storage.management.client;

import java.util.List;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Response;
import com.gluster.storage.management.core.model.RunningTask;
import com.gluster.storage.management.core.response.RunningTaskListResponse;

public class RunningTaskClient extends AbstractClient {
	
	public RunningTaskClient(String securityToken) {
		super(securityToken);
	}

	@Override
	public String getResourceName() {
		return RESTConstants.RESOURCE_PATH_RUNNING_TASKS;
	}
	
	@SuppressWarnings("rawtypes") 
	private Object fetchRunningTasks(Class responseClass) {
		return fetchResource( responseClass );
	}
	
	public RunningTaskListResponse getRunningTasks() {
		return (RunningTaskListResponse) fetchRunningTasks( RunningTaskListResponse.class );
	}
}
