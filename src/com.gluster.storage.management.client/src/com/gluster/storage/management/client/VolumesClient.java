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

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_ACCESS_PROTOCOLS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_BRICKS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_CIFS_USERS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_REPLICA_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_STRIPE_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_TRANSPORT_TYPE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VOLUME_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VOLUME_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VOLUME_TYPE;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.VolumeLogMessage;
import com.gluster.storage.management.core.model.VolumeOptionInfo;
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

	public void createVolume(Volume volume) {
		Form form = new Form();
		form.add(FORM_PARAM_VOLUME_NAME, volume.getName());
		form.add(FORM_PARAM_VOLUME_TYPE, volume.getVolumeType().toString());
		form.add(FORM_PARAM_TRANSPORT_TYPE, volume.getTransportType().toString());
		form.add(FORM_PARAM_REPLICA_COUNT, volume.getReplicaCount());
		form.add(FORM_PARAM_STRIPE_COUNT, volume.getStripeCount());
		form.add(FORM_PARAM_BRICKS, StringUtil.collectionToString(volume.getBricks(), ","));
		form.add(FORM_PARAM_ACCESS_PROTOCOLS, StringUtil.collectionToString(volume.getNASProtocols(), ","));
		form.add(FORM_PARAM_VOLUME_OPTIONS, StringUtil.collectionToString(volume.getOptions().getOptions(), ","));
		if (volume.isCifsEnable()) {
			form.add(FORM_PARAM_CIFS_USERS, StringUtil.collectionToString(volume.getCifsUsers(), ","));
		}
		postRequest(form);
	}

	private void performOperation(String volumeName, String operation, Boolean force) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, operation);
		form.add(RESTConstants.FORM_PARAM_FORCE, force);

		putRequest(volumeName, form);
	}

	public void startVolume(String volumeName, Boolean forceStart) {
		performOperation(volumeName, RESTConstants.TASK_START, forceStart);
	}

	public void stopVolume(String volumeName, Boolean forceStop) {
		performOperation(volumeName, RESTConstants.TASK_STOP, forceStop);
	}
	
	public void setCifsConfig(String volumeName, Boolean isCifsEnabled, String cifsUsers) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.FORM_PARAM_CIFS_CONFIG);
		form.add(RESTConstants.FORM_PARAM_CIFS_ENABLE, isCifsEnabled);
		form.add(RESTConstants.FORM_PARAM_CIFS_USERS, cifsUsers);
		putRequest(volumeName, form);
	}
	
	public boolean volumeExists(String volumeName) {
		try {
			// TODO: instead of fetching full volume name, fetch list of volumes and check if
			// it contains our volume name
			getVolume(volumeName);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	public void setVolumeOption(String volume, String key, String value) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPTION_KEY, key);
		form.add(RESTConstants.FORM_PARAM_OPTION_VALUE, value);
		postRequest(volume + "/" + RESTConstants.RESOURCE_OPTIONS, form);
	}

	public void resetVolumeOptions(String volume) {
		putRequest(volume + "/" + RESTConstants.RESOURCE_OPTIONS);
	}

	public List<Volume> getAllVolumes() {
		return ((VolumeListResponse) fetchResource(VolumeListResponse.class)).getVolumes();
	}

	public Volume getVolume(String volumeName) {
		return (Volume)fetchSubResource(volumeName, Volume.class);
	}

	public void deleteVolume(String volumeName, boolean deleteOption) {
		MultivaluedMap<String, String> queryParams = prepareDeleteVolumeQueryParams(deleteOption);
		deleteSubResource(volumeName, queryParams);
	}

	public List<VolumeOptionInfo> getVolumeOptionsInfo() {
		return ((VolumeOptionInfoListResponse) fetchSubResource(RESTConstants.RESOURCE_DEFAULT_OPTIONS,
				VolumeOptionInfoListResponse.class)).getOptions();
	}

	public void addBricks(String volumeName, Set<String> brickList) {
		String bricks = StringUtil.collectionToString(brickList, ",");
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_BRICKS, bricks);
		postRequest(volumeName + "/" + RESTConstants.RESOURCE_BRICKS, form);
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
	public List<VolumeLogMessage> getLogs(String volumeName, String brickName, String severity, Date fromTimestamp,
			Date toTimestamp, int messageCount) {
		MultivaluedMap<String, String> queryParams = prepareGetLogQueryParams(brickName, severity, fromTimestamp,
				toTimestamp, messageCount);

		return ((LogMessageListResponse) fetchSubResource(volumeName + "/" + RESTConstants.RESOURCE_LOGS,
				queryParams, LogMessageListResponse.class)).getLogMessages();
	}

	public void downloadLogs(String volumeName, String filePath) {
		downloadSubResource(volumeName + "/" + RESTConstants.RESOURCE_LOGS + "/" + RESTConstants.RESOURCE_DOWNLOAD, filePath);
	}

	public void removeBricks(String volumeName, Set<Brick> BrickList, boolean deleteOption) {
		String bricks = StringUtil.collectionToString(GlusterCoreUtil.getQualifiedBrickList(BrickList), ",");
		MultivaluedMap<String, String> queryParams = prepareRemoveBrickQueryParams(volumeName, bricks, deleteOption);
		deleteSubResource(volumeName + "/" + RESTConstants.RESOURCE_BRICKS, queryParams);
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

	public URI startMigration(String volumeName, String brickFrom, String brickTo, Boolean autoCommit) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_SOURCE, brickFrom);
		form.add(RESTConstants.FORM_PARAM_TARGET, brickTo);
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_START);
		form.add(RESTConstants.FORM_PARAM_AUTO_COMMIT, autoCommit);
		return  putRequestURI(volumeName + "/" + RESTConstants.RESOURCE_BRICKS, form);
	}
	
	public URI rebalanceStart(String volumeName, Boolean fixLayout, Boolean migrateData, Boolean forcedDataMigrate) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_REBALANCE_START);
		form.add(RESTConstants.FORM_PARAM_FIX_LAYOUT, fixLayout);
		form.add(RESTConstants.FORM_PARAM_MIGRATE_DATA, migrateData);
		form.add(RESTConstants.FORM_PARAM_FORCED_DATA_MIGRATE, forcedDataMigrate);
		return  putRequestURI(volumeName, form);
	}
	
	public void rebalanceStop(String volumeName) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_REBALANCE_STOP);
		putRequest(volumeName, form);
	}

	public void volumeLogRotate(String volumeName, List<String> brickList) {
		Form form = new Form();
		String bricks = StringUtil.collectionToString(brickList, ",");
		form.add(RESTConstants.FORM_PARAM_OPERATION, RESTConstants.TASK_LOG_ROTATE);
		form.add(FORM_PARAM_BRICKS, bricks);
		putRequest(volumeName, form);
	}
	
	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		try {
			usersClient.authenticate("gluster", "gluster");
			VolumesClient client = new VolumesClient(usersClient.getSecurityToken());
			System.out.println(client.getAllVolumes());
//			client.downloadLogs("vol1", "/tmp/temp1.tar.gz");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
