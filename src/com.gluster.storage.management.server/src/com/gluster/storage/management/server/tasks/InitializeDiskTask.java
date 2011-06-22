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
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.server.utils.SshUtil;

public class InitializeDiskTask extends Task {

	private String serverName;
	private String diskName;
	private SshUtil sshUtil = new SshUtil();

	public InitializeDiskTask( String serverName, String diskName) {
		super(TASK_TYPE.DISK_FORMAT, diskName);

		getTaskInfo().setCanPause(false);
		getTaskInfo().setCanStop(false);
		setServerName(serverName);
		setDiskName(diskName);
		setTaskDescription();
	}

	public InitializeDiskTask(TaskInfo info) {
		super(info);
	}

	@Override
	public String getId() {
		return getTaskInfo().getId();
	}

	@Override
	public TaskInfo resume() {
		getTaskInfo().setStatus( new TaskStatus( new Status(Status.STATUS_CODE_FAILURE, "Can not resume disk initialization")));
		return getTaskInfo();
	}

	@Override
	public TaskInfo stop() {
		getTaskInfo().setStatus( new TaskStatus( new Status(Status.STATUS_CODE_FAILURE, "Can not stop disk initialization")));
		return getTaskInfo();
	}

	@Override
	public TaskInfo pause() {
		getTaskInfo().setStatus( new TaskStatus( new Status(Status.STATUS_CODE_FAILURE, "Can not suspend disk initialization")));
		return getTaskInfo();
	}

	@Override
	public TASK_TYPE getType() {
		return TASK_TYPE.DISK_FORMAT;
	}

	@Override
	public TaskInfo getTaskInfo() {
		return getTaskInfo();
	}

	@Override
	public TaskInfo start() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getServerName(), "initialize_disk.py "
						+ getDiskName()))));
		return getTaskInfo();
	}

	@Override
	public TaskInfo status() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getServerName(), "initialize_disk_status.py "
						+ getDiskName()))));
		return getTaskInfo();
	}

	@Override
	public void setTaskDescription() {
		getTaskInfo().setDescription("Formating disk of " + getServerName() + ":" + getDiskName());
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}

	public String getDiskName() {
		return diskName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerName() {
		return serverName;
	}
}
