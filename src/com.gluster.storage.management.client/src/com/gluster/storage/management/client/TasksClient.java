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
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.response.TaskListResponse;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TasksClient extends AbstractClient {

	public TasksClient() {
		super();
	}

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
	
	public List<TaskInfo> getAllTasks() { // TaskListResponse get only the list of taskInfo not list of Tasks
		return ((TaskListResponse) fetchResource(TaskListResponse.class)).getTaskList();
	}
	
	// see startMigration @ VolumesClient, etc
	public void pauseTask(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_PAUSE);
		
		putRequest( taskId, form);
	}

	public void resumeTask(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_RESUME);
		
		putRequest(taskId, form);
	}

	public void stopTask(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_STOP);
		
		putRequest(taskId, form);
	}

	public void getTaskStatus(String taskId) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_STATUS);
		
		putRequest(taskId, form);
	}
	
	public void deleteTask(String taskId) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_DELETE);
		
		deleteSubResource(taskId, queryParams);
	}
}
