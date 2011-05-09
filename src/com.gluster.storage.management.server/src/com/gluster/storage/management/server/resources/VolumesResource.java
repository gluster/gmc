/**
 * VolumesResource.java
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
package com.gluster.storage.management.server.resources;

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OPERATION;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_PAUSE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_SOURCE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_START;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_STATUS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_STOP;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_TARGET;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_VOLUME_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DELETE_OPTION;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DISKS;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DISK_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DOWNLOAD;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_FROM_TIMESTAMP;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_LINE_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_LOG_SEVERITY;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_TO_TIMESTAMP;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_VOLUME_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_VOLUMES;
import static com.gluster.storage.management.core.constants.RESTConstants.SUBRESOURCE_DEFAULT_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.SUBRESOURCE_DISKS;
import static com.gluster.storage.management.core.constants.RESTConstants.SUBRESOURCE_DOWNLOAD;
import static com.gluster.storage.management.core.constants.RESTConstants.SUBRESOURCE_LOGS;
import static com.gluster.storage.management.core.constants.RESTConstants.SUBRESOURCE_OPTIONS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.LogMessage;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.LogMessageListResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.DateUtil;
import com.gluster.storage.management.core.utils.FileUtil;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.gluster.storage.management.server.constants.VolumeOptionsDefaults;
import com.gluster.storage.management.server.utils.GlusterUtil;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path(RESOURCE_PATH_VOLUMES)
public class VolumesResource {
	private static final String PREPARE_BRICK_SCRIPT = "create_volume_directory.py";
	private static final String VOLUME_DIRECTORY_CLEANUP_SCRIPT = "clear_volume_directory.py";
	private static final String VOLUME_BRICK_LOG_SCRIPT = "get_volume_brick_log.py";

	@InjectParam
	private static ServerUtil serverUtil;
	
	@InjectParam
	private static GlusterUtil glusterUtil;
	
	private static final FileUtil fileUtil = new FileUtil();

	@InjectParam
	private VolumeOptionsDefaults volumeOptionsDefaults;

	@GET
	@Produces(MediaType.TEXT_XML)
	public VolumeListResponse getAllVolumes() {
		try {
			return new VolumeListResponse(Status.STATUS_SUCCESS, glusterUtil.getAllVolumes());
		} catch (Exception e) {
			// TODO: log the error
			e.printStackTrace();
			return new VolumeListResponse(new Status(Status.STATUS_CODE_FAILURE, e.getMessage()), null);
		}
	}

	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public Status createVolume(Volume volume) {
		// Create the directories for the volume
		List<String> disks = volume.getDisks();
		Status status = createDirectories(disks, volume.getName());
		if (status.isSuccess()) {
			List<String> bricks = Arrays.asList(status.getMessage().split(" "));
			status = glusterUtil.createVolume(volume, bricks);
			if (status.isSuccess()) {
				Status optionsStatus = glusterUtil.createOptions(volume);
				if (!optionsStatus.isSuccess()) {
					status.setCode(Status.STATUS_CODE_PART_SUCCESS);
					status.setMessage("Error while setting volume options: " + optionsStatus);
				}
			} else {
				Status cleanupStatus = cleanupDirectories(disks, volume.getName(), disks.size(), "-d"); // delete
																										// permanently
				if (!cleanupStatus.isSuccess()) {
					status.setMessage(status.getMessage() + CoreConstants.NEWLINE + "Cleanup errors: "
							+ CoreConstants.NEWLINE + cleanupStatus);
				}
			}
		}
		return status;
	}

	@GET
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	@Produces(MediaType.TEXT_XML)
	public Volume getVolume(@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {
		return glusterUtil.getVolume(volumeName);
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	@Produces(MediaType.TEXT_XML)
	public Status performOperation(@FormParam(FORM_PARAM_OPERATION) String operation,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {

		if (operation.equals(FORM_PARAM_VALUE_START)) {
			return glusterUtil.startVolume(volumeName);
		}
		if (operation.equals(FORM_PARAM_VALUE_STOP)) {
			return glusterUtil.stopVolume(volumeName);
		}
		return new Status(Status.STATUS_CODE_FAILURE, "Invalid operation code [" + operation + "]");
	}

	@DELETE
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	@Produces(MediaType.TEXT_XML)
	public Status deleteVolume(@QueryParam(QUERY_PARAM_VOLUME_NAME) String volumeName,
			@QueryParam(QUERY_PARAM_DELETE_OPTION) boolean deleteFlag) {
		Volume volume = glusterUtil.getVolume(volumeName);
		Status status = glusterUtil.deleteVolume(volumeName);
		
		String deleteOption = "";
		if(deleteFlag) {
			deleteOption = "-d";
		}
		
		if (status.isSuccess()) {
			List<String> disks = volume.getDisks();
			Status postDeleteStatus = postDelete(volumeName, disks, deleteOption);

			if (!postDeleteStatus.isSuccess()) {
				status.setCode(Status.STATUS_CODE_PART_SUCCESS);
				status.setMessage("Error while post deletion operation: " + postDeleteStatus);
			}
		}
		return status;
	}

	@DELETE
	@Path("{" + QUERY_PARAM_VOLUME_NAME + "}/" + SUBRESOURCE_DISKS)
	@Produces(MediaType.TEXT_XML)
	public Status removeBricks(@PathParam(QUERY_PARAM_VOLUME_NAME) String volumeName,
			@QueryParam(QUERY_PARAM_DISKS) String disks, @QueryParam(QUERY_PARAM_DELETE_OPTION) boolean deleteFlag) {
		List<String> bricks = Arrays.asList(disks.split(",")); // Convert from comma separated string (query parameter)
		List<String> volumeBricks = new ArrayList<String>();
		for (String brickInfo : bricks) {
			String diskInfo[] = brickInfo.split(":");
			volumeBricks.add(getBrickForDisk(getVolume(volumeName), diskInfo[1]));
		}

		Status status = glusterUtil.removeBricks(volumeName, volumeBricks);

		String deleteOption = "";
		if(deleteFlag) {
			deleteOption = "-d";
		}
		
		if (status.isSuccess()) {
			Status cleanupStatus = cleanupDirectories(bricks, volumeName, bricks.size(), deleteOption);
			if (!cleanupStatus.isSuccess()) {
				// append cleanup error to prepare brick error
				status.setMessage(status.getMessage() + CoreConstants.NEWLINE + cleanupStatus.getMessage());
			}
		}
		return status;
	}

	private Status postDelete(String volumeName, List<String> disks, String deleteFlag) {
		String serverName, diskName, diskInfo[];
		Status result;
		for (int i = 0; i < disks.size(); i++) {
			diskInfo = disks.get(i).split(":");
			serverName = diskInfo[0];
			diskName = diskInfo[1];
			result = (Status) serverUtil.executeOnServer(true, serverName, VOLUME_DIRECTORY_CLEANUP_SCRIPT + " "
					+ diskName + " " + volumeName + " " + deleteFlag, Status.class);
			if (!result.isSuccess()) {
				return result;
			}
		}
		return new Status(Status.STATUS_CODE_SUCCESS, "Post volume delete operation successfully initiated");
	}

	@POST
	@Path("{" + PATH_PARAM_VOLUME_NAME + " }/" + SUBRESOURCE_OPTIONS)
	@Produces(MediaType.TEXT_XML)
	public Status setOption(@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName,
			@FormParam(RESTConstants.FORM_PARAM_OPTION_KEY) String key,
			@FormParam(RESTConstants.FORM_PARAM_OPTION_VALUE) String value) {
		return glusterUtil.setOption(volumeName, key, value);
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + " }/" + SUBRESOURCE_OPTIONS)
	@Produces(MediaType.TEXT_XML)
	public Status resetOptions(@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {
		return glusterUtil.resetOptions(volumeName);
	}

	@GET
	@Path(SUBRESOURCE_DEFAULT_OPTIONS)
	@Produces(MediaType.TEXT_XML)
	public VolumeOptionInfoListResponse getDefaultOptions() {
		// TODO: Fetch all volume options with their default values from GlusterFS
		// whenever such a CLI command is made available in GlusterFS
		return new VolumeOptionInfoListResponse(Status.STATUS_SUCCESS, volumeOptionsDefaults.getDefaults());
	}

	@SuppressWarnings("rawtypes")
	private Status prepareBrick(String serverName, String diskName, String volumeName) {
		Object response = serverUtil.executeOnServer(true, serverName, PREPARE_BRICK_SCRIPT + " "
				+ diskName + " " + volumeName, GenericResponse.class);
		if(response instanceof GenericResponse) {
			return ((GenericResponse)response).getStatus();
		} else {
			// in case of script failure on server, a Status object will be returned
			return (Status) response;
		}		
	}

	private Status createDirectories(List<String> disks, String volumeName) {
		List<String> bricks = new ArrayList<String>();
		Status status = null;
		for (int i = 0; i < disks.size(); i++) {
			String disk = disks.get(i);

			String[] diskParts = disk.split(":");
			String serverName = diskParts[0];
			String diskName = diskParts[1];

			status = prepareBrick(serverName, diskName, volumeName);
			if (status.isSuccess()) {
				String brickDir = status.getMessage().trim();
				bricks.add(serverName + ":" + brickDir);
			} else {
				// Brick preparation failed. Cleanup directories already created and return failure status
				Status cleanupStatus = cleanupDirectories(disks, volumeName, i + 1, "-d"); // delete permanently
				if (!cleanupStatus.isSuccess()) {
					// append cleanup error to prepare brick error
					status.setMessage(status.getMessage() + CoreConstants.NEWLINE + cleanupStatus.getMessage());
				}
				return status;
			}
		}
		status.setMessage(bricksAsString(bricks));
		return status;
	}

	// TODO Can be removed and use StringUtil.ListToString(List<String> list, String delimiter)
	private String bricksAsString(List<String> bricks) {
		String bricksStr = "";
		for (String brickInfo : bricks) {
			bricksStr += brickInfo + " ";
		}
		return bricksStr.trim();
	}

	@SuppressWarnings("rawtypes")
	private Status cleanupDirectories(List<String> disks, String volumeName, int maxIndex, String deleteFlag) {
		String serverName, diskName, diskInfo[];
		Status result;
		for (int i = 0; i < maxIndex; i++) {
			diskInfo = disks.get(i).split(":");
			serverName = diskInfo[0];
			diskName = diskInfo[1];
			
			Object response = serverUtil.executeOnServer(true, serverName, VOLUME_DIRECTORY_CLEANUP_SCRIPT
					+ " " + diskName + " " + volumeName + " " + deleteFlag, GenericResponse.class);
			if(response instanceof GenericResponse) {
				result = ((GenericResponse)response).getStatus();
				if (!result.isSuccess()) {
					// TODO: append error and continue with cleaning up of other directories
					return result;
				}
			} else {
				// TODO: append error and continue with cleaning up of other directories
				// In case of script execution failure, a Status object will be returned.
				return (Status)response;
			}
		}
		return new Status(Status.STATUS_CODE_SUCCESS, "Directories cleaned up successfully!");
	}
	
	private List<LogMessage> getBrickLogs(Volume volume, String brickName, Integer lineCount)
			throws GlusterRuntimeException {
		// brick name format is <serverName>:<brickDirectory>
		String[] brickParts = brickName.split(":");
		String serverName = brickParts[0];
		String brickDir = brickParts[1];

		String logDir = glusterUtil.getLogLocation(volume.getName(), brickName);
		String logFileName = glusterUtil.getLogFileNameForBrickDir(brickDir);
		String logFilePath = logDir + CoreConstants.FILE_SEPARATOR + logFileName;

		// Usage: get_volume_disk_log.py <volumeName> <diskName> <lineCount>
		Object responseObj = serverUtil.executeOnServer(true, serverName, VOLUME_BRICK_LOG_SCRIPT + " " + logFilePath
				+ " " + lineCount, LogMessageListResponse.class);
		Status status = null;
		LogMessageListResponse response = null;
		if (responseObj instanceof LogMessageListResponse) {
			response = (LogMessageListResponse) responseObj;
			status = response.getStatus();
		} else {
			status = (Status) responseObj;
		}

		if (!status.isSuccess()) {
			throw new GlusterRuntimeException(status.toString());
		}

		// populate disk and trim other fields
		List<LogMessage> logMessages = response.getLogMessages();
		for (LogMessage logMessage : logMessages) {
			logMessage.setDisk(getDiskForBrick(volume, brickName));
			logMessage.setMessage(logMessage.getMessage().trim());
			logMessage.setSeverity(logMessage.getSeverity().trim());
		}
		return logMessages;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + SUBRESOURCE_LOGS + "/" + SUBRESOURCE_DOWNLOAD)
	public StreamingOutput getLogs(@PathParam(PATH_PARAM_VOLUME_NAME) final String volumeName) {
		return new StreamingOutput() {
			
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				Volume volume = getVolume(volumeName);
				try {
					File archiveFile = new File(downloadLogs(volume));
					output.write(fileUtil.readFileAsByteArray(archiveFile));
					archiveFile.delete();
				} catch (Exception e) {
					e.printStackTrace();
					throw new GlusterRuntimeException("Exception while downloading/archiving volume log files!", e);
				}
			}
		};
	}

	private String downloadLogs(Volume volume) {
		// create temporary directory
		File tempDir = fileUtil.createTempDir();
		String tempDirPath = tempDir.getPath();
		
		for(String brickName : volume.getBricks()) {
			// brick name format is <serverName>:<brickDirectory>
			String[] brickParts = brickName.split(":");
			String serverName = brickParts[0];
			String brickDir = brickParts[1];
			
			String logDir = glusterUtil.getLogLocation(volume.getName(), brickName);
			String logFileName = glusterUtil.getLogFileNameForBrickDir(brickDir);
			String logFilePath = logDir + CoreConstants.FILE_SEPARATOR + logFileName;

			String logContents = serverUtil.getFileFromServer(serverName, logFilePath);
			fileUtil.createTextFile(tempDirPath + CoreConstants.FILE_SEPARATOR + logFileName, logContents);
		}
		
		String gzipPath = fileUtil.getTempDirName() + CoreConstants.FILE_SEPARATOR + volume.getName() + "-logs.tar.gz";
		new ProcessUtil().executeCommand("tar", "czvf", gzipPath, "-C", tempDir.getParent(), tempDir.getName());
		
		// delete the temp directory
		fileUtil.recursiveDelete(tempDir);
		
		return gzipPath;
	}

	@GET
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + SUBRESOURCE_LOGS)
	public LogMessageListResponse getLogs(@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName,
			@QueryParam(QUERY_PARAM_DISK_NAME) String diskName, @QueryParam(QUERY_PARAM_LOG_SEVERITY) String severity,
			@QueryParam(QUERY_PARAM_FROM_TIMESTAMP) String fromTimestamp,
			@QueryParam(QUERY_PARAM_TO_TIMESTAMP) String toTimestamp,
			@QueryParam(QUERY_PARAM_LINE_COUNT) Integer lineCount,
			@QueryParam(QUERY_PARAM_DOWNLOAD) Boolean download) {
		List<LogMessage> logMessages = null;

		try {
			Volume volume = getVolume(volumeName);
			if (diskName == null || diskName.isEmpty()) {
				logMessages = getLogsForAllBricks(volume, lineCount);
			} else {
				// fetch logs for given brick of the volume
				logMessages = getBrickLogs(volume, getBrickForDisk(volume, diskName), lineCount);
			}
		} catch (Exception e) {
			return new LogMessageListResponse(new Status(e), null);
		}

		filterLogsBySeverity(logMessages, severity);
		filterLogsByTime(logMessages, fromTimestamp, toTimestamp);
		return new LogMessageListResponse(Status.STATUS_SUCCESS, logMessages);
	}

	private void filterLogsByTime(List<LogMessage> logMessages, String fromTimestamp, String toTimestamp) {
		Date fromTime = null, toTime = null;

		if (fromTimestamp != null && !fromTimestamp.isEmpty()) {
			fromTime = DateUtil.stringToDate(fromTimestamp);
		}

		if (toTimestamp != null && !toTimestamp.isEmpty()) {
			toTime = DateUtil.stringToDate(toTimestamp);
		}

		List<LogMessage> messagesToRemove = new ArrayList<LogMessage>();
		for (LogMessage logMessage : logMessages) {
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

	private void filterLogsBySeverity(List<LogMessage> logMessages, String severity) {
		if (severity == null || severity.isEmpty()) {
			return;
		}

		List<LogMessage> messagesToRemove = new ArrayList<LogMessage>();
		for (LogMessage logMessage : logMessages) {
			if (!logMessage.getSeverity().equals(severity)) {
				messagesToRemove.add(logMessage);
			}
		}
		logMessages.removeAll(messagesToRemove);
	}

	private List<LogMessage> getLogsForAllBricks(Volume volume, Integer lineCount) {
		List<LogMessage> logMessages;
		logMessages = new ArrayList<LogMessage>();
		// fetch logs for every brick of the volume
		for (String brick : volume.getBricks()) {
			logMessages.addAll(getBrickLogs(volume, brick, lineCount));
		}

		// Sort the log messages based on log timestamp
		Collections.sort(logMessages, new Comparator<LogMessage>() {
			@Override
			public int compare(LogMessage message1, LogMessage message2) {
				return message1.getTimestamp().compareTo(message2.getTimestamp());
			}
		});

		return logMessages;
	}

	@POST
	@Path("{" + QUERY_PARAM_VOLUME_NAME + "}/" + SUBRESOURCE_DISKS)
	public Status addDisks(@PathParam(QUERY_PARAM_VOLUME_NAME) String volumeName,
			@FormParam(QUERY_PARAM_DISKS) String disks) {

		List<String> diskList = Arrays.asList(disks.split(",")); // Convert from comma separated sting (query parameter)
																	// to list
		Status status = createDirectories(diskList, volumeName);
		if (status.isSuccess()) {
			List<String> bricks = Arrays.asList(status.getMessage().split(" "));
			status = glusterUtil.addBricks(volumeName, bricks);

			if (!status.isSuccess()) {
				Status cleanupStatus = cleanupDirectories(diskList, volumeName, diskList.size(), "-d"); // Remove the
																										// directories
																										// if created
				if (!cleanupStatus.isSuccess()) {
					// append cleanup error to prepare brick error
					status.setMessage(status.getMessage() + CoreConstants.NEWLINE + cleanupStatus.getMessage());
				}
			}
		}
		return status;
	}

	@PUT
	@Path("{" + QUERY_PARAM_VOLUME_NAME + "}/" + SUBRESOURCE_DISKS)
	public Status replaceDisk(@PathParam(QUERY_PARAM_VOLUME_NAME) String volumeName,
			@FormParam(FORM_PARAM_VALUE_SOURCE) String diskFrom, @FormParam(FORM_PARAM_VALUE_TARGET) String diskTo,
			@FormParam(FORM_PARAM_OPERATION) String operation) {
		return glusterUtil.migrateDisk(volumeName, diskFrom, diskTo, operation);
	}

	private String getBrickForDisk(Volume volume, String diskName) {
		int index = volume.getDisks().indexOf(diskName);
		return volume.getBricks().get(index);
	}

	private String getDiskForBrick(Volume volume, String brickName) {
		int index = volume.getBricks().indexOf(brickName);
		return volume.getDisks().get(index);
	}

	public static void main(String[] args) throws ClassNotFoundException {
		VolumesResource vr = new VolumesResource();
		// VolumeListResponse response = vr.getAllVolumes();
		// for (Volume volume : response.getVolumes()) {
		// System.out.println("\nName:" + volume.getName() + "\nType: " + volume.getVolumeTypeStr() + "\nStatus: "
		// + volume.getStatusStr());
		// }
		Volume volume = new Volume();
		volume.setName("vol3");
		volume.setTransportType(TRANSPORT_TYPE.ETHERNET);
		List<String> disks = new ArrayList<String>();
		disks.add("192.168.1.210:sdb");
		volume.addDisks(disks);
		volume.setAccessControlList("192.168.*");
		// Status status = vr.createVolume(volume);
		// System.out.println(status.getMessage());
		Form form = new Form();
		form.add("volumeName", volume.getName());
		form.add(RESTConstants.FORM_PARAM_DELETE_OPTION, 1);
		Status status = vr.deleteVolume("Vol2", true);
		System.out.println("Code : " + status.getCode());
		System.out.println("Message " + status.getMessage());
	}
}
