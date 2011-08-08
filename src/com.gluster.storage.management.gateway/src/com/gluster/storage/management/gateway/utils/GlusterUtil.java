/**
 * GlusterUtil.java
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
package com.gluster.storage.management.gateway.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.InitDiskStatusResponse;
import com.gluster.storage.management.core.model.InitDiskStatusResponse.FORMAT_STATUS;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.StringUtil;
import com.gluster.storage.management.gateway.resources.v1_0.TasksResource;

@Component
public class GlusterUtil {
	private static final String glusterFSminVersion = "3.1";

	private static final String HOSTNAME_PFX = "Hostname:";
	private static final String UUID_PFX = "Uuid:";
	private static final String STATE_PFX = "State:";
	private static final String GLUSTER_SERVER_STATUS_ONLINE = "Connected";

	private static final String VOLUME_NAME_PFX = "Volume Name:";
	private static final String VOLUME_TYPE_PFX = "Type:";
	private static final String VOLUME_STATUS_PFX = "Status:";
	private static final String VOLUME_NUMBER_OF_BRICKS = "Number of Bricks:";
	private static final String VOLUME_TRANSPORT_TYPE_PFX = "Transport-type:";
	private static final String VOLUME_BRICKS_GROUP_PFX = "Bricks";
	private static final String VOLUME_OPTIONS_RECONFIG_PFX = "Options Reconfigured";
	private static final String VOLUME_OPTION_AUTH_ALLOW_PFX = "auth.allow:";
	private static final String VOLUME_LOG_LOCATION_PFX = "log file location:";
	private static final String VOLUME_TYPE_DISTRIBUTE = "Distribute";
	private static final String VOLUME_TYPE_REPLICATE = "Replicate";
	private static final String GLUSTERD_INFO_FILE = "/etc/glusterd/glusterd.info";
	
	private static final GlusterCoreUtil glusterCoreUtil = new GlusterCoreUtil();
	
	private static final String INITIALIZE_DISK_STATUS_SCRIPT = "get_format_device_status.py";
	private static final String BRICK_STATUS_SCRIPT = "get_brick_status.py";

	@Autowired
	private SshUtil sshUtil;
	
	@Autowired
	private ServerUtil serverUtil;
	
	@Autowired
	private TasksResource taskResource; 

	public void setSshUtil(SshUtil sshUtil) {
		this.sshUtil = sshUtil;
	}

	public SshUtil getSshUtil() {
		return sshUtil;
	}

	/**
	 * Extract value of given token from given line. It is assumed that the token, if present, will be of the following
	 * form: <code>token: value</code>
	 * 
	 * @param line
	 *            Line to be analyzed
	 * @param token
	 *            Token whose value is to be extracted
	 * @return Value of the token, if present in the line
	 */
	private final String extractToken(String line, String token) {
		if (line.contains(token)) {
			return line.split(token)[1].trim();
		}
		return null;
	}

	public GlusterServer getGlusterServer(GlusterServer onlineServer, String serverName) {
		List<GlusterServer> servers = getGlusterServers(onlineServer);
		for (GlusterServer server : servers) {
			if (server.getName().equalsIgnoreCase(serverName)) {
				return server;
			}
		}
		return null;
	}
	
	private String getUuid(String serverName) {
		ProcessResult result = getSshUtil().executeRemote(serverName, "cat " + GLUSTERD_INFO_FILE);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't read file [" + GLUSTERD_INFO_FILE + "]. Error: "
					+ result.toString());
		}
		return result.getOutput().split("=")[1];
	}

	public List<GlusterServer> getGlusterServers(GlusterServer knownServer) {
		String output = getPeerStatus(knownServer.getName());
		if (output == null) {
			return null;
		}

		knownServer.setUuid(getUuid(knownServer.getName()));
		
		List<GlusterServer> glusterServers = new ArrayList<GlusterServer>();
		glusterServers.add(knownServer);
		
		GlusterServer server = null;
		boolean foundHost = false;
		boolean foundUuid = false;
		for (String line : output.split(CoreConstants.NEWLINE)) {
			if (foundHost && foundUuid) {
				// Host and UUID is found, we should look for state
				String state = extractToken(line, STATE_PFX);
				if (state != null) {
					server.setStatus(state.contains(GLUSTER_SERVER_STATUS_ONLINE) ? SERVER_STATUS.ONLINE
							: SERVER_STATUS.OFFLINE);
					// Completed populating current server. Add it to the list
					// and reset all related variables.
					glusterServers.add(server);

					foundHost = false;
					foundUuid = false;
					server = null;
				}
			} else if (foundHost) {
				// Host is found, look for UUID
				String uuid = extractToken(line, UUID_PFX);
				if (uuid != null) {
					server.setUuid(uuid);
					foundUuid = true;
				}
			} else {
				// Look for the next host
				if (server == null) {
					server = new GlusterServer();
				}
				String hostName = extractToken(line, HOSTNAME_PFX);
				if (hostName != null) {
					server.setName(hostName);
					foundHost = true;
				}
			}

		}
		return glusterServers;
	}

	public List<String> getGlusterServerNames(String knownServer) {
		String output = getPeerStatus(knownServer);
		if (output == null) {
			return null;
		}

		List<String> glusterServerNames = new ArrayList<String>();
		for (String line : output.split(CoreConstants.NEWLINE)) {
			String hostName = extractToken(line, HOSTNAME_PFX);
			if (hostName != null) {
				glusterServerNames.add(hostName);
			}
		}
		return glusterServerNames;
	}

	/**
	 * @param knownServer
	 *            A known server on which the gluster command will be executed to fetch peer status
	 * @return Outout of the "gluster peer status" command
	 */
	private String getPeerStatus(String knownServer) {
		String output;
		ProcessResult result = getSshUtil().executeRemote(knownServer, "gluster peer status");
		if (!result.isSuccess()) {
			output = null;
		}
		output = result.getOutput();
		return output;
	}

	public void addServer(String existingServer, String newServer) {
		ProcessResult result = sshUtil.executeRemote(existingServer, "gluster peer probe " + newServer);
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't probe server [" + newServer + "] from [" + existingServer
					+ "]. Error: " + result);
		}
		
		// reverse peer probe to ensure that host names appear in peer status on both sides
		result = sshUtil.executeRemote(newServer, "gluster peer probe " + existingServer);
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't _reverse_ probe server [" + existingServer + "] from ["
					+ newServer + "]. Error: " + result);
		}
	}

	public void startVolume(String volumeName, String knownServer) {
		ProcessResult result = sshUtil.executeRemote(knownServer, "gluster volume start " + volumeName);
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't start volume [" + volumeName + "]! Error: " + result.toString());
		}
	}

	public void stopVolume(String volumeName, String knownServer) {
		ProcessResult result = sshUtil.executeRemote(knownServer, "gluster --mode=script volume stop " + volumeName);
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't stop volume [" + volumeName + "]! Error: " + result.toString());
		}
	}

	public void resetOptions(String volumeName, String knownServer) {
		ProcessResult result = sshUtil.executeRemote(knownServer, "gluster volume reset " + volumeName);
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't reset options for volume [" + volumeName + "]! Error: "
					+ result);
		}
	}

	public void createVolume(String knownServer, String volumeName, String volumeTypeStr, String transportTypeStr,
			Integer replicaCount, Integer stripeCount, String bricks, String accessProtocols, String options) {
		
		int count = 1; // replica or stripe count

		VOLUME_TYPE volType = Volume.getVolumeTypeByStr(volumeTypeStr);
		String volTypeArg = null;
		if (volType == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			volTypeArg = "replica";
			count = replicaCount;
		} else if (volType == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			volTypeArg = "stripe";
			count = stripeCount;
		}

		String transportTypeArg = null;
		TRANSPORT_TYPE transportType = Volume.getTransportTypeByStr(transportTypeStr);
		transportTypeArg = (transportType == TRANSPORT_TYPE.ETHERNET) ? "tcp" : "rdma";

		String command = prepareVolumeCreateCommand(volumeName, StringUtil.extractList(bricks, ","), count,
				volTypeArg, transportTypeArg);

		ProcessResult result = sshUtil.executeRemote(knownServer, command);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Error in creating volume [" + volumeName + "]: " + result);
		}

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

	public void setOption(String volumeName, String key, String value, String knownServer) {
		ProcessResult result = sshUtil.executeRemote(knownServer, "gluster volume set " + volumeName + " " + key + " "
				+ "\"" + value + "\"");
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Volume [" + volumeName + "] set [" + key + "=" + value + "] => "
					+ result);
		}
	}

	public void deleteVolume(String volumeName, String knownServer) {
		ProcessResult result = sshUtil.executeRemote(knownServer, "gluster --mode=script volume delete " + volumeName);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't delete volume [" + volumeName + "]! Error: " + result);
		}
	}

	private String getVolumeInfo(String volumeName, String knownServer) {
		ProcessResult result = sshUtil.executeRemote(knownServer, "gluster volume info " + volumeName);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Command [gluster volume info " + volumeName + "] failed on ["
					+ knownServer + "] with error: " + result);
		}
		return result.getOutput();
	}

	private String getVolumeInfo(String knownServer) {
		ProcessResult result = sshUtil.executeRemote(knownServer, "gluster volume info ");
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Command [gluster volume info] failed on [" + knownServer
					+ "] with error: " + result);
		}
		return result.getOutput();
	}

	private boolean readVolumeType(Volume volume, String line) {
		String volumeType = extractToken(line, VOLUME_TYPE_PFX);
		if (volumeType != null) {
			if (volumeType.equals(VOLUME_TYPE_DISTRIBUTE)) {
				volume.setVolumeType(VOLUME_TYPE.PLAIN_DISTRIBUTE);
			} else if (volumeType.equals(VOLUME_TYPE_REPLICATE)) {
				volume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_MIRROR);
				volume.setReplicaCount(Volume.DEFAULT_REPLICA_COUNT);
			} else {
				volume.setVolumeType(VOLUME_TYPE.DISTRIBUTED_STRIPE);
				volume.setStripeCount(Volume.DEFAULT_STRIPE_COUNT);
			}
			return true;
		}
		return false;
	}

	private void readReplicaOrStripeCount(Volume volume, String line) {
		if (extractToken(line, "x") != null) {
			// expected formated of line is "Number of Bricks: 3 x 2 = 6"
			int count = Integer.parseInt(line.split("x")[1].split("=")[0].trim());
			if (volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
				volume.setStripeCount(count);
			} else if (volume.getVolumeType() == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
				volume.setReplicaCount(count);
				volume.setStripeCount(0);
			}

		}
		return;
	}

	private boolean readVolumeStatus(Volume volume, String line) {
		String volumeStatus = extractToken(line, VOLUME_STATUS_PFX);
		if (volumeStatus != null) {
			volume.setStatus(volumeStatus.equals("Started") ? VOLUME_STATUS.ONLINE : VOLUME_STATUS.OFFLINE);
			return true;
		}
		return false;
	}

	private boolean readTransportType(Volume volume, String line) {
		String transportType = extractToken(line, VOLUME_TRANSPORT_TYPE_PFX);
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
			String output = serverUtil.executeScriptOnServer(true, serverName, BRICK_STATUS_SCRIPT + " " + volumeName
					+ " " + brick, String.class);
			if (output.equals(CoreConstants.ONLINE)) {
				return BRICK_STATUS.ONLINE;
			} else {
				return BRICK_STATUS.OFFLINE;
			}
		} catch(Exception e) { // Particularly interested on ConnectionExecption, if the server is offline
			return BRICK_STATUS.OFFLINE;
		}
	}

	private boolean readBrickGroup(String line) {
		return extractToken(line, VOLUME_BRICKS_GROUP_PFX) != null;
	}

	private boolean readOptionReconfigGroup(String line) {
		return extractToken(line, VOLUME_OPTIONS_RECONFIG_PFX) != null;
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

	public Volume getVolume(String volumeName, String knownServer) {
		return parseVolumeInfo(getVolumeInfo(volumeName, knownServer)).get(0);
	}

	public List<Volume> getAllVolumes(String knownServer) {
		return parseVolumeInfo(getVolumeInfo(knownServer));
	}

	private List<Volume> parseVolumeInfo(String volumeInfoText) {
		List<Volume> volumes = new ArrayList<Volume>();
		boolean isBricksGroupFound = false;
		boolean isOptionReconfigFound = false;
		Volume volume = null;

		for (String line : volumeInfoText.split(CoreConstants.NEWLINE)) {
			String volumeName = extractToken(line, VOLUME_NAME_PFX);
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
			if (extractToken(line, VOLUME_NUMBER_OF_BRICKS) != null) {
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
		
		updateCifsInfo(volumes);

		return volumes;
	}

	private void updateCifsInfo(List<Volume> volumes) {
		for (Volume volume : volumes) {
			boolean isCifsEnabled = false;
			
			// TODO: Call python script to check if CIFS is enabled on the volume
			
			List<String> cifsUsers = new ArrayList<String>();
			if (isCifsEnabled) {
				volume.enableCifs();
				volume.setCifsUsers(cifsUsers);
			} else {
				volume.disableCifs();
			}
		}
	}

	public void addBricks(String volumeName, List<String> bricks, String knownServer) {
		StringBuilder command = new StringBuilder("gluster volume add-brick " + volumeName);
		for (String brickDir : bricks) {
			command.append(" " + brickDir);
		}
		
		ProcessResult result = sshUtil.executeRemote(knownServer, command.toString());
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Error in volume [" + volumeName + "] add-brick [" + bricks + "]: "
					+ result);
		}
	}

	public String getLogLocation(String volumeName, String brickName, String knownServer) {
		String command = "gluster volume log locate " + volumeName + " " + brickName;
		ProcessResult result = sshUtil.executeRemote(knownServer, command);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Command [" + command + "] failed with error: [" + result.getExitValue()
					+ "][" + result.getOutput() + "]");
		}
		String output = result.getOutput();
		if (output.startsWith(VOLUME_LOG_LOCATION_PFX)) {
			return output.substring(VOLUME_LOG_LOCATION_PFX.length()).trim();
		}

		throw new GlusterRuntimeException("Couldn't parse output of command [" + command + "]. Output [" + output
				+ "] doesn't start with prefix [" + VOLUME_LOG_LOCATION_PFX + "]");
	}

	//TODO File.separator should be changed if gateway runs on windows/mac
	public String getLogFileNameForBrickDir(String brickDir) {
		String logFileName = brickDir;
		if (logFileName.startsWith(File.separator)) {
			logFileName = logFileName.replaceFirst(File.separator, "");
		}
		logFileName = logFileName.replaceAll(File.separator, "-") + ".log";
		return logFileName;
	}

	public void removeBricks(String volumeName, List<String> bricks, String knownServer) {
		StringBuilder command = new StringBuilder("gluster --mode=script volume remove-brick " + volumeName);
		for (String brickDir : bricks) {
			command.append(" " + brickDir);
		}
		ProcessResult result = sshUtil.executeRemote(knownServer, command.toString());
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException(result.toString());
		}
	}

	public void removeServer(String existingServer, String serverName) {
		ProcessResult result = sshUtil.executeRemote(existingServer, "gluster --mode=script peer detach " + serverName);
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Couldn't remove server [" + serverName + "]! Error: " + result);
		}
	}
	
	public TaskStatus checkRebalanceStatus(String serverName, String volumeName) {
		String command = "gluster volume rebalance " + volumeName + " status";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			if (processResult.getOutput().trim().matches("^rebalance completed.*")) {
				taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
			} else if(processResult.getOutput().trim().matches(".*in progress.*")) {
				taskStatus.setCode(Status.STATUS_CODE_RUNNING);
			} else {
				taskStatus.setCode(Status.STATUS_CODE_FAILURE);
			}
		} else {
			taskStatus.setCode(Status.STATUS_CODE_FAILURE);
		}
		taskStatus.setMessage(processResult.getOutput()); // Common
		return taskStatus;
	}
	
	public void stopRebalance(String serverName, String volumeName) {
		String command = "gluster volume rebalance " + volumeName + " stop";
		ProcessResult processResult = sshUtil.executeRemote(serverName, command);
		TaskStatus taskStatus = new TaskStatus();
		if (processResult.isSuccess()) {
			taskStatus.setCode(Status.STATUS_CODE_SUCCESS);
			taskStatus.setMessage(processResult.getOutput());
		}
	}
	
	public TaskStatus getInitializingDeviceStatus(String serverName, String diskName) {
		InitDiskStatusResponse initDiskStatusResponse;
		TaskStatus taskStatus = new TaskStatus();
		
		try {
			initDiskStatusResponse = serverUtil.executeScriptOnServer(true, serverName, INITIALIZE_DISK_STATUS_SCRIPT + " "
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
	
	public ProcessResult executeBrickMigration(String onlineServerName, String volumeName, String fromBrick,
			String toBrick, String operation) {
		String command = "gluster volume replace-brick " + volumeName + " " + fromBrick + " " + toBrick + " " + operation;
		ProcessResult processResult = sshUtil.executeRemote(onlineServerName, command);
		if (!processResult.isSuccess()) {
			throw new GlusterRuntimeException(processResult.toString());
		}
		return processResult;
	}

	public static void main(String args[]) {
		// List<String> names = new GlusterUtil().getGlusterServerNames();
		// System.out.println(names);
		List<String> disks = new ArrayList<String>();
		disks.add("server1:sda");
		disks.add("server1:sdb");
		new GlusterUtil().addBricks("Volume3", disks, "localhost");
	}
}
