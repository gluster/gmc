/*******************************************************************************
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
 *******************************************************************************/
package com.gluster.storage.management.server.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_RUNNING_TASKS;
import com.gluster.storage.management.core.model.Response;
import com.gluster.storage.management.core.model.RunningTask;
import com.gluster.storage.management.core.model.RunningTaskStatus;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.RunningTaskListResponse;
import com.gluster.storage.management.core.utils.StringUtil;
import com.gluster.storage.management.server.runningtasks.managers.RunningTaskManager;

@Component
@Path(RESOURCE_PATH_RUNNING_TASKS)
public class RunningTaskResource {
	
	private static final String PKG = "com.gluster.storage.management.server.runningtasks.managers";
	private static final String MANAGER = "Manager";

	@GET
	@Produces(MediaType.TEXT_XML)
	public RunningTaskListResponse getRunningTasks() {

		RunningTaskStatus status = new RunningTaskStatus();
		List<RunningTask> runningTasks = new ArrayList<RunningTask>(); 
		
		status.setCode(Status.STATUS_CODE_RUNNING);
		status.setPercentageSupported(false);

		// Volume rebalance
		RunningTask task = new RunningTask();
		task.setId("0001");
		task.setType(RunningTask.TASK_TYPES.VOLUME_REBALANCE);
		task.setReference("Volume1");
		task.setTaskInfo( task.getTaskType(task.getType()) +  "is running in volume " + task.getReference());
		task.setStatus(status);
		runningTasks.add(task);

		task = new RunningTask();
		task.setId("0002");
		task.setType(RunningTask.TASK_TYPES.VOLUME_REBALANCE);
		task.setReference("Volume2");
		task.setTaskInfo( task.getTaskType(task.getType()) +  " is running in volume " + task.getReference());
		task.setStatus(status);
		runningTasks.add(task);

		// MigrateDisk
		task = new RunningTask();
		task.setId("0003");
		task.setType(RunningTask.TASK_TYPES.MIGRATE_DISK);
		task.setReference("Volume3:server1:sda1");  // Disk reference
		task.setTaskInfo( task.getTaskType(task.getType()) +  " is running in disk " +  task.getReference() + "");
		task.setStatus(status);
		runningTasks.add(task);

		// FormatDisk
		task = new RunningTask();
		task.setId("0004");
		task.setType(RunningTask.TASK_TYPES.FORMAT_DISK);
		task.setReference("Volume1:server1:sdb1");  // Disk reference
		task.setTaskInfo( task.getTaskType(task.getType()) +  " " + task.getReference() );
		status.setPercentageSupported(true);
		status.getPercentCompleted(45);
		task.setStatus(status);
		runningTasks.add(task);
		
		return new RunningTaskListResponse(Status.STATUS_SUCCESS, runningTasks);
	}

	@SuppressWarnings("rawtypes")
	public Response startTask(@FormParam("taskType") RunningTask.TASK_TYPES taskType) {
		String taskTypeStr =  StringUtil.removeSpaces( new RunningTask().getTaskType(taskType) );
		String managerClassName = PKG + "." + taskTypeStr + MANAGER;

		Class managerClass;
		RunningTaskManager manager = null;
		try {
			managerClass = Class.forName(managerClassName);
			manager = (RunningTaskManager) managerClass.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// TODO: set form params on the manager
		Map params = null;
		manager.setFormParams(params);
		manager.start();
		return null;
	}
	
	// TODO Remove the test script for production
	public static void main(String[] args) {
		RunningTaskResource rt = new RunningTaskResource();
		RunningTaskListResponse tasks = rt.getRunningTasks();
		List<RunningTask> runningTasks = tasks.getRunningTasks();
		for( RunningTask x : runningTasks) {
			System.out.println( x.getId() +  " : " + x.getType() +  " : " + x.getTaskInfo() );
		}
	}
}

