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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.StringUtil;

/**
 * Gluster Interface for GlusterFS version 3.2.3
 */
@Component
@Lazy(value=true)
public class Gluster323InterfaceService extends AbstractGlusterInterface {

	private static final String VOLUME_NAME_PFX = "Volume Name:";
	private static final String VOLUME_TYPE_PFX = "Type:";
	private static final String VOLUME_STATUS_PFX = "Status:";
	private static final String VOLUME_NUMBER_OF_BRICKS = "Number of Bricks:";
	private static final String VOLUME_TRANSPORT_TYPE_PFX = "Transport-type:";
	private static final String VOLUME_BRICKS_GROUP_PFX = "Bricks";
	private static final String VOLUME_OPTIONS_RECONFIG_PFX = "Options Reconfigured";
	private static final String VOLUME_LOG_LOCATION_PFX = "log file location:";
	private static final String VOLUME_TYPE_DISTRIBUTE = "Distribute";
	private static final String VOLUME_TYPE_REPLICATE = "Replicate";
	private static final String VOLUME_TYPE_DISTRIBUTED_REPLICATTE = "Distributed-Replicate";
	private static final String VOLUME_TYPE_STRIPE = "Stripe";
	private static final String VOLUME_TYPE_DISTRIBUTED_STRIPE = "Distributed-Stripe";
	
	private static final String BRICK_STATUS_SCRIPT = "get_brick_status.py";
	private static final Logger logger = Logger.getLogger(Gluster323InterfaceService.class);

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#addServer(java.lang.String, java.lang.String)
	 */
	@Override
	public void addServer(String existingServer, String newServer) {
		serverUtil.executeOnServer(existingServer, "gluster peer probe " + newServer);
		// reverse peer probe to ensure that host names appear in peer status on both sides
		serverUtil.executeOnServer(newServer, "gluster peer probe " + existingServer);
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#startVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public void startVolume(String volumeName, String knownServer, Boolean force) {
		serverUtil.executeOnServer(knownServer, "gluster volume start " + volumeName + ((force) ? " force" : ""));
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#stopVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public void stopVolume(String volumeName, String knownServer, Boolean force) {
		serverUtil.executeOnServer(knownServer, "gluster --mode=script volume stop " + volumeName
				+ ((force) ? " force" : ""));
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#resetOptions(java.lang.String, java.lang.String)
	 */
	@Override
	public void resetOptions(String volumeName, String knownServer) {
		serverUtil.executeOnServer(knownServer, "gluster volume reset " + volumeName);
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#createVolume(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void createVolume(String knownServer, String volumeName, String volumeTypeStr, String transportTypeStr,
			Integer count, String bricks, String accessProtocols, String options) {

		// TODO: Disable NFS if required depending on value of accessProtocols
		VOLUME_TYPE volType = Volume.getVolumeTypeByStr(volumeTypeStr);
		String volTypeArg = null;
		if (volType == VOLUME_TYPE.REPLICATE || volType == VOLUME_TYPE.DISTRIBUTED_REPLICATE) {
			volTypeArg = "replica";
		} else if (volType == VOLUME_TYPE.STRIPE || volType == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			volTypeArg = "stripe";
		}

		String transportTypeArg = null;
		TRANSPORT_TYPE transportType = Volume.getTransportTypeByStr(transportTypeStr);
		transportTypeArg = (transportType == TRANSPORT_TYPE.ETHERNET) ? "tcp" : "rdma";

		String command = prepareVolumeCreateCommand(volumeName, StringUtil.extractList(bricks, ","), count,
				volTypeArg, transportTypeArg);

		serverUtil.executeOnServer(knownServer, command);

		try {
			createOptions(volumeName, StringUtil.extractMap(options, ",", "="), knownServer);
		} catch(Exception e) {
			throw new GlusterRuntimeException(
					"Volume created successfully, however following errors occurred while setting options: "
							+ CoreConstants.NEWLINE + e.getMessage());
		}
	}

	private String prepareVolumeCreateCommand(String volumeName, List<String> brickDirectories, int count,
			String volumeType, String transportTypeStr) {
		StringBuilder command = new StringBuilder("gluster volume create " + volumeName + " ");
		if (volumeType != null) {
			command.append(volumeType + " " + count + " ");
		}
		command.append("transport " + transportTypeStr);
		for (String brickDir : brickDirectories) {
			command.append(" " + brickDir);
		}
		return command.toString();
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#createOptions(java.lang.String, java.util.Map, java.lang.String)
	 */
	@Override
	public void createOptions(String volumeName, Map<String, String> options, String knownServer) {
		String errors = "";
		if (options != null) {
			for (Entry<String, String> option : options.entrySet()) {
				String key = option.getKey();
				String value = option.getValue();
				
				try {
					setOption(volumeName, key, value, knownServer);
				} catch(Exception e) {
					// append error
					errors += e.getMessage() + CoreConstants.NEWLINE;
				}
			}
		}
		if (!errors.trim().isEmpty()) {
			throw new GlusterRuntimeException("Errors while setting option(s) on volume [" + volumeName + "] : "
					+ errors.trim());
		}
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#setOption(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setOption(String volumeName, String key, String value, String knownServer) {
		serverUtil.executeOnServer(knownServer, "gluster volume set " + volumeName + " " + key + " " + "\""
				+ value + "\"");
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#deleteVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteVolume(String volumeName, String knownServer) {
		serverUtil.executeOnServer(knownServer, "gluster --mode=script volume delete " + volumeName);
	}

	private String getVolumeInfo(String volumeName, String knownServer) {
		return serverUtil.executeOnServer(knownServer, "gluster volume info " + volumeName, String.class);
	}

	private String getVolumeInfo(String knownServer) {
		return serverUtil.executeOnServer(knownServer, "gluster volume info", String.class);
	}

	private boolean readVolumeType(Volume volume, String line) {
		String volumeType = StringUtil.extractToken(line, VOLUME_TYPE_PFX);
		if (volumeType != null) {
			if (volumeType.equals(VOLUME_TYPE_DISTRIBUTE)) {
				volume.setVolumeType(VOLUME_TYPE.DISTRIBUTE);

			} else if (volumeType.equals(VOLUME_TYPE_REPLICATE)) {
				volume.setVolumeType(VOLUME_TYPE.REPLICATE);
				volume.setReplicaCount(Volume.DEFAULT_REPLICA_COUNT);
			
			} else if (	volumeType.equals(VOLUME_TYPE_DISTRIBUTED_REPLICATTE) ){
				volume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_REPLICATE);
				volume.setReplicaCount(Volume.DEFAULT_REPLICA_COUNT);
			
			} else if (	volumeType.equals(VOLUME_TYPE_STRIPE) ){
				volume.setVolumeType(VOLUME_TYPE.STRIPE);
				volume.setReplicaCount(Volume.DEFAULT_REPLICA_COUNT);
			
			} else if (	volumeType.equals(VOLUME_TYPE_DISTRIBUTED_STRIPE) ){
				volume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_STRIPE);
				volume.setReplicaCount(Volume.DEFAULT_STRIPE_COUNT);
			}
			return true;
		}
		return false;
	}

	private void readReplicaOrStripeCount(Volume volume, String line) {
		if (StringUtil.extractToken(line, "x") != null) {
			// expected formated of line is "Number of Bricks: 3 x 2 = 6"
			int count = Integer.parseInt(line.split("x")[1].split("=")[0].trim());
			if (volume.getVolumeType() == VOLUME_TYPE.STRIPE
					|| volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
				volume.setStripeCount(count);
			} else if (volume.getVolumeType() == VOLUME_TYPE.REPLICATE
					|| volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_REPLICATE) {
				volume.setReplicaCount(count);
				volume.setStripeCount(0);
			}
		}
		return;
	}

	private boolean readVolumeStatus(Volume volume, String line) {
		String volumeStatus = StringUtil.extractToken(line, VOLUME_STATUS_PFX);
		if (volumeStatus != null) {
			volume.setStatus(volumeStatus.equals("Started") ? VOLUME_STATUS.ONLINE : VOLUME_STATUS.OFFLINE);
			return true;
		}
		return false;
	}

	private boolean readTransportType(Volume volume, String line) {
		String transportType = StringUtil.extractToken(line, VOLUME_TRANSPORT_TYPE_PFX);
		if (transportType != null) {
			volume.setTransportType(transportType.equals("tcp") ? TRANSPORT_TYPE.ETHERNET : TRANSPORT_TYPE.INFINIBAND);
			return true;
		}
		return false;
	}

	private boolean readBrick(Volume volume, String brickLine) {
		BRICK_STATUS brickStatus;
		if (brickLine.matches("Brick[0-9]+:.*")) {
			// line: "Brick1: server1:/export/md0/volume-name"
			String brickName = brickLine.split(": ")[1];
			String[] brickParts = brickLine.split(":");
			String serverName = brickParts[1].trim();
			String brickDir = brickParts[2].trim();
			//To get the brick status
			brickStatus = getBrickStatus(serverName, volume.getName(), brickName);
			
			addBrickToVolume(volume, serverName, brickDir, brickStatus);
			return true;
		}
		return false;
	}

	private void addBrickToVolume(Volume volume, String serverName, String brickDir, BRICK_STATUS status) {
		volume.addBrick(new Brick(serverName, status, brickDir));
	}
	
	// Do not throw exception, Gracefully handle as Offline brick. 
	private BRICK_STATUS getBrickStatus(String serverName, String volumeName, String brick){
		try {
			String output = serverUtil.executeScriptOnServer(serverName, BRICK_STATUS_SCRIPT + " " + volumeName
					+ " " + brick, String.class);
			if (output.equals(CoreConstants.ONLINE)) {
				return BRICK_STATUS.ONLINE;
			} else {
				return BRICK_STATUS.OFFLINE;
			}
		} catch(Exception e) { // Particularly interested on ConnectionExecption, if the server is offline
			logger.warn("Exception while fetching brick status for [" + volumeName + "][" + brick
					+ "]. Marking it as offline!", e);
			return BRICK_STATUS.OFFLINE;
		}
	}

	private boolean readBrickGroup(String line) {
		return StringUtil.extractToken(line, VOLUME_BRICKS_GROUP_PFX) != null;
	}

	private boolean readOptionReconfigGroup(String line) {
		return StringUtil.extractToken(line, VOLUME_OPTIONS_RECONFIG_PFX) != null;
	}

	private boolean readOption(Volume volume, String line) {
		if (line.matches("^[^:]*:.*$")) {
			int index = line.indexOf(':');
			volume.setOption(line.substring(0, index).trim(), line.substring(index + 1, line.length()).trim());
			
			if (line.substring(0, index).trim().equals(Volume.OPTION_NFS_DISABLE)) {
				if (line.substring(index + 1, line.length()).trim().equals(GlusterConstants.ON)) {
					volume.disableNFS();
				} else {
					volume.enableNFS();
				}
			}
			
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#getVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public Volume getVolume(String volumeName, String knownServer) {
		return parseVolumeInfo(getVolumeInfo(volumeName, knownServer)).get(0);
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#getAllVolumes(java.lang.String)
	 */
	@Override
	public List<Volume> getAllVolumes(String knownServer) {
		return parseVolumeInfo(getVolumeInfo(knownServer));
	}

	private List<Volume> parseVolumeInfo(String volumeInfoText) {
		List<Volume> volumes = new ArrayList<Volume>();
		boolean isBricksGroupFound = false;
		boolean isOptionReconfigFound = false;
		Volume volume = null;

		for (String line : volumeInfoText.split(CoreConstants.NEWLINE)) {
			String volumeName = StringUtil.extractToken(line, VOLUME_NAME_PFX);
			if (volumeName != null) {
				if (volume != null) {
					volumes.add(volume);
				}

				// prepare next volume to be read
				volume = new Volume();
				volume.setName(volumeName);
				isBricksGroupFound = isOptionReconfigFound = false;
				continue;
			}

			if (readVolumeType(volume, line))
				continue;
			if (StringUtil.extractToken(line, VOLUME_NUMBER_OF_BRICKS) != null) {
				readReplicaOrStripeCount(volume, line);
			}
			if (readVolumeStatus(volume, line))
				continue;
			if (readTransportType(volume, line))
				continue;
			if (readBrickGroup(line)) {
				isBricksGroupFound = true;
				continue;
			}

			if (isBricksGroupFound) {
				if (readBrick(volume, line)) {
					continue;
				} else {
					isBricksGroupFound = false;
				}
			}

			if (readOptionReconfigGroup(line)) {
				isOptionReconfigFound = true;
				continue;
			}

			if (isOptionReconfigFound) {
				if (readOption(volume, line)) {
					continue;
				} else {
					isOptionReconfigFound = false;
				}
			}
		}
		
		// add the last read volume
		if (volume != null) {
			volumes.add(volume);
		}
		
		return volumes;
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#addBricks(java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void addBricks(String volumeName, List<String> bricks, String knownServer) {
		StringBuilder command = new StringBuilder("gluster volume add-brick " + volumeName);
		for (String brickDir : bricks) {
			command.append(" " + brickDir);
		}
		
		serverUtil.executeOnServer(knownServer, command.toString());
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#getLogLocation(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getLogLocation(String volumeName, String brickName, String knownServer) {
		String command = "gluster volume log locate " + volumeName + " " + brickName;
		String output = serverUtil.executeOnServer(knownServer, command, String.class);
		if (output.startsWith(VOLUME_LOG_LOCATION_PFX)) {
			return output.substring(VOLUME_LOG_LOCATION_PFX.length()).trim();
		}

		throw new GlusterRuntimeException("Couldn't parse output of command [" + command + "]. Output [" + output
				+ "] doesn't start with prefix [" + VOLUME_LOG_LOCATION_PFX + "]");
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#getLogFileNameForBrickDir(java.lang.String)
	 */
	@Override
	public String getLogFileNameForBrickDir(String brickDir) {
		String logFileName = brickDir;
		if (logFileName.length() > 0 && logFileName.charAt(0) == '/') {
			logFileName = logFileName.replaceFirst("/", "");
		}
		logFileName = logFileName.replaceAll("/", "-") + ".log";
		return logFileName;
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#removeBricks(java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void removeBricks(String volumeName, List<String> bricks, String knownServer) {
		StringBuilder command = new StringBuilder("gluster --mode=script volume remove-brick " + volumeName);
		for (String brickDir : bricks) {
			command.append(" " + brickDir);
		}
		serverUtil.executeOnServer(knownServer, command.toString());
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#removeServer(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeServer(String existingServer, String serverName) {
		serverUtil.executeOnServer(existingServer, "gluster --mode=script peer detach " + serverName);
	}
	
	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#checkRebalanceStatus(java.lang.String, java.lang.String)
	 */
	@Override
	public TaskStatus checkRebalanceStatus(String serverName, String volumeName) {
		String command = "gluster volume rebalance " + volumeName + " status";
		String output = serverUtil.executeOnServer(serverName, command, String.class).trim();
		TaskStatus taskStatus = new TaskStatus();
		if (output.matches("^rebalance completed.*")) {
			taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
		} else if (output.matches(".*in progress.*")) {
			taskStatus.setCode(Status.STATUS_CODE_RUNNING);
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		taskStatus.setMessage(output);
		return taskStatus;
	}
	
	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#stopRebalance(java.lang.String, java.lang.String)
	 */
	@Override
	public void stopRebalance(String serverName, String volumeName) {
		String command = "gluster volume rebalance " + volumeName + " stop";
		serverUtil.executeOnServer(serverName, command);
	}

	/**
	 * Performs given Brick Migration (replace-brick) Operation on given volume
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume on which the Brick Migration Operation is to be executed
	 * @param fromBrick
	 *            The source Brick (being replaced)
	 * @param toBrick
	 *            The destination Brick (which is replacing the source Brick)
	 * @param operation
	 * @return
	 */
	private String performBrickMigrationOperation(String serverName, String volumeName, String fromBrick,
			String toBrick, String operation) {
		String command = "gluster volume replace-brick " + volumeName + " " + fromBrick + " " + toBrick + " "
				+ operation;
		return serverUtil.executeOnServer(serverName, command, String.class);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#executeBrickMigration(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void startBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick) {
		performBrickMigrationOperation(serverName, volumeName, fromBrick, toBrick, "start");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.services.GlusterInterface#pauseBrickMigration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void pauseBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick) {
		performBrickMigrationOperation(serverName, volumeName, fromBrick, toBrick, "pause");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.services.GlusterInterface#stopBrickMigration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void stopBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick) {
		performBrickMigrationOperation(serverName, volumeName, fromBrick, toBrick, "abort");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.services.GlusterInterface#commitBrickMigration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void commitBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick) {
		performBrickMigrationOperation(serverName, volumeName, fromBrick, toBrick, "commit");
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.services.GlusterInterface#checkBrickMigrationStatus(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public TaskStatus checkBrickMigrationStatus(String serverName, String volumeName, String fromBrick, String toBrick) {
		String output = performBrickMigrationOperation(serverName, volumeName, fromBrick, toBrick, "status");

		TaskStatus taskStatus = new TaskStatus();
		if (output.matches("^Number of files migrated.*Migration complete$")
				|| output.matches("^Number of files migrated = 0 .*Current file=")) {
			// Note: Workaround - if no file in the volume brick to migrate,
			// Gluster CLI is not giving proper (complete) status
			taskStatus.setCode(Status.STATUS_CODE_COMMIT_PENDING);
			taskStatus.setMessage(output.replaceAll("Migration complete", "Commit pending"));
		} else if (output.matches("^Number of files migrated.*Current file=.*")) {
			taskStatus.setCode(Status.STATUS_CODE_RUNNING);
		} else if (output.matches("^replace brick has been paused.*")) {
			taskStatus.setCode(Status.STATUS_CODE_PAUSE);
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		
		taskStatus.setMessage(output);
		return taskStatus;
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gateway.utils.GlusterInterface#getVolumeOptionsInfo(java.lang.String)
	 */
	@Override
	public VolumeOptionInfoListResponse getVolumeOptionsInfo(String serverName) {
		return serverUtil.executeOnServer(serverName, "gluster volume set help-xml", VolumeOptionInfoListResponse.class);
	}
	
	public void logRotate(String volumeName, List<String> brickList, String knownServer) {
		if (brickList == null || brickList.size() > 0) {
			for (String brickDir : brickList) {
				serverUtil.executeOnServer(knownServer, "gluster volume log rotate " + volumeName + " " + brickDir);
			}
		} else {
			serverUtil.executeOnServer(knownServer, "gluster volume log rotate " + volumeName);
		}
	}
}
