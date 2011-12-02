/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Gateway.
 *
 * Gluster Management Gateway is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Gateway is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.gateway.tasks;

import org.gluster.storage.management.core.constants.GlusterConstants;
import org.gluster.storage.management.core.exceptions.ConnectionException;
import org.gluster.storage.management.core.model.InitDiskStatusResponse;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskInfo;
import org.gluster.storage.management.core.model.TaskStatus;
import org.gluster.storage.management.core.model.InitDiskStatusResponse.FORMAT_STATUS;
import org.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import org.gluster.storage.management.gateway.services.ClusterService;
import org.gluster.storage.management.gateway.utils.ServerUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.sun.jersey.core.util.Base64;

public class InitializeDiskTask extends Task {

	private static final String INITIALIZE_DISK_SCRIPT = "format_device.py";
	private static final String INITIALIZE_DISK_STATUS_SCRIPT = "get_format_device_status.py";
	
	private String serverName;
	private String diskName;
	private String fsType;
	private String mountPoint;
	private ServerUtil serverUtil;

	public InitializeDiskTask(ClusterService clusterService, String clusterName, String serverName, String diskName,
			String fsType, String mountPoint) {
		// Reference contains "Server:disk"
		super(clusterService, clusterName, TASK_TYPE.DISK_FORMAT, serverName + ":" + diskName, "Initialize disk "
				+ serverName + ":" + diskName, false, false, false);

		setServerName(serverName);
		setDiskName(diskName);
		setFsType(fsType);
		setMountpoint(mountPoint);
		taskInfo.setName(getId());
		init();
	}

	public InitializeDiskTask(ClusterService clusterService, String clusterName, TaskInfo info) {
		super(clusterService, clusterName, info);
		init();
	}

	private void init() {
		ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		serverUtil = ctx.getBean(ServerUtil.class);
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
	public void start() {
		try {
			startInitializeDisk(serverName);
		} catch(ConnectionException e) {
			// online server might have gone offline.  update the failure status
			getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage())));
		}
	}

	private void startInitializeDisk(String serverName) {
		String output = serverUtil.executeScriptOnServer(serverName, INITIALIZE_DISK_SCRIPT + " " + getFsType() + " \""
				+ getMountpoint() + "\" " + getDiskName() );
		TaskStatus taskStatus = new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, output));
		taskStatus.setPercentageSupported((getFsType().equals(GlusterConstants.FSTYPE_XFS)) ? false : true);
		getTaskInfo().setStatus(taskStatus);
	}

	@Override
	public TaskStatus checkStatus() {
		
		try {
			return getInitializingDeviceStatus(serverName, getDiskName());
		} catch(ConnectionException e) {
			// online server might have gone offline. update the failure status
			return new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage()));
		}
	}
	
	private TaskStatus getInitializingDeviceStatus(String serverName, String diskName) {
		InitDiskStatusResponse initDiskStatusResponse;
		TaskStatus taskStatus = new TaskStatus();
		
		try {
			initDiskStatusResponse = serverUtil.executeScriptOnServer(serverName, INITIALIZE_DISK_STATUS_SCRIPT + " "
				+ diskName, InitDiskStatusResponse.class);
		} catch(RuntimeException e) {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			taskStatus.setMessage(e.getMessage());
			throw e;
		}

		if (initDiskStatusResponse.getFormatStatus() == FORMAT_STATUS.COMPLETED) {
			taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
		} else if (initDiskStatusResponse.getFormatStatus() == FORMAT_STATUS.IN_PROGRESS) {
			taskStatus.setCode(Status.STATUS_CODE_RUNNING);
			taskStatus.setPercentCompleted(Math.round(initDiskStatusResponse.getCompletedBlocks()
					/ initDiskStatusResponse.getTotalBlocks() * 100));
		} else if(initDiskStatusResponse.getFormatStatus() == FORMAT_STATUS.NOT_RUNNING) {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		
		taskStatus.setMessage(initDiskStatusResponse.getMessage());
		return taskStatus;
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
	
	public void setMountpoint(String deviceMountPoint) {
		this.mountPoint = deviceMountPoint;
	}
	
	public String getMountpoint() {
		return mountPoint;
	}
}
