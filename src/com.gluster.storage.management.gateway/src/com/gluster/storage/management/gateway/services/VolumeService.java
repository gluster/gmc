/*******************************************************************************
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
 *******************************************************************************/
package com.gluster.storage.management.gateway.services;

import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_BRICKS;
import static com.gluster.storage.management.core.constants.RESTConstants.TASK_START;
import static com.gluster.storage.management.core.constants.RESTConstants.TASK_STOP;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.NAS_PROTOCOL;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.model.VolumeLogMessage;
import com.gluster.storage.management.core.response.LogMessageListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.DateUtil;
import com.gluster.storage.management.core.utils.FileUtil;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.gluster.storage.management.gateway.data.ClusterInfo;
import com.gluster.storage.management.gateway.resources.v1_0.TasksResource;
import com.gluster.storage.management.gateway.tasks.MigrateBrickTask;
import com.gluster.storage.management.gateway.tasks.RebalanceVolumeTask;
import com.gluster.storage.management.gateway.utils.ServerUtil;

/**
 *
 */
@Component
public class VolumeService {
	private static final String ALL_SERVERS_FILE_NAME = "servers";
	private static final String VOLUME_GET_CIFS_USERS_SCRIPT = "get_volume_user_cifs.py";
	private static final String VOLUME_CIFS_GRUN_SCRIPT = "grun.py";
	private static final String VOLUME_CREATE_CIFS_SCRIPT = "create_volume_cifs_all.py";
	private static final String VOLUME_MODIFY_CIFS_SCRIPT = "update_volume_cifs_all.py";
	private static final String VOLUME_START_CIFS_PEER_SCRIPT = "start_volume_cifs.py";
	private static final String VOLUME_STOP_CIFS_PEER_SCRIPT = "stop_volume_cifs.py";
	private static final String VOLUME_DELETE_CIFS_SCRIPT = "delete_volume_cifs_all.py";
	private static final String VOLUME_BRICK_LOG_SCRIPT = "get_volume_brick_log.py";
	private static final String VOLUME_DIRECTORY_CLEANUP_SCRIPT = "clear_volume_directory.py";
	private static final String REMOVE_SERVER_VOLUME_CIFS_CONFIG = "remove_server_volume_cifs_config.py";
	private static final String ALL_ONLINE_VOLUMES_FILE_NAME = "volumes";

	@Autowired
	private ClusterService clusterService;

	@Autowired
	private GlusterInterfaceService glusterUtil;
	
	@Autowired
	private GlusterServerService glusterServerService;

	@Autowired
	protected ServerUtil serverUtil;

	// TODO: To be replaced with taskService
	@Autowired
	private TasksResource taskResource;
	
	private static final Logger logger = Logger.getLogger(VolumeService.class);
	
	public void addBricksToVolume(String clusterName, String volumeName, String bricks) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		if (volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}

		if (bricks == null || bricks.isEmpty()) {
			throw new GlusterValidationException("Bricks must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		List<String> brickList = Arrays.asList(bricks.split(",")); 
		try {
			glusterUtil.addBricks(volumeName, brickList, onlineServer.getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				glusterUtil.addBricks(volumeName, brickList, onlineServer.getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}

	public Volume getVolume(String clusterName, String volumeName) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		Volume volume;
		try {
			volume = glusterUtil.getVolume(volumeName, onlineServer.getName());
			// Collect the CIFS users if CIFS Re-exported 
			fetchVolumeCifsUsers(clusterName, volume);
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				volume = glusterUtil.getVolume(volumeName, onlineServer.getName());
				// Collect the CIFS users if CIFS Re-exported
				fetchVolumeCifsUsers(clusterName, volume);
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
		return volume;
	}
	
	public List<Volume> getVolumes(String clusterName, Integer maxCount, String previousVolumeName) {
		List<Volume> volumes = getVolumes(clusterName);
		// Skip the volumes by maxCount / previousServerName
		volumes = GlusterCoreUtil.skipEntities(volumes, maxCount, previousVolumeName);
		// fetch CIFS users of the volumes
		fetchCifsUsers(clusterName, volumes);
		
		return volumes;
	}

	private List<Volume> getVolumes(String clusterName) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		if(cluster.getServers().size() == 0) {
			// no server added yet. return an empty array.
			return new ArrayList<Volume>();
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			return glusterUtil.getAllVolumes(onlineServer.getName());
		} catch (Exception e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}
			
			return glusterUtil.getAllVolumes(onlineServer.getName());
		}
	}
	
	private void fetchVolumeCifsUsers(String clusterName, Volume volume) {
		List<String> users = new ArrayList<String>();
		try {
			ProcessResult result = serverUtil
					.executeGlusterScript(true, VOLUME_GET_CIFS_USERS_SCRIPT, volume.getName());
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
			String output = result.getOutput().trim();
			if (output.isEmpty()) {
				volume.disableCifs();
			} else {
				users = Arrays.asList(output.split(CoreConstants.NEWLINE));
				volume.enableCifs();
				volume.setCifsUsers(users);
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in fetching CIFS users [" + volume.getName() + "]: "
					+ e.getMessage());
		}
		return;
	}

	private void fetchCifsUsers(String clusterName, List<Volume> volumes) {
		for (Volume volume: volumes) {
			fetchVolumeCifsUsers(clusterName, volume);
		}
	}
	
	private File createOnlineServerList(String clusterName) {
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String clusterServersListFile = FileUtil.getTempDirName() + CoreConstants.FILE_SEPARATOR
		+ ALL_SERVERS_FILE_NAME + "_" + timestamp;
		
		try {
			GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
			List<GlusterServer> glusterServers = glusterServerService.getGlusterServers(onlineServer.getName());
			File serversFile = new File(clusterServersListFile);
			FileOutputStream fos = new FileOutputStream(serversFile);
			for (GlusterServer server : glusterServers) {
				if (server.getStatus() == SERVER_STATUS.ONLINE) {
					fos.write((server.getName() + CoreConstants.NEWLINE).getBytes());
				}
			}
			fos.close();
			return serversFile;
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in preparing server list: [" + e.getMessage() + "]");
		}
	}
	
	public void startCifsReExport(String clusterName, String volumeName) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_CIFS_GRUN_SCRIPT,
					file.getAbsolutePath(), VOLUME_START_CIFS_PEER_SCRIPT, volumeName);
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in starting CIFS services for volume [" + volumeName + "]: "
					+ e.getMessage());
		}
	}

	public void stopCifsReExport(String clusterName, String volumeName) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_CIFS_GRUN_SCRIPT,
					file.getAbsolutePath(), VOLUME_STOP_CIFS_PEER_SCRIPT, volumeName);
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in stoping CIFS services for volume [" + volumeName + "]: "
					+ e.getMessage());
		}
	}
	
	public void deleteCifsUsers(String clusterName, String volumeName) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_DELETE_CIFS_SCRIPT,
					file.getAbsolutePath(), volumeName);
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in deleting CIFS configuration [" + volumeName + "]: "
					+ e.getMessage());
		}
	}

	public void createCIFSUsers(String clusterName, String volumeName, String cifsUsers) {
		try {
			File file = createOnlineServerList(clusterName);
			List<String> arguments = new ArrayList<String>(); 
			arguments.add(file.getAbsolutePath());
			arguments.add(volumeName);
			arguments.addAll( Arrays.asList(cifsUsers.replaceAll(" ", "").split(",")));
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_CREATE_CIFS_SCRIPT, arguments);
			file.delete();
			Volume volume = getVolume(clusterName, volumeName);
			// If the volume service is already in running, create user may start CIFS re-export automatically.
			if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
				startCifsReExport(clusterName, volumeName);
			} 
			/*
			 * else { stopCifsReExport(clusterName, volumeName); }
			 */
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in creating CIFS configuration [" + volumeName + "]: "
					+ e.getMessage());
		}
	}

	@Deprecated
	public void modifyCIFSUsers(String clusterName, String volumeName, String cifsUsers) {
		try {
			File file = createOnlineServerList(clusterName);
			List<String> arguments = new ArrayList<String>(); 
			arguments.add(file.getAbsolutePath());
			arguments.add(volumeName);
			arguments.addAll( Arrays.asList(cifsUsers.split(",")));
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_MODIFY_CIFS_SCRIPT, arguments);
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in updating CIFS configuration [" + volumeName + "]: "
					+ e.getMessage());
		}
	}
	
	// To clear all the volume CIFS configurations from the server
	public void clearCifsConfiguration(String clusterName, String onlineServerName, String serverName) {
		File volumesFile = createOnlineVolumeList(clusterName, onlineServerName);
		if (volumesFile == null) {
			return;
		}
		try {
			removeServerVolumeCifsConfig(serverName, volumesFile.getAbsolutePath());
			volumesFile.delete();
		} catch(Exception e) {
			volumesFile.delete();
			throw new GlusterRuntimeException("Error in clearing volume CIFS configuration: [" + e.getMessage() + "]");
		}
	}
	
	private File createOnlineVolumeList(String clusterName, String onlineServerName) {
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String volumeListFileName = FileUtil.getTempDirName() + CoreConstants.FILE_SEPARATOR
				+ ALL_ONLINE_VOLUMES_FILE_NAME + "_" + timestamp;
		try {
			List<Volume> volumes = getVolumes(clusterName);
			if (volumes == null || volumes.size() == 0) {
				return null;
			}
			File volumesFile = new File(volumeListFileName);
			FileOutputStream fos = new FileOutputStream(volumesFile);
			for (Volume volume : volumes) {
				if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
					fos.write((volume.getName() + CoreConstants.NEWLINE).getBytes());
				}
			}
			fos.close();
			return volumesFile;
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in preparing volume list: [" + e.getMessage() + "]");
		}
	}
	
	
	public void removeServerVolumeCifsConfig(String serverName, String volumesFileName) {
		ProcessResult result = serverUtil.executeGlusterScript(true, REMOVE_SERVER_VOLUME_CIFS_CONFIG, serverName,
				volumesFileName);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException(result.toString());
		}
	}
	
	public void createVolume(String clusterName, String volumeName, String volumeType, String transportType,
			Integer count, String bricks, String accessProtocols, String options,
			String cifsUsers) {
		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		if ((volumeType.equals(VOLUME_TYPE.REPLICATE.toString()) || volumeType.equals(VOLUME_TYPE.DISTRIBUTED_REPLICATE
				.toString())) && count <= 0) {
			throw new GlusterValidationException("Replica count must be a positive integer");
		}

		if ((volumeType.equals(VOLUME_TYPE.STRIPE.toString()) || volumeType.equals(VOLUME_TYPE.DISTRIBUTED_STRIPE
				.toString())) && count <= 0) {
			throw new GlusterValidationException("Stripe count must be a positive integer");
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			glusterUtil.createVolume(onlineServer.getName(), volumeName, volumeType, transportType, count,
					bricks, accessProtocols, options);
			
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				glusterUtil.createVolume(onlineServer.getName(), volumeName, volumeType, transportType, count,
						bricks, accessProtocols, options);
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}

		List<String> nasProtocols = Arrays.asList(accessProtocols.split(","));
		// if cifs enabled
		if (nasProtocols.contains(NAS_PROTOCOL.CIFS.toString())) {
			try {
				createCIFSUsers(clusterName, volumeName, cifsUsers);
			} catch (Exception e) {
				throw new GlusterRuntimeException(CoreConstants.NEWLINE + e.getMessage());
			}
		}
	}
	
	public String downloadLogs(Volume volume) {
		// create temporary directory
		File tempDir = FileUtil.createTempDir();
		String tempDirPath = tempDir.getPath();

		for (Brick brick : volume.getBricks()) {
			String logDir = glusterUtil.getLogLocation(volume.getName(), brick.getQualifiedName(),
					brick.getServerName());
			String logFileName = glusterUtil.getLogFileNameForBrickDir(brick.getBrickDirectory());
			String logFilePath = logDir + CoreConstants.FILE_SEPARATOR + logFileName;

			serverUtil.getFileFromServer(brick.getServerName(), logFilePath, tempDirPath);

			String fetchedLogFile = tempDirPath + File.separator + logFileName;
			// append log file name with server name so that log files don't overwrite each other 
			// in cases where the brick log file names are same on multiple servers
			String localLogFile = tempDirPath + File.separator + brick.getServerName() + "-" + logFileName;

			FileUtil.renameFile(fetchedLogFile, localLogFile);
		}

		String gzipPath = FileUtil.getTempDirName() + CoreConstants.FILE_SEPARATOR + volume.getName() + "-logs.tar.gz";
		ProcessUtil.executeCommand("tar", "czvf", gzipPath, "-C", tempDir.getParent(), tempDir.getName());

		// delete the temp directory
		FileUtil.recursiveDelete(tempDir);

		return gzipPath;
	}
	
	public List<VolumeLogMessage> getLogs(String clusterName, String volumeName, String brickName, String severity,
			String fromTimestamp, String toTimestamp, Integer lineCount) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		if (volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Volume name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		if (lineCount == null || lineCount == 0) {
			lineCount = 100;
		}

		List<VolumeLogMessage> logMessages = null;
		Volume volume = getVolume(clusterName, volumeName);

		if (brickName == null || brickName.isEmpty() || brickName.equals(CoreConstants.ALL)) {
			logMessages = getLogsForAllBricks(volume, lineCount);
		} else {
			// fetch logs for given brick of the volume
			for (Brick brick : volume.getBricks()) {
				if (brick.getQualifiedName().equals(brickName)) {
					logMessages = getBrickLogs(volume, brick, lineCount);
					break;
				}
			}
		}

		filterLogsBySeverity(logMessages, severity);
		filterLogsByTime(logMessages, fromTimestamp, toTimestamp);
		return logMessages;
	}

	private void filterLogsByTime(List<VolumeLogMessage> logMessages, String fromTimestamp, String toTimestamp) {
		Date fromTime = null, toTime = null;

		if (fromTimestamp != null && !fromTimestamp.isEmpty()) {
			fromTime = DateUtil.stringToDate(fromTimestamp);
		}

		if (toTimestamp != null && !toTimestamp.isEmpty()) {
			toTime = DateUtil.stringToDate(toTimestamp);
		}

		List<VolumeLogMessage> messagesToRemove = new ArrayList<VolumeLogMessage>();
		for (VolumeLogMessage logMessage : logMessages) {
			Date logTimestamp = logMessage.getTimestamp();
			if (fromTime != null && logTimestamp.before(fromTime)) {
				messagesToRemove.add(logMessage);
				continue;
			}

			if (toTime != null && logTimestamp.after(toTime)) {
				messagesToRemove.add(logMessage);
			}
		}
		logMessages.removeAll(messagesToRemove);
	}

	private void filterLogsBySeverity(List<VolumeLogMessage> logMessages, String severity) {
		if (severity == null || severity.isEmpty()) {
			return;
		}

		List<VolumeLogMessage> messagesToRemove = new ArrayList<VolumeLogMessage>();
		for (VolumeLogMessage logMessage : logMessages) {
			if (!logMessage.getSeverity().equals(severity)) {
				messagesToRemove.add(logMessage);
			}
		}
		logMessages.removeAll(messagesToRemove);
	}

	private List<VolumeLogMessage> getLogsForAllBricks(Volume volume, Integer lineCount) {
		List<VolumeLogMessage> logMessages;
		logMessages = new ArrayList<VolumeLogMessage>();
		// fetch logs for every brick of the volume
		for (Brick brick : volume.getBricks()) {
			logMessages.addAll(getBrickLogs(volume, brick, lineCount));
		}

		// Sort the log messages based on log timestamp
		Collections.sort(logMessages, new Comparator<VolumeLogMessage>() {
			@Override
			public int compare(VolumeLogMessage message1, VolumeLogMessage message2) {
				return message1.getTimestamp().compareTo(message2.getTimestamp());
			}
		});

		return logMessages;
	}
	
	private List<VolumeLogMessage> getBrickLogs(Volume volume, Brick brick, Integer lineCount)
			throws GlusterRuntimeException {
		String logDir = glusterUtil.getLogLocation(volume.getName(), brick.getQualifiedName(), brick.getServerName());
		String logFileName = glusterUtil.getLogFileNameForBrickDir(brick.getBrickDirectory());
		String logFilePath = logDir + CoreConstants.FILE_SEPARATOR + logFileName;

		// Usage: get_volume_disk_log.py <volumeName> <diskName> <lineCount>
		LogMessageListResponse response = serverUtil.executeScriptOnServer(brick.getServerName(),
				VOLUME_BRICK_LOG_SCRIPT + " " + logFilePath + " " + lineCount, LogMessageListResponse.class);

		// populate disk and trim other fields
		List<VolumeLogMessage> logMessages = response.getLogMessages();
		for (VolumeLogMessage logMessage : logMessages) {
			logMessage.setBrick(brick.getQualifiedName());
		}
		return logMessages;
	}
	
	public String migrateBrickStart(String clusterName, String volumeName, String fromBrick, String toBrick,
			Boolean autoCommit) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		if (volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Volume name must not be empty!");
		}

		if (fromBrick == null || fromBrick.isEmpty()) {
			throw new GlusterValidationException("From brick must not be empty!");
		}
		
		if (toBrick == null || toBrick.isEmpty()) {
			throw new GlusterValidationException("To brick must not be empty!");
		}
		
		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}
		
		if(autoCommit == null) {
			autoCommit = false;
		}
		
		MigrateBrickTask migrateDiskTask = new MigrateBrickTask(clusterService, clusterName, volumeName, fromBrick,
				toBrick);
		migrateDiskTask.setAutoCommit(autoCommit);
		migrateDiskTask.start();
		taskResource.addTask(clusterName, migrateDiskTask);
		return migrateDiskTask.getTaskInfo().getName(); // Return Task ID
	}
	
	private String getLayout(Boolean isFixLayout, Boolean isMigrateData,
			Boolean isForcedDataMigrate) {
		String layout = "";
		if (isForcedDataMigrate) {
			layout = "forced-data-migrate";
		} else if (isMigrateData) {
			layout = "migrate-data";
		} else if (isFixLayout) {
			layout = "fix-layout";
		}
		return layout;
	}
	
	public String rebalanceStart(String clusterName, String volumeName, Boolean isFixLayout, Boolean isMigrateData,
			Boolean isForcedDataMigrate) {
		RebalanceVolumeTask rebalanceTask = new RebalanceVolumeTask(clusterService, clusterName, volumeName, getLayout(
				isFixLayout, isMigrateData, isForcedDataMigrate));
		rebalanceTask.start();
		taskResource.addTask(clusterName, rebalanceTask);
		return rebalanceTask.getId();
	}

	public void rebalanceStop(String clusterName, String volumeName) {
		// TODO: arrive at the task id and fetch it
		String taskId = "";

		taskResource.getTask(clusterName, taskId).stop();
	}
	
	public void startVolume(String clusterName, GlusterServer onlineServer, Volume volume, Boolean force) {
		glusterUtil.startVolume(volume.getName(), onlineServer.getName(), force);

		// call the start_volume_cifs.py script only if the volume is cifs enabled
		if (volume.isCifsEnable()) {
			startCifsReExport(clusterName, volume.getName());
		}
	}

	public void stopVolume(String clusterName, GlusterServer onlineServer, Volume volume, Boolean force) {
		glusterUtil.stopVolume(volume.getName(), onlineServer.getName(), force);

		// call the stop_volume_cifs.py script only if the volume is cifs enabled
		if (volume.isCifsEnable()) {
			stopCifsReExport(clusterName, volume.getName());
		}
	}
	
	public void logRotate(String clusterName, String volumeName, List<String> brickList) {
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		try {
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}

			glusterUtil.logRotate(volumeName, brickList, onlineServer.getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				glusterUtil.logRotate(volumeName, brickList, onlineServer.getName());
			} else {
				throw new GlusterRuntimeException("Volume [" + volumeName + "] log rotation failed!", e);
			}
		}
	}
	
	public void performVolumeOperation(String clusterName, String volumeName, String operation, Boolean force) {
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		try {
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}

			performOperation(clusterName, volumeName, operation, onlineServer, force);
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				performOperation(clusterName, volumeName, operation, onlineServer, force);
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}

	private void performOperation(String clusterName, String volumeName, String operation, GlusterServer onlineServer,
			Boolean force) {
		Volume volume = null;
		try {
			volume = getVolume(clusterName, volumeName);
		} catch (Exception e) {
			throw new GlusterRuntimeException("Could not fetch volume info for volume [" + volumeName + "]"
					+ e.getMessage());
		}

		if (operation.equals(TASK_START)) {
			startVolume(clusterName, onlineServer, volume, force);
		} else if (operation.equals(TASK_STOP)) {
			stopVolume(clusterName, onlineServer, volume, force);
		} else {
			throw new GlusterValidationException("Invalid operation code [" + operation + "]");
		}
	}
	
	public void removeBricksFromVolume(String clusterName, String volumeName, String bricks, Boolean deleteFlag) {
		// Convert from comma separated string (query parameter)
		List<String> brickList = Arrays.asList(bricks.split(",")); 
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}

		if (volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Volume name must not be empty!");
		}

		if (bricks == null || bricks.isEmpty()) {
			throw new GlusterValidationException("Parameter [" + QUERY_PARAM_BRICKS + "] is missing in request!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		if(deleteFlag == null) {
			deleteFlag = false;
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}
		
		removeBricks(clusterName, volumeName, brickList, onlineServer);
		cleanupDirectories(brickList, volumeName, brickList.size(), deleteFlag);
	}

	private void removeBricks(String clusterName, String volumeName, List<String> brickList, GlusterServer onlineServer) {
		try {
			glusterUtil.removeBricks(volumeName, brickList, onlineServer.getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				glusterUtil.removeBricks(volumeName, brickList, onlineServer.getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}

	private void cleanupDirectories(List<String> bricks, String volumeName, int maxIndex, boolean deleteFlag) {
		String errors = "";
		for (int i = 0; i < maxIndex; i++) {
			String[] brickInfo = bricks.get(i).split(":");
			String serverName = brickInfo[0];
			String brickDirectory = brickInfo[1];

			try {
				serverUtil.executeScriptOnServer(serverName, VOLUME_DIRECTORY_CLEANUP_SCRIPT + " "
						+ brickDirectory + " " + (deleteFlag ? "-d" : ""));
			} catch(Exception e) {
				logger.error("Error while cleaning brick [" + serverName + ":" + brickDirectory + "] of volume ["
						+ volumeName + "] : " + e.getMessage(), e);
				errors += "[" + brickDirectory + "] => " + e.getMessage() + CoreConstants.NEWLINE;
			}
		}
		if(!errors.trim().isEmpty()) {
			throw new GlusterRuntimeException("Volume directory cleanup errors: " + errors.trim());
		}
	}
	
	public void deleteVolume(String clusterName, String volumeName, Boolean deleteFlag) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty");
		}

		if (volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Volume name must not be empty");
		}
		
		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if(onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}
		
		if (deleteFlag == null) {
			deleteFlag = false;
		}
		
		Volume volume = getVolume(clusterName, volumeName);			
		
		List<Brick> bricks = volume.getBricks();
		glusterUtil.deleteVolume(volumeName, onlineServer.getName());

		try {
			postDelete(volumeName, bricks, deleteFlag);
			if (volume.isCifsEnable()) {
				if (volume.getStatus() == VOLUME_STATUS.ONLINE) {
					stopCifsReExport(clusterName, volumeName);
				}
				deleteCifsUsers(clusterName, volumeName);
			}
		} catch(Exception e) {
			throw new GlusterRuntimeException("Volume [" + volumeName
					+ "] deleted from cluster, however following error(s) occurred: " + CoreConstants.NEWLINE
					+ e.getMessage());
		}
	}
	
	private void postDelete(String volumeName, List<Brick> bricks, boolean deleteFlag) {
		for (Brick brick : bricks) {
			String brickDirectory = brick.getBrickDirectory();
			// String mountPoint = brickDirectory.substring(0, brickDirectory.lastIndexOf("/"));

			serverUtil.executeScriptOnServer(brick.getServerName(), VOLUME_DIRECTORY_CLEANUP_SCRIPT + " "
					+ brickDirectory + " " + (deleteFlag ? "-d" : ""));
		}
	}
	
	public void resetVolumeOptions(String clusterName, String volumeName) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		if(volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Volume name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			glusterUtil.resetOptions(volumeName, onlineServer.getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}

				glusterUtil.resetOptions(volumeName, onlineServer.getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
						
		}
	}
	
	public void setVolumeOption(String clusterName, String volumeName, String key, String value) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		if(volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Volume name must not be empty!");
		}

		if(key == null || key.isEmpty()) {
			throw new GlusterValidationException("Option key must not be empty!");
		}
		
		if(value == null || value.isEmpty()) {
			throw new GlusterValidationException("Option value must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterRuntimeException("Cluster [" + clusterName + "] not found!");
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}
		
		try {
			glusterUtil.setOption(volumeName, key, value, onlineServer.getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				// online server has gone offline! try with a different one.
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				if (onlineServer == null) {
					throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
				}
				glusterUtil.setOption(volumeName, key, value, onlineServer.getName());
			} else {
				throw new GlusterRuntimeException(e.getMessage());
			}
		}
	}
	
	public VolumeOptionInfoListResponse getVolumeOptionsInfo(String clusterName) {
		if (clusterName == null || clusterName.isEmpty()) {
			throw new GlusterValidationException("Cluster name must not be empty!");
		}
		
		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		if(cluster.getServers().isEmpty()) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] is empty! Can't fetch Volume Options Information!");
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}
		
		try {
			return glusterUtil.getVolumeOptionsInfo(onlineServer.getName());
		} catch (Exception e) {
			// check if online server has gone offline. If yes, try again one more time.
			if (e instanceof ConnectionException || serverUtil.isServerOnline(onlineServer) == false) {
				onlineServer = clusterService.getNewOnlineServer(clusterName);
				return glusterUtil.getVolumeOptionsInfo(onlineServer.getName());
			} else {
				throw new GlusterRuntimeException("Fetching volume options info failed! [" + e.getMessage() + "]");
			}
		}
	}
}
