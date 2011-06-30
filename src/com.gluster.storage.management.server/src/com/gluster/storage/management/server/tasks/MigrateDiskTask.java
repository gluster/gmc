/**
 * MigrateDiskTask.java
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

import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.server.services.ClusterService;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.core.util.Base64;

public class MigrateDiskTask extends Task {

	private String fromBrick;
	private String toBrick;
	private Boolean autoCommit;

	private SshUtil sshUtil = new SshUtil();

	public String getFromBrick() {
		return fromBrick;
	}

	public void setFromBrick(String fromBrick) {
		this.fromBrick = fromBrick;
	}

	public String getToBrick() {
		return toBrick;
	}

	public void setToBrick(String toBrick) {
		this.toBrick = toBrick;
	}

	public Boolean getAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public MigrateDiskTask(ClusterService clusterService, String clusterName, String volumeName, String fromBrick, String toBrick) {
		super(clusterService, clusterName, TASK_TYPE.BRICK_MIGRATE, volumeName, "Brick Migration on volume ["
				+ volumeName + "] from [" + fromBrick + "] to [" + toBrick + "]", true, true, true);
		setFromBrick(fromBrick);
		setToBrick(toBrick);
		taskInfo.setName(getId());
	}

	public MigrateDiskTask(ClusterService clusterService, String clusterName, TaskInfo info) {
		super(clusterService, clusterName, info);
	}

	@Override
	public String getId() {
		return new String( Base64.encode( taskInfo.getType() + "-" + taskInfo.getReference() + "-" + fromBrick + "-" + toBrick ));
	}

	@Override
	public void start() {
		try {
			startMigration(getOnlineServer().getName());
		} catch(ConnectionException e) {
			// online server might have gone offline. try with a new one.
			startMigration(getNewOnlineServer().getName());
		}
	}

	private void startMigration(String onlineServerName) {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " "
				+ getToBrick() + " start";
		ProcessResult processResult = sshUtil.executeRemote(onlineServerName, command);
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches(".*started successfully$")) {
				getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, processResult.getOutput().trim())));
				return;
			}
		}
		
		// if we come here, it means task couldn't be started successfully.
		throw new GlusterRuntimeException(processResult.toString());
	}

	@Override
	public void pause() {
		try {
			pauseMigration(getOnlineServer().getName());
		} catch(ConnectionException e) {
			// online server might have gone offline. try with a new one.
			pauseMigration(getNewOnlineServer().getName());
		}
	}

	private void pauseMigration(String onlineServer) {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " " + getToBrick()
		+ " pause";

		ProcessResult processResult = sshUtil.executeRemote(onlineServer, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().matches("*pause")) { //TODO replace correct pattern to identify the pause status
				taskStatus.setCode(Status.STATUS_CODE_PAUSE);
				taskStatus.setMessage(processResult.getOutput());
				getTaskInfo().setStatus(taskStatus);
				return;
			}
		} 
		
		// if we reach here, it means rebalance start failed.
		throw new GlusterRuntimeException(processResult.toString());
	}
	
	
	@Override
	public void resume() {
		start();
	}
	
	@Override
	public void commit() {
		try {
			commitMigration(getOnlineServer().getName());
		} catch(ConnectionException e) {
			// online server might have gone offline. try with a new one.
			commitMigration(getNewOnlineServer().getName());
		}
	}

	@Override
	public void stop() {
		try {
			stopMigration(getOnlineServer().getName());
		} catch(ConnectionException e) {
			// online server might have gone offline. try with a new one.
			stopMigration(getNewOnlineServer().getName());
		}
	}

	private void stopMigration(String serverName) {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " " + getToBrick()
		+ " abort";
		
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches(".*aborted successfully$")) {
				taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
				taskStatus.setMessage(processResult.getOutput());
				getTaskInfo().setStatus(taskStatus);
				return;
			} 
		} 
		
		// if we reach here, it means rebalance start failed.
		throw new GlusterRuntimeException(processResult.toString());
	}

	
	@Override
	public TaskStatus checkStatus() {
		try {
			return checkMigrationStatus(getOnlineServer().getName());
		} catch(ConnectionException e) {
			// online server might have gone offline. try with a new one.
			return checkMigrationStatus(getNewOnlineServer().getName());
		}
	}
	
	
	public void commitMigration(String serverName) {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " "
		+ getToBrick() + " commit";
		
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches(".*commit successful$")) {
				taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
				taskStatus.setMessage(processResult.getOutput()); // Common
				getTaskInfo().setStatus(taskStatus);
				return;
			}
		}
		
		// if we reach here, it means rebalance start failed.
		throw new GlusterRuntimeException(processResult.toString());
	}
	

	private TaskStatus checkMigrationStatus(String serverName) {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " "
				+ getToBrick() + " status";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches("^Number of files migrated.*Migration complete$")) {
				taskStatus.setCode(Status.STATUS_CODE_COMMIT_PENDING);
				if (autoCommit) {
					commitMigration(serverName);
				}
			} else if (	processResult.getOutput().trim().matches("^Number of files migrated.*Current file=.*")) {
				taskStatus.setCode(Status.STATUS_CODE_RUNNING);
			} else {
				taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			}
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
				
		taskStatus.setMessage(processResult.getOutput()); // common
		taskInfo.setStatus(taskStatus); // Update the task status
		return taskStatus;
	}
}
