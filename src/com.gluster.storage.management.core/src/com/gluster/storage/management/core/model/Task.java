/**
 * Task.java
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
package com.gluster.storage.management.core.model;

import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;

public abstract class Task {
	public String[] TASK_TYPE_STR = { "Format Disk", "Migrate Brick", "Volume Rebalance" };
	
	protected TaskInfo taskInfo;
	
	protected String serverName;
	
	public Task(TASK_TYPE type, String reference, String desc, boolean canPause, boolean canStop, boolean canCommit) {
		taskInfo = new TaskInfo();
		taskInfo.setType(type);
		taskInfo.setReference(reference);
		taskInfo.setDescription(desc);
		
		// IMPORTANT. This call must be in the end since getId may need to use the values set in above statements
		taskInfo.setName(getId()); 
	}
	
	public Task(TaskInfo taskInfo) {
		setTaskInfo(taskInfo);
	}

	public String getTypeStr() {
		return TASK_TYPE_STR[taskInfo.getType().ordinal()];
	}
	
	public TASK_TYPE getType() {
		return getTaskInfo().getType();
	}
	
	public String getOnlineServer() {
		return serverName;
	}
	
	public void setOnlineServer(String serverName) {
		this.serverName = serverName;
	}
	
	public TaskInfo getTaskInfo() {
		return taskInfo;
	}
	
	public void setTaskInfo(TaskInfo info) {
		this.taskInfo = info;  
	}
	
	public abstract String getId();

	public abstract void start(); 
	
	public abstract void resume();

	public abstract void stop();

	public abstract void pause();
	
	public abstract void commit();

	/**
	 * This method should check current status of the task and update it's taskInfo accordingly
	 */
	public abstract TaskStatus checkStatus();
}
