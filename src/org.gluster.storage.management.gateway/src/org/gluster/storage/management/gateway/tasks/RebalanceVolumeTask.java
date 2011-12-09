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

import org.gluster.storage.management.core.exceptions.ConnectionException;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.model.TaskStatus;
import org.gluster.storage.management.core.model.TaskInfo.TASK_TYPE;
import org.gluster.storage.management.gateway.services.ClusterService;
import org.gluster.storage.management.gateway.services.GlusterInterfaceService;
import org.gluster.storage.management.gateway.utils.ServerUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.sun.jersey.core.util.Base64;

public class RebalanceVolumeTask extends Task {

	private String layout;
	private String serverName;
	private ServerUtil serverUtil;
	private GlusterInterfaceService glusterUtil;

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
		glusterUtil = ctx.getBean(GlusterInterfaceService.class);
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
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone offline. try with a new one
				serverName = getNewOnlineServer().getName();
				startRebalance(serverName);
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}

	private void startRebalance(String serverName) {
		String command = "gluster volume rebalance " + getTaskInfo().getReference() + " " + getLayout() + " start";
		String output = serverUtil.executeOnServer(serverName, command);
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
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone offline. update the failure status
				getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage())));
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
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
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(getOnlineServer()) == false) {
				// online server might have gone offline. update the failure status
				getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_FAILURE, e.getMessage())));
				return getTaskInfo().getStatus();
			} else {
				getTaskInfo().setStatus(new TaskStatus(new Status(Status.STATUS_CODE_ERROR, e.getMessage())));
				return getTaskInfo().getStatus();
			}
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
