/**
 * TaskResource.java
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
package com.gluster.storage.management.server.resources;

import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_TASK_ID;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OPERATION;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_TASKS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Task;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.response.TaskListResponse;
import com.gluster.storage.management.core.response.TaskResponse;
import com.sun.jersey.spi.resource.Singleton;

@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_TASKS)
@Singleton
public class TasksResource {
	private Map<String, Task> tasksMap = new HashMap<String, Task>();

	public TasksResource() {
	}

	
	public void addTask(Task task) { // task should be one of MuigrateDiskTask, FormatDiskTask, etc
		tasksMap.put(task.getId(), task);
	}

	public void removeTask(Task task) {
		tasksMap.remove(task.getId());
	}

	public List<TaskInfo> getAllTasksInfo() {
		List<TaskInfo> allTasksInfo = new ArrayList<TaskInfo>();
		for (Map.Entry<String, Task> entry : tasksMap.entrySet()) {
			allTasksInfo.add(entry.getValue().getTaskInfo());
		}
		return allTasksInfo;
	}
	
	public List<Task> getAllTasks() {
		List<Task> allTasks = new ArrayList<Task>();
		for (Map.Entry<String, Task> entry : tasksMap.entrySet()) {
			allTasks.add(entry.getValue());
		}
		return allTasks;
	}

	public Task getTask(String taskId) {
		for (Map.Entry<String, Task> entry : tasksMap.entrySet()) {
			if (entry.getValue().getId().equals(taskId)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public TaskListResponse getTasks() {
		TaskListResponse taskListResponse = new TaskListResponse();
		try {
			taskListResponse.setData(getAllTasksInfo());
			taskListResponse.setStatus(new Status(Status.STATUS_CODE_SUCCESS, ""));
		} catch (GlusterRuntimeException e) {
			taskListResponse.setStatus(new Status(e));
		}
		return taskListResponse;
	}

	@PUT
	@Path("/{" + PATH_PARAM_TASK_ID + "}")
	@Produces(MediaType.TEXT_XML)
	public TaskResponse performTask(@PathParam(PATH_PARAM_TASK_ID) String taskId,
			@FormParam(FORM_PARAM_OPERATION) String taskOperation) {
		Task task = getTask(taskId);
		TaskInfo taskInfo = null;
		TaskResponse taskResponse = new TaskResponse();

		try {
			if (taskOperation.equals(RESTConstants.TASK_RESUME)) {
				taskInfo = task.resume();
			}
			if (taskOperation.equals(RESTConstants.TASK_PAUSE)) {
				taskInfo = task.pause();
			}
			if (taskOperation.equals(RESTConstants.TASK_STOP)) {
				taskInfo = task.stop();
			}
			taskResponse.setData(taskInfo);
			taskResponse.setStatus(new Status(Status.STATUS_CODE_SUCCESS, ""));
		} catch (GlusterRuntimeException e) {
			taskResponse.setStatus(new Status(e));
		}
		return taskResponse;
	}

	@DELETE
	@Path("/{" + PATH_PARAM_TASK_ID + "}")
	@Produces(MediaType.TEXT_XML)
	public TaskResponse deleteTask(@PathParam(PATH_PARAM_TASK_ID) String taskId,
			@QueryParam(FORM_PARAM_OPERATION) String taskOperation) {
		TaskResponse taskResponse = new TaskResponse();
		Task task = getTask(taskId);
		if (task == null) {
			taskResponse.setStatus( new Status(Status.STATUS_CODE_FAILURE, "No such task " + taskId + "is found ")); 
		}
		if (taskOperation.equals("delete")) {
			removeTask(task);
			taskResponse.setStatus(new Status(Status.STATUS_CODE_SUCCESS, "Task [" + taskId
						+ "] removed successfully"));
					}
		return null;
	}

}
