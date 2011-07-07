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
package com.gluster.storage.management.server.resources.v1_0;

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OPERATION;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_TASK_ID;
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
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.response.TaskInfoListResponse;
import com.gluster.storage.management.server.tasks.Task;
import com.sun.jersey.spi.resource.Singleton;

@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_TASKS)
@Singleton
@Component
public class TasksResource extends AbstractResource {
	private Map<String, Task> tasksMap = new HashMap<String, Task>();

	public TasksResource() {
	}

	public void addTask(Task task) {
		tasksMap.put(task.getId(), task);
	}

	public void removeTask(Task task) {
		tasksMap.remove(task.getId());
	}

	public List<TaskInfo> getAllTasksInfo() {
		List<TaskInfo> allTasksInfo = new ArrayList<TaskInfo>();
		for (Map.Entry<String, Task> entry : tasksMap.entrySet()) {
			checkTaskStatus(entry.getKey());
			allTasksInfo.add(entry.getValue().getTaskInfo()); // TaskInfo with latest status 
		}
		return allTasksInfo;
	}

	public Task getTask(String taskId) {
		for (Map.Entry<String, Task> entry : tasksMap.entrySet()) {
			if (entry.getValue().getId().equals(taskId)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public List<Task> getAllTasks() {
		List<Task> tasks = new ArrayList<Task>();
		for (Map.Entry<String, Task> entry : tasksMap.entrySet()) {
			checkTaskStatus(entry.getKey());
			tasks.add( (Task) entry.getValue());
		}
		return tasks;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getTasks() {
		try {
			return okResponse(new TaskInfoListResponse(getAllTasksInfo()), MediaType.APPLICATION_XML);
		} catch (GlusterRuntimeException e) {
			return errorResponse(e.getMessage());
		}
	}
	
	@GET
	@Path("/{" + PATH_PARAM_TASK_ID + "}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getTaskStatus( @PathParam(PATH_PARAM_TASK_ID) String taskId) {
		try {
			Task task = checkTaskStatus(taskId);
			return okResponse(task.getTaskInfo(), MediaType.APPLICATION_XML);
		} catch (GlusterRuntimeException e) {
			return errorResponse(e.getMessage());
		}
	}

	private Task checkTaskStatus(String taskId) {
		Task task = getTask(taskId);
		task.getTaskInfo().setStatus(task.checkStatus());
		return task;
	}

	@PUT
	@Path("/{" + PATH_PARAM_TASK_ID + "}")
	@Produces(MediaType.APPLICATION_XML)
	public Response performTask(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_TASK_ID) String taskId, @FormParam(FORM_PARAM_OPERATION) String taskOperation) {
		Task task = getTask(taskId);
		
		try {
			if (taskOperation.equals(RESTConstants.TASK_RESUME)) {
				task.resume();
			} else if (taskOperation.equals(RESTConstants.TASK_PAUSE)) {
				task.pause();
			} else if (taskOperation.equals(RESTConstants.TASK_STOP)) {
				// task.stop();
				clearTask(taskId, taskOperation); // Stop and remove from the task list
			} else if (taskOperation.equals(RESTConstants.TASK_COMMIT)) {
				task.commit();
			}
			return (Response) noContentResponse();
		} catch(GlusterValidationException ve) {
			return badRequestResponse(ve.getMessage());
		} catch (GlusterRuntimeException e) {
			return errorResponse(e.getMessage());
		}
	}

	@DELETE
	@Path("/{" + PATH_PARAM_TASK_ID + "}")
	@Produces(MediaType.APPLICATION_XML)
	public Response clearTask(@PathParam(PATH_PARAM_TASK_ID) String taskId,
			@QueryParam(FORM_PARAM_OPERATION) String taskOperation) {
		Task task = getTask(taskId);
		if (task == null) {
			return notFoundResponse("Task [" + taskId + "] not found!");
		}
		
		if(taskOperation == null || taskOperation.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_OPERATION + "] is missing in request!");
		}
		
		if(!taskOperation.equals(RESTConstants.TASK_STOP) && !taskOperation.equals(RESTConstants.TASK_DELETE)) {
			return badRequestResponse("Invalid value [" + taskOperation + "] for parameter [" + FORM_PARAM_OPERATION
					+ "]");
		}
		
		try {
			if (taskOperation.equals(RESTConstants.TASK_STOP)) {
				task.stop();
				// On successfully stopping the task, we can delete (forget) it as it is no more useful
				taskOperation = RESTConstants.TASK_DELETE;
			}

			if (taskOperation.equals(RESTConstants.TASK_DELETE)) {
				removeTask(task);
			}
			
			return noContentResponse();
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}
	}
}
