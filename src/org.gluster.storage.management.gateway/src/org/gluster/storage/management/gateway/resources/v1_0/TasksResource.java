/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Gateway.
 *
 * Gluster Management Gateway is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Gateway is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.gateway.resources.v1_0;

import static org.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OPERATION;
import static org.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static org.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_TASK_ID;
import static org.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static org.gluster.storage.management.core.constants.RESTConstants.RESOURCE_TASKS;

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

import org.gluster.storage.management.core.constants.RESTConstants;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.exceptions.GlusterValidationException;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;
import org.gluster.storage.management.core.response.TaskInfoListResponse;
import org.gluster.storage.management.gateway.tasks.Task;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.Singleton;

@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_TASKS)
@Singleton
@Component

public class TasksResource extends AbstractResource {
	private Map<String, Map<String, Task>> tasksMap = new HashMap<String, Map<String, Task>>();

	public TasksResource() {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addTask(String clusterName, Task task) {
		Map clusterTaskMap = tasksMap.get(clusterName);
		if(clusterTaskMap == null) {
			clusterTaskMap = new HashMap<String, Task>();
			tasksMap.put(clusterName, clusterTaskMap);
		}
		// Remove the task if already exist
		if (clusterTaskMap.containsKey(task.getId())) {
			 removeTask(clusterName, task);
		}
		clusterTaskMap.put(task.getId(), task);
	}

	@SuppressWarnings("rawtypes")
	public void removeTask(String clusterName, Task task) {
		Map clusterTaskMap = tasksMap.get(clusterName);
		if (clusterTaskMap != null) {
			clusterTaskMap.remove(task.getId());
		}
	}

	public List<TaskInfo> getAllTasksInfo(String clusterName) {
		Map<String, Task> clusterTasksMap = tasksMap.get(clusterName);
		List<TaskInfo> allTasksInfo = new ArrayList<TaskInfo>();
		if ( clusterTasksMap == null) {
			return allTasksInfo;
		}
		for (Map.Entry<String, Task> entry : clusterTasksMap.entrySet()) {
			checkTaskStatus(clusterName, entry.getKey());
			allTasksInfo.add(entry.getValue().getTaskInfo()); // TaskInfo with latest status 
		}
		return allTasksInfo;
	}

	public Task getTask(String clusterName, String taskId) {
		Map<String, Task> clusterTasksMap = tasksMap.get(clusterName);
		for (Map.Entry<String, Task> entry : clusterTasksMap.entrySet()) {
			if (entry.getValue().getId().equals(taskId)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public List<Task> getAllTasks(String clusterName) {
		Map<String, Task> clusterTasksMap = tasksMap.get(clusterName);
		List<Task> tasks = new ArrayList<Task>();
		for (Map.Entry<String, Task> entry : clusterTasksMap.entrySet()) {
			checkTaskStatus(clusterName, entry.getKey());
			tasks.add( (Task) entry.getValue());
		}
		return tasks;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getTasks(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		try {
			return okResponse(new TaskInfoListResponse(getAllTasksInfo(clusterName)), MediaType.APPLICATION_XML);
		} catch (GlusterRuntimeException e) {
			return errorResponse(e.getMessage());
		}
	}
	
	@GET
	@Path("/{" + PATH_PARAM_TASK_ID + "}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getTaskStatus(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_TASK_ID) String taskId) {
		try {
			Task task = checkTaskStatus(clusterName, taskId);
			return okResponse(task.getTaskInfo(), MediaType.APPLICATION_XML);
		} catch (GlusterRuntimeException e) {
			return errorResponse(e.getMessage());
		}
	}

	private Task checkTaskStatus(String clusterName, String taskId) {
		Task task = getTask(clusterName, taskId);
		// No status check required if the task already complete or failure
		if (task.getTaskInfo().getStatus().getCode() == Status.STATUS_CODE_FAILURE
				|| task.getTaskInfo().getStatus().getCode() == Status.STATUS_CODE_SUCCESS) {
			return task;
		}
		task.getTaskInfo().setStatus(task.checkStatus());
		return task;
	}

	@PUT
	@Path("/{" + PATH_PARAM_TASK_ID + "}")
	@Produces(MediaType.APPLICATION_XML)
	public Response performTask(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_TASK_ID) String taskId, @FormParam(FORM_PARAM_OPERATION) String taskOperation) {
		Task task = getTask(clusterName, taskId);
		
		try {
			if (taskOperation.equals(RESTConstants.TASK_RESUME)) {
				task.resume();
			} else if (taskOperation.equals(RESTConstants.TASK_PAUSE)) {
				task.pause();
			} else if (taskOperation.equals(RESTConstants.TASK_STOP)) {
				// task.stop();
				clearTask(clusterName, taskId, taskOperation); // Stop and remove from the task list
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
	public Response clearTask(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_TASK_ID) String taskId, @QueryParam(FORM_PARAM_OPERATION) String taskOperation) {
		Task task = getTask(clusterName, taskId);
		if (task == null) {
			return notFoundResponse("Requested task not found!");
		}
		
		if(clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Parameter [" + PATH_PARAM_CLUSTER_NAME + "] is missing in request!");
		}
		
		if(taskOperation == null || taskOperation.isEmpty()) {
			int taskStatus = task.getTaskInfo().getStatus().getCode();
			if (taskStatus == Status.STATUS_CODE_SUCCESS || taskStatus == Status.STATUS_CODE_FAILURE) {
				taskOperation = RESTConstants.TASK_DELETE;
			} else {
				taskOperation = RESTConstants.TASK_STOP;
			}
//			return badRequestResponse("Parameter [" + FORM_PARAM_OPERATION + "] is missing in request!");
		}
		
		if(!taskOperation.equals(RESTConstants.TASK_STOP) && !taskOperation.equals(RESTConstants.TASK_DELETE)) {
			return badRequestResponse("Invalid value [" + taskOperation + "] for parameter [" + FORM_PARAM_OPERATION
					+ "]");
		}
		
		try {
			if (taskOperation.equals(RESTConstants.TASK_STOP)) {
				task.stop();
				// On successful, intentionally stopped task can be removed from task list also
				taskOperation = RESTConstants.TASK_DELETE;
			}

			if (taskOperation.equals(RESTConstants.TASK_DELETE)) {
				removeTask(clusterName, task);
			}
			
			return noContentResponse();
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}
	}
}
