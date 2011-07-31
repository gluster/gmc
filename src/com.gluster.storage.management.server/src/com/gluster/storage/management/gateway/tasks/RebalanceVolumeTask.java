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
package com.gluster.storage.management.gateway.tasks;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.utils.GlusterUtil;
import com.gluster.storage.management.gateway.utils.ServerUtil;
import com.gluster.storage.management.gateway.utils.SshUtil;
import com.sun.jersey.core.util.Base64;

public class RebalanceVolumeTask extends Task {

	private String layout;
	private String serverName;
	private ServerUtil serverUtil;
	private GlusterUtil glusterUtil;

	public RebalanceVolumeTask(ClusterService clusterService, String clusterName, String volumeName, String layout) {
		super(clusterService, clusterName, TASK_TYPE.VOLUME_REBALANCE, volumeName, "Volume " + volumeName
				+ " Rebalance", false, true, false);
		setLayout(layout);
		taskInfo.setName(getId());
		init();
	}
	
	private void init() {
		ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		serverUtil = ctx.getBean(ServerUtil.class);
		glusterUtil = ctx.getBean(GlusterUtil.class);
	}

	@Override
	public String getId() {
		return new String(Base64.encode(getClusterName() + "-" + taskInfo.getType() + "-" + taskInfo.getReference()));
	}

	@Override
	public void start() {
		try {
			serverName = getOnlineServer().getName();
			startRebalance(serverName);
		} catch(ConnectionException e) {
			// online server might have gone offline. try with a new one
			serverName = getNewOnlineServer().getName();
			startRebalance(serverName);
		}
	}

	private void startRebalance(String serverName) {
		String command = "gluster volume rebalance " + getTaskInfo().getReference() + " " + getLayout() + " start";
		String output = (String)serverUtil.executeOnServer(true, serverName, command, String.class);
		getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_RUNNING, output)));
	}

	@Override
	public void resume() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE,
						"Pause/Resume is not supported in Volume Rebalance")));
	}

	@Override
	public void stop() {
		try {
			glusterUtil.stopRebalance(serverName, getTaskInfo().getReference());
		} catch (ConnectionException e) {
			// online server might have gone offline. update the failure status
			getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage())));
		}
	}

	@Override
	public void pause() {
		getTaskInfo().setStatus(
				new TaskStatus(new Status(Status.STATUS_CODE_FAILURE,
						"Pause/Resume is not supported in Volume Rebalance")));
	}

	@Override
	public TaskStatus checkStatus() {
		try {
			return glusterUtil.checkRebalanceStatus(serverName, getTaskInfo().getReference());
		} catch(ConnectionException e) {
			// online server might have gone offline. update the failure status
			getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage())));
			return getTaskInfo().getStatus();
		}
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
