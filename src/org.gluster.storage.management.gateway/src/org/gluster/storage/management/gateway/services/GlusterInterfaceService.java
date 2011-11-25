/**
 * GlusterInterfaceService.java
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
package org.gluster.storage.management.gateway.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gluster.storage.management.core.model.TaskStatus;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import org.springframework.stereotype.Component;


@Component
public class GlusterInterfaceService extends AbstractGlusterInterface {
	private HashMap<String, GlusterInterface> glusterInterfaces = new HashMap<String, GlusterInterface>();
	
	/**
	 * Returns an instance of the Gluster Interface for given version of GlusterFS
	 * @param glusterFsVersion
	 * @return
	 */
	private GlusterInterface getGlusterInterfaceForVersion(String glusterFsVersion) {
		GlusterInterface glusterInterface = glusterInterfaces.get(glusterFsVersion);
		if(glusterInterface != null) {
			return glusterInterface;
		}
		
		glusterInterface = serverUtil.getBean(Gluster323InterfaceService.class);
		glusterInterfaces.put(glusterFsVersion, glusterInterface);
		return glusterInterface;
	}
	
	/**
	 * Returns an instance of Gluster Interface for the version of GlusterFS installed on given server.
	 * 
	 * @param serverName
	 * @return
	 */
	private GlusterInterface getGlusterInterface(String serverName) {
		return getGlusterInterfaceForVersion(getVersion(serverName));
	}
	
	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#addServer(java.lang.String, java.lang.String)
	 */
	@Override
	public void addServer(String existingServer, String newServer) {
		getGlusterInterface(existingServer).addServer(existingServer, newServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#startVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public void startVolume(String volumeName, String knownServer, Boolean force) {
		getGlusterInterface(knownServer).startVolume(volumeName, knownServer, force);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#stopVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public void stopVolume(String volumeName, String knownServer, Boolean force) {
		getGlusterInterface(knownServer).stopVolume(volumeName, knownServer, force);
	}
	
	public void logRotate(String volumeName, List<String> brickList, String knownServer) {
		getGlusterInterface(knownServer).logRotate(volumeName, brickList, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#resetOptions(java.lang.String, java.lang.String)
	 */
	@Override
	public void resetOptions(String volumeName, String knownServer) {
		getGlusterInterface(knownServer).resetOptions(volumeName, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#createVolume(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void createVolume(String knownServer, String volumeName, String volumeTypeStr, String transportTypeStr,
			Integer count, String bricks, String accessProtocols, String options) {
		getGlusterInterface(knownServer).createVolume(knownServer, volumeName, volumeTypeStr, transportTypeStr, count,
				bricks, accessProtocols, options);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#createOptions(java.lang.String, java.util.Map, java.lang.String)
	 */
	@Override
	public void createOptions(String volumeName, Map<String, String> options, String knownServer) {
		getGlusterInterface(knownServer).createOptions(volumeName, options, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#setOption(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setOption(String volumeName, String key, String value, String knownServer) {
		getGlusterInterface(knownServer).setOption(volumeName, key, value, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#deleteVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteVolume(String volumeName, String knownServer) {
		getGlusterInterface(knownServer).deleteVolume(volumeName, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#getVolume(java.lang.String, java.lang.String)
	 */
	@Override
	public Volume getVolume(String volumeName, String knownServer) {
		return getGlusterInterface(knownServer).getVolume(volumeName, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#getAllVolumes(java.lang.String)
	 */
	@Override
	public List<Volume> getAllVolumes(String knownServer) {
		return getGlusterInterface(knownServer).getAllVolumes(knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#addBricks(java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void addBricks(String volumeName, List<String> bricks, String knownServer) {
		getGlusterInterface(knownServer).addBricks(volumeName, bricks, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#getLogLocation(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getLogLocation(String volumeName, String brickName, String knownServer) {
		return getGlusterInterface(knownServer).getLogLocation(volumeName, brickName, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#getLogFileNameForBrickDir(java.lang.String)
	 */
	@Override
	public String getLogFileNameForBrickDir(String serverName, String brickDir) {
		return getGlusterInterface(serverName).getLogFileNameForBrickDir(serverName, brickDir);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#removeBricks(java.lang.String, java.util.List, java.lang.String)
	 */
	@Override
	public void removeBricks(String volumeName, List<String> bricks, String knownServer) {
		getGlusterInterface(knownServer).removeBricks(volumeName, bricks, knownServer);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#removeServer(java.lang.String, java.lang.String)
	 */
	@Override
	public void removeServer(String existingServer, String serverName) {
		getGlusterInterface(serverName).removeServer(existingServer, serverName);
	}
	
	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#checkRebalanceStatus(java.lang.String, java.lang.String)
	 */
	@Override
	public TaskStatus checkRebalanceStatus(String serverName, String volumeName) {
		return getGlusterInterface(serverName).checkRebalanceStatus(serverName, volumeName);
	}
	
	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#stopRebalance(java.lang.String, java.lang.String)
	 */
	@Override
	public void stopRebalance(String serverName, String volumeName) {
		getGlusterInterface(serverName).stopRebalance(serverName, volumeName);
	}
	
	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#executeBrickMigration(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void startBrickMigration(String onlineServerName, String volumeName, String fromBrick, String toBrick) {
		getGlusterInterface(onlineServerName).startBrickMigration(onlineServerName, volumeName, fromBrick, toBrick);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.services.GlusterInterface#pauseBrickMigration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void pauseBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick) {
		getGlusterInterface(serverName).pauseBrickMigration(serverName, volumeName, fromBrick, toBrick);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.services.GlusterInterface#stopBrickMigration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void stopBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick) {
		getGlusterInterface(serverName).stopBrickMigration(serverName, volumeName, fromBrick, toBrick);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.services.GlusterInterface#commitBrickMigration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void commitBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick) {
		getGlusterInterface(serverName).commitBrickMigration(serverName, volumeName, fromBrick, toBrick);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.services.GlusterInterface#checkBrickMigrationStatus(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public TaskStatus checkBrickMigrationStatus(String serverName, String volumeName, String fromBrick, String toBrick) {
		return getGlusterInterface(serverName).checkBrickMigrationStatus(serverName, volumeName, fromBrick, toBrick);
	}

	/* (non-Javadoc)
	 * @see org.gluster.storage.management.gateway.utils.GlusterInterface#getVolumeOptionsInfo(java.lang.String)
	 */
	@Override
	public VolumeOptionInfoListResponse getVolumeOptionsInfo(String serverName) {
		return getGlusterInterface(serverName).getVolumeOptionsInfo(serverName);
	}
}
