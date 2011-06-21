/**
 * tasksClient.java
 *
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.gluster.storage.management.client;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.response.TaskListResponse;
import com.gluster.storage.management.core.response.TaskResponse;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TasksClient extends AbstractClient {

	public TasksClient(String clusterName) {
		super(clusterName);
	}
	
	public TasksClient(String securityToken,String clusterName) {
		super(securityToken, clusterName);
	}
	
	@Override
	public String getResourcePath() {
		return RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESTConstants.RESOURCE_TASKS + "/";
	}
	
	@SuppressWarnings("unchecked")
	public List<TaskInfo> getAllTasks() { // TaskListResponse get only the list of taskInfo not list of Tasks
		TaskListResponse response = (TaskListResponse) fetchResource(TaskListResponse.class);
		if (response.getStatus().isSuccess()) {
			return (List<TaskInfo>) response.getData();
		} else {
			throw new GlusterRuntimeException("Exception on fetching tasks [" + response.getStatus().getMessage() + "]");
		}
	}
	
	// see startMigration @ VolumesClient, etc
	public TaskResponse pauseTask(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_PAUSE);
		
		return (TaskResponse) putRequest( taskId, TaskResponse.class, form);
	}

	public TaskResponse resumeTask(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_RESUME);
		
		return (TaskResponse) putRequest( taskId, TaskResponse.class, form);
	}

	public TaskResponse stopTask(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_STOP);
		
		return (TaskResponse) putRequest( taskId, TaskResponse.class, form);
	}

	public TaskResponse getTaskStatus(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_STATUS);
		
		return (TaskResponse) putRequest( taskId, TaskResponse.class, form);
	}
	
	public TaskResponse deleteTask(String taskId) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_DELETE);
		
		return (TaskResponse) deleteSubResource(taskId, TaskResponse.class, queryParams);
	}
}