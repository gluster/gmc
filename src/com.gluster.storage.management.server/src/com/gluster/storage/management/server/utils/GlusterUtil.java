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
package com.gluster.storage.management.server.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;

public class GlusterUtil {
	private static final String glusterFSminVersion = "3.1";

	private static final String HOSTNAME_PFX = "Hostname:";
	private static final String UUID_PFX = "Uuid:";
	private static final String STATE_PFX = "State:";
	private static final String GLUSTER_SERVER_STATUS_ONLINE = "Connected";

	private static final String VOLUME_NAME_PFX = "Volume Name:";
	private static final String VOLUME_TYPE_PFX = "Type:";
	private static final String VOLUME_STATUS_PFX = "Status:";
	private static final String VOLUME_TRANSPORT_TYPE_PFX = "Transport-type:";
	private static final String VOLUME_BRICKS_GROUP_PFX = "Bricks";
	private static final String VOLUME_OPTIONS_RECONFIG_PFX = "Options Reconfigured";
	private static final String VOLUME_OPTION_AUTH_ALLOW = "auth.allow:";

	private static final ProcessUtil processUtil = new ProcessUtil();

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

	public List<GlusterServer> getGlusterServers() {
		String output = getPeerStatus();
		if (output == null) {
			return null;
		}

		List<GlusterServer> glusterServers = new ArrayList<GlusterServer>();
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

	public List<String> getGlusterServerNames() {
		String output = getPeerStatus();
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

	private String getPeerStatus() {
		String output;
		ProcessResult result = processUtil.executeCommand("gluster", "peer", "status");
		if (!result.isSuccess()) {
			output = null;
		}
		output = result.getOutput();
		return output;
	}

	public Status addServer(String serverName) {
		return new Status(processUtil.executeCommand("gluster", "peer", "probe", serverName));
	}

	public Status startVolume(String volumeName) {
		return new Status(processUtil.executeCommand("gluster", "volume", "start", volumeName));
	}

	public Status stopVolume(String volumeName) {
		return new Status(processUtil.executeCommand("gluster", "--mode=script", "volume", "stop", volumeName));
	}

	public Status resetOptions(String volumeName) {
		return new Status(processUtil.executeCommand("gluster", "volume", "reset", volumeName));
	}

	public Status createVolume(Volume volume, List<String> bricks) {
		int count = 1; // replica or stripe count
		String volumeType = null;
		VOLUME_TYPE volType = volume.getVolumeType();
		if (volType == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			volumeType = "replica";
			count = 2;
		} else if (volType == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			volumeType = "stripe";
			count = 4;
		}

		String transportTypeStr = null;
		TRANSPORT_TYPE transportType = volume.getTransportType();
		transportTypeStr = (transportType == TRANSPORT_TYPE.ETHERNET) ? "tcp" : "rdma";

		List<String> command = prepareVolumeCreateCommand(volume, bricks, count, volumeType, transportTypeStr);
		ProcessResult result = processUtil.executeCommand(command);
		if(!result.isSuccess()) {
			// TODO: Perform cleanup on all nodes before returning
			return new Status(result);
		}
		
		return createOptions(volume);
	}

	private List<String> prepareVolumeCreateCommand(Volume volume, List<String> bricks, int count, String volumeType,
			String transportTypeStr) {
		List<String> command = new ArrayList<String>();
		command.add("gluster");
		command.add("volume");
		command.add("create");
		command.add(volume.getName());
		if (volumeType != null) {
			command.add(volumeType);
			command.add("" + count);
		}
		command.add("transport");
		command.add(transportTypeStr);
		command.addAll(bricks);
		return command;
	}

	public Status createOptions(Volume volume) {
		Map<String, String> options = volume.getOptions();
		if (options != null) {
			for (Entry<String, String> option : options.entrySet()) {
				String key = option.getKey();
				String value = option.getValue();
				Status status = setOption(volume.getName(), key, value);
				if (!status.isSuccess()) {
					return status;
				}
			}
		}
		return Status.STATUS_SUCCESS;
	}

	public Status setOption(String volumeName, String key, String value) {
		List<String> command = new ArrayList<String>();
		command.add("gluster");
		command.add("volume");
		command.add("set");
		command.add(volumeName);
		command.add(key);
		command.add(value);

		return new Status(processUtil.executeCommand(command));
	}

	private String getVolumeInfo(String volumeName) {
		ProcessResult result = new ProcessUtil().executeCommand("gluster", "volume", "info", volumeName);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Command [gluster volume info] failed with error: ["
					+ result.getExitValue() + "][" + result.getOutput() + "]");
		}
		return result.getOutput();
	}

	private String getVolumeInfo() {
		ProcessResult result = new ProcessUtil().executeCommand("gluster", "volume", "info");
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Command [gluster volume info] failed with error: ["
					+ result.getExitValue() + "][" + result.getOutput() + "]");
		}
		return result.getOutput();
	}
	
	private boolean readVolumeType(Volume volume, String line) {
		String volumeType = extractToken(line, VOLUME_TYPE_PFX);
		if (volumeType != null) {
			volume.setVolumeType((volumeType.equals("Distribute")) ? VOLUME_TYPE.PLAIN_DISTRIBUTE
					: VOLUME_TYPE.DISTRIBUTED_MIRROR); // TODO: for Stripe
			return true;
		}
		return false;
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
			volume.setTransportType(transportType.equals("tcp") ? TRANSPORT_TYPE.ETHERNET
					: TRANSPORT_TYPE.INFINIBAND);
			return true;
		}
		return false;
	}
	
	private boolean readBrick(Volume volume, String line) {
		if (line.matches("Brick[0-9]+:.*")) {
			// line: "Brick1: server1:/export/md0/volume-name"
			String[] brickParts = line.split(":");
			String serverName = brickParts[1].trim();
			String brickDir = brickParts[2].trim();
			// brick directory should be of the form /export/<diskname>/volume-name
			try {
				volume.addDisk(serverName + ":" + brickDir.split("/")[2].trim());
			} catch(ArrayIndexOutOfBoundsException e) {
				// brick directory of a different form, most probably created manually
				// connect to the server and get disk for the brick directory
				Status status = new ServerUtil().getDiskForDir(serverName, brickDir);
				if(status.isSuccess()) {
					volume.addDisk(serverName + ":" + status.getMessage());
				} else {
					// Couldn't fetch disk for the brick directory. Log error and add "unknown" as disk name.
					System.out.println("Couldn't fetch disk name for brick [" + serverName + ":" + brickDir + "]");
					volume.addDisk(serverName + ":unknown");
				}
			}
			return true;
		}
		return false;
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
			return true;
		}
		return false;
	}

	public Volume getVolume(String volumeName) {
		List<Volume> volumes = parseVolumeInfo(getVolumeInfo(volumeName));
		if(volumes.size() > 0) {
			return volumes.get(0);
		}
		return null;
	}

	public List<Volume> getAllVolumes() {
		return parseVolumeInfo(getVolumeInfo());
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
			if (readVolumeStatus(volume, line))
				continue;
			if(readTransportType(volume, line))
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
				if(readOption(volume, line)) {
					continue;
				} else {
					isOptionReconfigFound = false;
				}
			}
		}

		if (volume != null) {// Adding the last volume parsed
			volumes.add(volume);
		}
		return volumes;
	}

	public static void main(String args[]) {
		// List<String> names = new GlusterUtil().getGlusterServerNames();
		// System.out.println(names);
	}
}
