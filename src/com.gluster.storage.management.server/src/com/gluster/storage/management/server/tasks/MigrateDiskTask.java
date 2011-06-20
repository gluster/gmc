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

import org.springframework.beans.factory.annotation.Autowired;

import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Task;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskStatus;
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

	public MigrateDiskTask(TASK_TYPE type, String volumeName, String fromBrick, String toBrick) {
		super(type, volumeName);
		setFromBrick(fromBrick);
		setToBrick(toBrick);
		setTaskDescription();
		getTaskInfo().setCanPause(true);
		getTaskInfo().setCanStop(true);
	}

	public MigrateDiskTask(TaskInfo info) {
		super(info);
		setTaskDescription();
	}

	@Override
	public String getId() {
		return getTaskInfo().getId();
	}

	@Override
	public TaskInfo start() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getOnlineServer(), "gluster volume replace-brick "
						+ getTaskInfo().getReference() + " " + getFromBrick() + " " + getToBrick() + " start" ) )));
		return getTaskInfo();
	}

	@Override
	public TaskInfo resume() {
		return start();
	}

	@Override
	public TaskInfo stop() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getOnlineServer(), "gluster volume replace-brick "
						+ getTaskInfo().getReference() + " " + getFromBrick() + " " + getToBrick() + " abort" ) )));
		return getTaskInfo();
	}

	@Override
	public TaskInfo pause() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(sshUtil.executeRemote(getOnlineServer(), "gluster volume replace-brick "
						+ getTaskInfo().getReference() + " " + getFromBrick() + " " + getToBrick() + " pause" ) )));
		return getTaskInfo();
		
	}

	
	@Override
	public void setTaskDescription() {
		TaskInfo taskInfo = getTaskInfo();
		getTaskInfo().setDescription(
				getTypeStr() + " on volume [" + taskInfo.getReference() + "] from [" + getFromBrick()
						+ "] to [" + getToBrick() + "]");
	}

	
	@Override
	public TaskInfo status() {
		return getTaskInfo();
	}
}
