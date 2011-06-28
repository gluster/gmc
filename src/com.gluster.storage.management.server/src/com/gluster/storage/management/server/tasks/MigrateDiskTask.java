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

import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Task;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.server.utils.SshUtil;

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

	public MigrateDiskTask(String volumeName, String fromBrick, String toBrick) {
		super(TASK_TYPE.BRICK_MIGRATE, volumeName, "Brick Migration on volume [" + volumeName + "] from [" + fromBrick
				+ "] to [" + toBrick + "]", true, true, true);
		setFromBrick(fromBrick);
		setToBrick(toBrick);
	}

	public MigrateDiskTask(TaskInfo info) {
		super(info);
	}

	@Override
	public String getId() {
		return taskInfo.getType() + "-" + taskInfo.getReference() + "-" + fromBrick + "-" + toBrick;
	}

	@Override
	public void start() {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " "
				+ getToBrick() + " start";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().matches("*started successfully")) {
				taskStatus.setCode(Status.STATUS_CODE_RUNNING);
			} else {
				taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			}
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		taskStatus.setMessage(processResult.getOutput()); // Common
		getTaskInfo().setStatus(taskStatus);
	}

	

	@Override
	public void pause() {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " " + getToBrick()
		+ " pause";

		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().matches("*pause")) {
				taskStatus.setCode(Status.STATUS_CODE_PAUSE);
			} else {
				taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			}
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		taskStatus.setMessage(processResult.getOutput()); // Common
		getTaskInfo().setStatus(taskStatus);
	}
	
	
	@Override
	public void resume() {
		start();
	}
	
	@Override
	public void commit() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " " + getToBrick()
		+ " abort";
		
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().matches("*abort")) {
				taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
			} else {
				taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			}
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		taskStatus.setMessage(processResult.getOutput()); // Common
		getTaskInfo().setStatus(taskStatus);
	}

	
	@Override
	public TaskStatus checkStatus() {
		String command = "gluster volume replace-brick " + getTaskInfo().getReference() + " " + getFromBrick() + " "
				+ getToBrick() + " status";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().matches("*Migration complete")) {
				taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
			} else {
				taskStatus.setCode(Status.STATUS_CODE_RUNNING);
			}
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		taskStatus.setMessage(processResult.getOutput()); // Common
		return taskStatus;
	}
}
