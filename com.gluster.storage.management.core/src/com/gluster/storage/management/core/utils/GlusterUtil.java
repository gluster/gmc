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
package com.gluster.storage.management.core.utils;

import java.util.ArrayList;
import java.util.List;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.GlusterServer.SERVER_STATUS;

/**
 *
 */
public class GlusterUtil {
	private static final String HOSTNAME_PFX = "Hostname:";
	private static final String UUID_PFX = "Uuid:";
	private static final String STATE_PFX = "State:";
	private static final String GLUSTER_SERVER_STATUS_ONLINE = "Connected";
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
					// Completed populating current server. Add it to the list and reset all related variables.
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
		if(output == null) {
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

	public ProcessResult addServer(String serverName) {
		return processUtil.executeCommand("gluster", "peer", "probe", serverName);
	}
	
	public ProcessResult startVolume(String volumeName) {
		return processUtil.executeCommand("gluster", "volume", "start", volumeName);
	}

	public ProcessResult stopVolume(String volumeName) {
		return processUtil.executeCommand("gluster", "--mode=script", "volume", "stop", volumeName);
	}

	public static void main(String args[]) {
		List<String> names = new GlusterUtil().getGlusterServerNames();
		System.out.println(names);
	}
}
