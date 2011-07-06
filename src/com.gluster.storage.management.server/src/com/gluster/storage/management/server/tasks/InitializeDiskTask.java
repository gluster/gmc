/**
 * InitializeDiskTask.java
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

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.server.services.ClusterService;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.SshUtil;
import com.sun.jersey.core.util.Base64;

public class InitializeDiskTask extends Task {

	private static final String INITIALIZE_DISK_SCRIPT = "format_device.py";
	
	private String serverName;
	private String diskName;
	private String fsType;
	private SshUtil sshUtil;
	private GlusterUtil glusterUtil;

	public InitializeDiskTask(ClusterService clusterService, String clusterName, String serverName, String diskName, String fsType) {
		super(clusterService, clusterName, TASK_TYPE.DISK_FORMAT, diskName, "Initialize disk " + serverName + ":"
				+ diskName, false, false, false);

		setServerName(serverName);
		setDiskName(diskName);
		setFsType(fsType);
		init();
	}

	public InitializeDiskTask(ClusterService clusterService, String clusterName, TaskInfo info) {
		super(clusterService, clusterName, info);
		init();
	}

	private void init() {
		ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		glusterUtil = ctx.getBean(GlusterUtil.class);
		sshUtil = ctx.getBean(SshUtil.class);
	}
	
	@Override
	public String getId() {
		return new String(
				Base64.encode(getClusterName() + "-" + taskInfo.getType() + "-" + serverName + ":" + diskName));
	}

	@Override
	public void resume() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE,
						"Stop/Pause/Resume is not supported in Disk Initialization")));
	}

	@Override
	public void stop() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE,
						"Stop/Pause/Resume is not supported in Disk Initialization")));
	}

	@Override
	public void pause() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE,
						"Stop/Pause/Resume is not supported in Disk Initialization")));
	}
	
	@Override
	public void commit() {
		// TODO Auto-generated method stub
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
	public void start() {
		try {
			startInitializeDisk(serverName);
		} catch(ConnectionException e) {
			// online server might have gone offline.  update the failure status
			getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage())));
		}
	}

	private void startInitializeDisk(String serverName) {
		String fsTypeCommand = (getFsType().equals(GlusterConstants.FSTYPE_DEFAULT)) ? "" : " -t " + getFsType();
		ProcessResult processResult = sshUtil.executeRemote(serverName, INITIALIZE_DISK_SCRIPT + fsTypeCommand + " "
				+ getDiskName());
		if (processResult.isSuccess()) {
			TaskStatus taskStatus = new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, processResult.getOutput()));
			taskStatus.setPercentageSupported((getFsType().equals(GlusterConstants.FSTYPE_XFS)) ? false : true);
			getTaskInfo().setStatus(taskStatus);
			return;
		}

		// if we reach here, it means Initialize disk start failed.
		throw new GlusterRuntimeException(processResult.toString());
	}

	@Override
	public TaskStatus checkStatus() {
		
		try {
			return glusterUtil.checkInitializeDiskStatus(serverName, getDiskName());
		} catch(ConnectionException e) {
			// online server might have gone offline. update the failure status
			return new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage()));
		}
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

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getFsType() {
		return fsType;
	}
}
