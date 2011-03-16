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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.GenericResponse;
import com.gluster.storage.management.core.model.Volume;

public class VolumesClient extends AbstractClient {
	private static final String RESOURCE_NAME = "cluster/volumes";

	public VolumesClient(String serverName, String securityToken) {
		super(serverName, securityToken);
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	public String createVolume(Volume volume) {

		GenericResponse<String> response = (GenericResponse<String>) resource.path("createvolume")
				.type(MediaType.APPLICATION_XML).accept(MediaType.APPLICATION_XML).post(GenericResponse.class, volume);

		System.out.println("Response : " + response.getData());

		return response.getData();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient("localhost");
		if (usersClient.authenticate("gluster", "gluster")) {
			VolumesClient VC = new VolumesClient("localhost", usersClient.getSecurityToken());
			List<Disk> disks = new ArrayList<Disk>();
			Disk diskElement = new Disk();
			diskElement.setName("sda1");
			diskElement.setStatus(DISK_STATUS.READY);
			disks.add(diskElement);
			diskElement.setName("sda2");
			diskElement.setStatus(DISK_STATUS.READY);
			disks.add(diskElement);

			Volume vol = new Volume("vol1", null, Volume.VOLUME_TYPE.PLAIN_DISTRIBUTE, Volume.TRANSPORT_TYPE.ETHERNET,
					Volume.VOLUME_STATUS.ONLINE);
			vol.setDisks(disks);
			System.out.println(VC.createVolume(vol));
		}
	}
}
