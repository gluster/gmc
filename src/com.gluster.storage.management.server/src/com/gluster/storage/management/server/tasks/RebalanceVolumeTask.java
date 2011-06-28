/**
 * RebalanceVolumeTask.java
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

public class RebalanceVolumeTask extends Task {

	private String layout;
	private SshUtil sshUtil = new SshUtil();

	public RebalanceVolumeTask(TaskInfo taskInfo) {
		super(taskInfo);
	}

	public RebalanceVolumeTask(String volumeName) {
		super(TASK_TYPE.VOLUME_REBALANCE, volumeName, "Volume rebalance running on " + volumeName, false, true, false);
	}

	@Override
	public String getId() {
		return taskInfo.getType() + "-" + taskInfo.getReference();
	}

	@Override
	public void start() {
		String command = "gluster volume rebalance " + getTaskInfo().getReference() + " " + getLayout() + " start";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches(".*has been successful$")) {
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
	public void resume() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, "Pause/Resume is not supported in Volume Rebalance")));
	}

	@Override
	public void stop() {
		String command = "gluster volume rebalance " + getTaskInfo().getReference() + " stop";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches(".*has been successful$")) {
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
	public void pause() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, "Pause/Resume is not supported in Volume Rebalance")));
	}

	@Override
	public TaskStatus checkStatus() {
		String command = "gluster volume rebalance " + getTaskInfo().getReference() + " status";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches("Rebalance completed!")) {
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

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getLayout() {
		return layout;
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
	}
}
