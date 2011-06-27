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
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.server.utils.SshUtil;

public class RebalanceVolumeTask extends Task {

	private String layout;
	private SshUtil sshUtil = new SshUtil();

	public RebalanceVolumeTask(TaskInfo taskInfo) {
		super(taskInfo);
	}

	public RebalanceVolumeTask(String volumeName) {
		super(TASK_TYPE.VOLUME_REBALANCE, volumeName);
		setTaskDescription();
		getTaskInfo().setCanPause(false);
		getTaskInfo().setCanStop(true);
	}

	@Override
	public String getId() {
		return getTaskInfo().getId();
	}

	@Override
	public TaskInfo start() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getOnlineServer(), "gluster volume rebalance "
						+ getTaskInfo().getReference() + " " + getLayout() + " start"))));
		return getTaskInfo();
	}

	@Override
	public TaskInfo resume() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, "Can not suspend volume rebalance")));
		return getTaskInfo();
	}

	@Override
	public TaskInfo stop() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getOnlineServer(), "gluster volume rebalance "
						+ getTaskInfo().getReference() + " stop"))));
		return getTaskInfo();
	}

	@Override
	public TaskInfo pause() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, "Can not pause volume rebalance")));
		return getTaskInfo();
	}

	@Override
	public TaskInfo status() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getOnlineServer(), "gluster volume rebalance "
						+ getTaskInfo().getReference() + " status"))));
		return getTaskInfo();
	}

	@Override
	public void setTaskDescription() {
		TaskInfo taskInfo = getTaskInfo();
		getTaskInfo().setDescription("Volume rebalance running on " + taskInfo.getReference());

	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getLayout() {
		return layout;
	}

}
