/**
 * VolumesClient.java
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
package com.gluster.storage.management.client;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.LogMessageListResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.DateUtil;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.StringUtil;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class VolumesClient extends AbstractClient {
	public VolumesClient() {
		super();
	}
	
	public VolumesClient(String clusterName) {
		super(clusterName);
	}
	
	public VolumesClient(String securityToken, String clusterName) {
		super(securityToken, clusterName);
	}

	@Override
	public String getResourcePath() {
		return RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESTConstants.RESOURCE_VOLUMES;
	}

	public Status createVolume(Volume volume) {
		return (Status) postObject(Status.class, volume);
	}

	private Status performOperation(String volumeName, String operation) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, operation);

		return (Status) putRequest(volumeName, Status.class, form);
	}

	public Status startVolume(String volumeName) {
		return performOperation(volumeName, RESTConstants.TASK_START);
	}

	public Status stopVolume(String volumeName) {
		return performOperation(volumeName, RESTConstants.TASK_STOP);
	}

	public Status setVolumeOption(String volume, String key, String value) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPTION_KEY, key);
		form.add(RESTConstants.FORM_PARAM_OPTION_VALUE, value);
		return (Status) postRequest(volume + "/" + RESTConstants.RESOURCE_OPTIONS, Status.class, form);
	}

	public Status resetVolumeOptions(String volume) {
		return (Status) putRequest(volume + "/" + RESTConstants.RESOURCE_OPTIONS, Status.class);
	}

	public VolumeListResponse getAllVolumes() {
		return (VolumeListResponse) fetchResource(VolumeListResponse.class);
	}

	@SuppressWarnings("unchecked")
	public GenericResponse<Volume> getVolume(String volumeName) {
		return (GenericResponse<Volume>)fetchSubResource(volumeName, GenericResponse.class);
	}

	public Status deleteVolume(Volume volume, boolean deleteOption) {
		MultivaluedMap<String, String> queryParams = prepareDeleteVolumeQueryParams(deleteOption);
		return (Status) deleteSubResource(volume.getName(), Status.class, queryParams);
	}

	public VolumeOptionInfoListResponse getVolumeOptionsDefaults() {
		return ((VolumeOptionInfoListResponse) fetchSubResource(RESTConstants.RESOURCE_DEFAULT_OPTIONS,
				VolumeOptionInfoListResponse.class));
	}

	public Status addBricks(String volumeName, List<String> brickList) {
		String bricks = StringUtil.ListToString(brickList, ",");
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_BRICKS, bricks);
		return (Status) postRequest(volumeName + "/" + RESTConstants.RESOURCE_BRICKS, Status.class, form);
	}

	/**
	 * Fetches volume logs for the given volume based on given filter criteria
	 * 
	 * @param volumeName
	 *            Name of volume whose logs are to be fetched
	 * @param brickName
	 *            Name of the brick whose logs are to be fetched. Pass ALL to fetch log messages from all bricks of the
	 *            volume.
	 * @param severity
	 *            Log severity {@link GlusterConstants#VOLUME_LOG_LEVELS_ARR}. Pass ALL to fetch log messages of all
	 *            severity levels.
	 * @param fromTimestamp
	 *            From timestamp. Pass null if this filter is not required.
	 * @param toTimestamp
	 *            To timestamp. Pass null if this filter is not required.
	 * @param messageCount
	 *            Number of most recent log messages to be fetched (from each disk)
	 * @return Log Message List response received from the Gluster Management Server.
	 */
	public LogMessageListResponse getLogs(String volumeName, String brickName, String severity, Date fromTimestamp,
			Date toTimestamp, int messageCount) {
		MultivaluedMap<String, String> queryParams = prepareGetLogQueryParams(brickName, severity, fromTimestamp,
				toTimestamp, messageCount);

		return (LogMessageListResponse) fetchSubResource(volumeName + "/" + RESTConstants.RESOURCE_LOGS,
				queryParams, LogMessageListResponse.class);
	}

	public void downloadLogs(String volumeName, String filePath) {
		downloadSubResource(volumeName + "/" + RESTConstants.RESOURCE_LOGS + "/" + RESTConstants.RESOURCE_DOWNLOAD, filePath);
	}

	public Status removeBricks(String volumeName, List<Brick> BrickList, boolean deleteOption) {
		String bricks = StringUtil.ListToString(GlusterCoreUtil.getQualifiedBrickList(BrickList), ",");
		MultivaluedMap<String, String> queryParams = prepareRemoveBrickQueryParams(volumeName, bricks, deleteOption);
		return (Status) deleteSubResource(volumeName + "/" + RESTConstants.RESOURCE_BRICKS, Status.class, queryParams);
	}
	
	private MultivaluedMap<String, String> prepareRemoveBrickQueryParams(String volumeName, String bricks,
			boolean deleteOption) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_VOLUME_NAME, volumeName);
		queryParams.add(RESTConstants.QUERY_PARAM_BRICKS, bricks);
		queryParams.add(RESTConstants.QUERY_PARAM_DELETE_OPTION, "" + deleteOption);
		return queryParams;
	}

	private MultivaluedMap<String, String> prepareDeleteVolumeQueryParams(boolean deleteOption) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_DELETE_OPTION, "" + deleteOption);
		return queryParams;
	}

	private MultivaluedMap<String, String> prepareGetLogQueryParams(String brickName, String severity,
			Date fromTimestamp, Date toTimestamp, int messageCount) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_LINE_COUNT, "" + messageCount);
		if (!brickName.equals(CoreConstants.ALL)) {
			queryParams.add(RESTConstants.QUERY_PARAM_BRICK_NAME, brickName);
		}

		if (!severity.equals(CoreConstants.ALL)) {
			queryParams.add(RESTConstants.QUERY_PARAM_LOG_SEVERITY, severity);
		}

		if (fromTimestamp != null) {
			queryParams.add(RESTConstants.QUERY_PARAM_FROM_TIMESTAMP,
					DateUtil.dateToString(fromTimestamp, CoreConstants.DATE_WITH_TIME_FORMAT));
		}

		if (toTimestamp != null) {
			queryParams.add(RESTConstants.QUERY_PARAM_TO_TIMESTAMP,
					DateUtil.dateToString(toTimestamp, CoreConstants.DATE_WITH_TIME_FORMAT));
		}
		return queryParams;
	}

	public Status startMigration(String volumeName, String brickFrom, String brickTo, Boolean autoCommit) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_SOURCE, brickFrom);
		form.add(RESTConstants.FORM_PARAM_TARGET, brickTo);
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_START);
		form.add(RESTConstants.FORM_PARAM_AUTO_COMMIT, autoCommit);
		
		return (Status) putRequest( volumeName + "/" + RESTConstants.RESOURCE_BRICKS, Status.class, form);
	}

	

	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		if (usersClient.authenticate("gluster", "gluster").isSuccess()) {
			VolumesClient client = new VolumesClient(usersClient.getSecurityToken());
//			List<Disk> disks = new ArrayList<Disk>();
//			Disk diskElement = new Disk();
//			diskElement.setName("sda1");
//			diskElement.setStatus(DISK_STATUS.READY);
//			disks.add(diskElement);
//			diskElement.setName("sda2");
//			diskElement.setStatus(DISK_STATUS.READY);
//			disks.add(diskElement);
//
//			Volume vol = new Volume("vol1", null, Volume.VOLUME_TYPE.PLAIN_DISTRIBUTE, Volume.TRANSPORT_TYPE.ETHERNET,
//					Volume.VOLUME_STATUS.ONLINE);
//			// vol.setDisks(disks);
//			System.out.println(client.createVolume(vol));
//			for (VolumeOptionInfo option : client.getVolumeOptionsDefaults()) {
//				System.out.println(option.getName() + "-" + option.getDescription() + "-" + option.getDefaultValue());
//			}
//			System.out.println(client.getVolume("Volume3").getOptions());
//			System.out.println(client.setVolumeOption("Volume3", "network.frame-timeout", "600").getMessage());
//			List<Disk> disks = new ArrayList<Disk>(); 
//			Disk disk = new Disk();
//			disk.setServerName("server1");
//			disk.setName("sda");
//			disk.setStatus(DISK_STATUS.READY);
//			disks.add(disk);
//			
//			Status status = client.addDisks("Volume3", disks);
//			System.out.println(status.getMessage());
			client.downloadLogs("vol1", "/tmp/temp1.tar.gz");
		}
	}
}
