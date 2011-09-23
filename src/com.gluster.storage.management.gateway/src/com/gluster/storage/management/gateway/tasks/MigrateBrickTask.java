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
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.services.GlusterInterfaceService;
import com.gluster.storage.management.gateway.utils.ServerUtil;
import com.sun.jersey.core.util.Base64;

public class MigrateBrickTask extends Task {

	private String fromBrick;
	private String toBrick;
	private Boolean autoCommit;
	private GlusterInterfaceService glusterInterface;
	protected ServerUtil serverUtil;

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
		super(clusterService, clusterName, TASK_TYPE.BRICK_MIGRATE, volumeName + "#" + fromBrick + "#" + toBrick,
				"Brick Migration on volume [" + volumeName + "] from [" + fromBrick + "] to [" + toBrick + "]", true,
				true, true);
		setFromBrick(fromBrick);
		setToBrick(toBrick);
		taskInfo.setName(getId());
		init();
	}
	
	private void init() {
		ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		glusterInterface = ctx.getBean(GlusterInterfaceService.class);
		serverUtil = ctx.getBean(ServerUtil.class);
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
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone Offline. try with a new one.
				startMigration(getNewOnlineServer().getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}

	private void startMigration(String onlineServerName) {
		String volumeName = getTaskInfo().getReference().split("#")[0];
		glusterInterface.startBrickMigration(onlineServerName, volumeName, getFromBrick(), getToBrick());
		getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, "Brick Migration Started.")));
	}

	@Override
	public void pause() {
		try {
			pauseMigration(getOnlineServer().getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone offline. try with a new one.
				pauseMigration(getNewOnlineServer().getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}

	private void pauseMigration(String onlineServer) {
		String volumeName = getTaskInfo().getReference().split("#")[0];
		glusterInterface.pauseBrickMigration(onlineServer, volumeName, getFromBrick(), getToBrick());
		TaskStatus taskStatus = new TaskStatus();
		taskStatus.setCode(Status.STATUS_CODE_PAUSE);
		taskStatus.setMessage("Brick Migration Paused");
		getTaskInfo().setStatus(taskStatus);
	}

	@Override
	public void resume() {
		start();
	}

	@Override
	public void commit() {
		try {
			commitMigration(getOnlineServer().getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone offline. try with a new one.
				commitMigration(getNewOnlineServer().getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}
	
	private void commitMigration(String serverName) {
		String volumeName = getTaskInfo().getReference().split("#")[0];
		glusterInterface.commitBrickMigration(serverName, volumeName, getFromBrick(), getToBrick());
		TaskStatus taskStatus = new TaskStatus();
		taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
		taskStatus.setMessage("Brick Migration Committed.");
		getTaskInfo().setStatus(taskStatus);
	}

	@Override
	public void stop() {
		try {
			stopMigration(getOnlineServer().getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone offline. try with a new one.
				stopMigration(getNewOnlineServer().getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}

	private void stopMigration(String serverName) {
		String volumeName = getTaskInfo().getReference().split("#")[0];
		glusterInterface.stopBrickMigration(serverName, volumeName, getFromBrick(), getToBrick());
		TaskStatus taskStatus = new TaskStatus();
		taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
		taskStatus.setMessage("Brick Migration Stopped");
		getTaskInfo().setStatus(taskStatus);
	}

	@Override
	public TaskStatus checkStatus() {
		try {
			return checkMigrationStatus(getOnlineServer().getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone offline. try with a new one.
				return checkMigrationStatus(getNewOnlineServer().getName());
			} 
		}
		return null;
	}
	
	private TaskStatus checkMigrationStatus(String serverName) {
		// For committed task, status command (CLI) is invalid, just return current status
		if (taskInfo.getStatus().getCode() == Status.STATUS_CODE_SUCCESS) {
			return taskInfo.getStatus();
		}

		String volumeName = getTaskInfo().getReference().split("#")[0];
		TaskStatus taskStatus = glusterInterface.checkBrickMigrationStatus(serverName, volumeName, getFromBrick(),
				getToBrick());
		if (autoCommit && taskStatus.isCommitPending()) {
			commitMigration(serverName);
			return taskInfo.getStatus(); // return the committed status
		}

		taskInfo.setStatus(taskStatus); // Update the task status
		return taskStatus;
	}
}
