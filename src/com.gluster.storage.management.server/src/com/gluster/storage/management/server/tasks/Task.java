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
package com.gluster.storage.management.server.tasks;

import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.server.services.ClusterService;

public abstract class Task {
	public String[] TASK_TYPE_STR = { "Format Disk", "Migrate Brick", "Volume Rebalance" };
	
	protected TaskInfo taskInfo;	
	protected String clusterName;
	private ClusterService clusterService;
	
	public Task(ClusterService clusterService, String clusterName, TASK_TYPE type, String reference, String desc, boolean canPause, boolean canStop, boolean canCommit) {
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.setType(type);
		taskInfo.setReference(reference);
		taskInfo.setDescription(desc);
		taskInfo.setPauseSupported(canPause);
		taskInfo.setStopSupported(canStop);
		taskInfo.setCommitSupported(canCommit);
		
		init(clusterService, clusterName, taskInfo);
		
	}
	
	public Task(ClusterService clusterService, String clusterName, TaskInfo taskInfo) {
		init(clusterService, clusterName, taskInfo);
	}
	
	private void init(ClusterService clusterService, String clusterName, TaskInfo taskInfo) {
		this.clusterService = clusterService;
		setClusterName(clusterName);
		setTaskInfo(taskInfo);
	}
	
	protected GlusterServer getOnlineServer() {
		return clusterService.getOnlineServer(clusterName);
	}
	
	protected GlusterServer getNewOnlineServer() {
		return clusterService.getNewOnlineServer(clusterName);
	}
	
	protected GlusterServer getNewOnlineServer(String exceptServerName) {
		return clusterService.getNewOnlineServer(clusterName, exceptServerName);
	}

	public String getTypeStr() {
		return TASK_TYPE_STR[taskInfo.getType().ordinal()];
	}
	
	public TASK_TYPE getType() {
		return getTaskInfo().getType();
	}
	
	public String getClusterName() {
		return clusterName;
	}
	
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
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
