package com.gluster.storage.management.client;

import java.util.List;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Response;
import com.gluster.storage.management.core.model.RunningTask;
import com.gluster.storage.management.core.model.RunningTaskListResponse;

public class RunningTaskClient extends AbstractClient {
	private static final String RESOURCE_NAME = RESTConstants.PATH_PARAM_RUNNING_TASKS;
	
	public RunningTaskClient(String securityToken) {
		super(securityToken);
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}
	
	@SuppressWarnings("rawtypes") 
	private Object fetchRunningTasks(Class responseClass) {
		return fetchResource( responseClass );
	}
	
	public List<RunningTask> getRunningTasks() {
		RunningTaskListResponse response = (RunningTaskListResponse) fetchRunningTasks( RunningTaskListResponse.class );
		return response.getRunningTasks(); 
	}
}
