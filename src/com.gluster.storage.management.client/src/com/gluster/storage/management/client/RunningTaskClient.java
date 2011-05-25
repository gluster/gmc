package com.gluster.storage.management.client;

import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_RUNNING_TASKS;

import com.gluster.storage.management.core.response.RunningTaskListResponse;

public class RunningTaskClient extends AbstractClient {

	public RunningTaskClient(String clusterName) {
		super(clusterName);
	}
	
	public RunningTaskClient(String securityToken, String clusterName) {
		super(securityToken, clusterName);
	}

	@Override
	public String getResourcePath() {
		return RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_RUNNING_TASKS;
	}

	public RunningTaskListResponse getRunningTasks() {
		return (RunningTaskListResponse) fetchResource(RunningTaskListResponse.class);
	}
}
