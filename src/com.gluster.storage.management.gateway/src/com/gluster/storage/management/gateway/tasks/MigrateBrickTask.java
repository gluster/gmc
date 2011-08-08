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
package com.gluster.storage.management.gateway.tasks;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.utils.GlusterUtil;
import com.sun.jersey.core.util.Base64;

public class MigrateBrickTask extends Task {

	private String fromBrick;
	private String toBrick;
	private Boolean autoCommit;
	private GlusterUtil glusterUtil;

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

	public MigrateBrickTask(ClusterService clusterService, String clusterName, String volumeName, String fromBrick,
			String toBrick) {
		super(clusterService, clusterName, TASK_TYPE.BRICK_MIGRATE, volumeName + "-" + fromBrick + "-" + toBrick,
				"Brick Migration on volume [" + volumeName + "] from [" + fromBrick + "] to [" + toBrick + "]", true,
				true, true);
		setFromBrick(fromBrick);
		setToBrick(toBrick);
		taskInfo.setName(getId());
		init();
	}
	
	private void init() {
		ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		glusterUtil = ctx.getBean(GlusterUtil.class);
	}

	@Override
	public String getId() {
		return new String(Base64.encode(clusterName + "-" + taskInfo.getType() + "-" + taskInfo.getReference() + "-" + fromBrick + "-"
				+ toBrick));
	}

	@Override
	public void start() {
		try {
			startMigration(getOnlineServer().getName());
		} catch (ConnectionException e) {
			// online server might have gone Offline. try with a new one.
			startMigration(getNewOnlineServer().getName());
		}
	}

	private void startMigration(String onlineServerName) {
		String volumeName = getTaskInfo().getReference().split("-")[0];
		ProcessResult processResult = glusterUtil.executeBrickMigration(onlineServerName, volumeName,
				getFromBrick(), getToBrick(), "start");
		if (processResult.getOutput().trim().matches(".*started successfully$")) {
			getTaskInfo().setStatus(
					new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, processResult.getOutput().trim())));
			return;
		}
	}

	@Override
	public void pause() {
		try {
			pauseMigration(getOnlineServer().getName());
		} catch (ConnectionException e) {
			// online server might have gone offline. try with a new one.
			pauseMigration(getNewOnlineServer().getName());
		}
	}

	private void pauseMigration(String onlineServer) {
		String volumeName = getTaskInfo().getReference().split("-")[0];
		ProcessResult processResult = glusterUtil.executeBrickMigration(onlineServer, volumeName,
				getFromBrick(), getToBrick(), "pause");
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.getOutput().trim().matches(".*paused successfully$")) { 
			taskStatus.setCode(Status.STATUS_CODE_PAUSE);
			taskStatus.setMessage(processResult.getOutput());
			getTaskInfo().setStatus(taskStatus);
			return;
		}
	}

	@Override
	public void resume() {
		start();
	}

	@Override
	public void commit() {
		try {
			commitMigration(getOnlineServer().getName());
		} catch (ConnectionException e) {
			// online server might have gone offline. try with a new one.
			commitMigration(getNewOnlineServer().getName());
		}
	}
	
	private void commitMigration(String serverName) {
		String volumeName = getTaskInfo().getReference().split("-")[0];
		ProcessResult processResult = glusterUtil.executeBrickMigration(serverName, volumeName,
				getFromBrick(), getToBrick(), "commit");
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches(".*commit successful$")) {
				taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
				taskStatus.setMessage(processResult.getOutput()); 
				getTaskInfo().setStatus(taskStatus);
			}
		}
	}

	@Override
	public void stop() {
		try {
			stopMigration(getOnlineServer().getName());
		} catch (ConnectionException e) {
			// online server might have gone offline. try with a new one.
			stopMigration(getNewOnlineServer().getName());
		}
	}

	private void stopMigration(String serverName) {
		String volumeName = getTaskInfo().getReference().split("-")[0];
		ProcessResult processResult = glusterUtil.executeBrickMigration(serverName, volumeName, getFromBrick(),
				getToBrick(), "abort");
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.getOutput().trim().matches(".*aborted successfully$")) {
			taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
			taskStatus.setMessage(processResult.getOutput());
			getTaskInfo().setStatus(taskStatus);
		}
	}

	@Override
	public TaskStatus checkStatus() {
		try {
			return checkMigrationStatus(getOnlineServer().getName());
		} catch (ConnectionException e) {
			// online server might have gone offline. try with a new one.
			return checkMigrationStatus(getNewOnlineServer().getName());
		}
	}
	
	private TaskStatus checkMigrationStatus(String serverName) {
		// For committed task, status command (CLI) is invalid, just return current status
		if (getTaskInfo().getStatus().getCode() == Status.STATUS_CODE_SUCCESS) {
			return getTaskInfo().getStatus();
		}

		TaskStatus taskStatus = new TaskStatus();
		try {
			String volumeName = getTaskInfo().getReference().split("-")[0];
			ProcessResult processResult = glusterUtil.executeBrickMigration(serverName, volumeName,
					getFromBrick(), getToBrick(), "status");
			String output = processResult.getOutput().trim();
			if (output.matches("^Number of files migrated.*Migration complete$")
					|| output.matches("^Number of files migrated = 0 .*Current file=")) {
				// Note: Workaround - if no file in the volume brick to migrate,
				// Gluster CLI is not giving proper (complete) status
				taskStatus.setCode(Status.STATUS_CODE_COMMIT_PENDING);
				if (autoCommit) {
					commitMigration(serverName);
					return getTaskInfo().getStatus(); // return the committed status
				} else {
					taskStatus.setMessage(output.replaceAll("Migration complete", "Commit pending"));
				}
			} else if (output.matches("^Number of files migrated.*Current file=.*")) {
				taskStatus.setCode(Status.STATUS_CODE_RUNNING);
			} else if (output.matches("^replace brick has been paused.*")) {
				taskStatus.setCode(Status.STATUS_CODE_PAUSE);
			} else {
				taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			}
			taskStatus.setMessage(output);
		} catch (Exception e) {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			taskStatus.setMessage(e.getMessage());
		}
		taskInfo.setStatus(taskStatus); // Update the task status
		return taskStatus;
	}
}
