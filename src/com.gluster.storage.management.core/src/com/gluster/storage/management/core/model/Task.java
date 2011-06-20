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

public abstract class Task {
	public enum TASK_TYPE {
		DISK_FORMAT, BRICK_MIGRATE, VOLUME_REBALANCE
	}
	
	public String[] TASK_TYPE_STR = { "Format Disk", "Migrate Brick", "Volume Rebalance" };
	
	private TaskInfo taskInfo;
	
	protected String serverName;
	
	public Task(TASK_TYPE type, String reference) {
		taskInfo = new TaskInfo();
		taskInfo.setType(type);
		taskInfo.setId(getTypeStr() + "-" + reference); // construct id
		taskInfo.setReference(reference);
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

	public abstract TaskInfo start(); 
	
	public abstract TaskInfo resume();

	public abstract TaskInfo stop();

	public abstract TaskInfo pause();
	
	public abstract TaskInfo status();
	
	public abstract void setTaskDescription();
	
}
