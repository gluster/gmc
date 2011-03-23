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

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.GenericResponse;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.sun.jersey.api.representation.Form;

public class VolumesClient extends AbstractClient {
	private static final String RESOURCE_NAME = "/cluster/volumes"; // TODO: move to common place

	public VolumesClient(String securityToken) {
		super(securityToken);
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@SuppressWarnings("unchecked")
	public Status createVolume(Volume volume) {
		GenericResponse<String> response = (GenericResponse<String>) postObject(GenericResponse.class, volume);
		return response.getStatus();
	}
	
	private Status performOperation(String volumeName, String operation) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, operation);
		
		return (Status)putRequest(volumeName, Status.class, form);
	}
	
	public Status startVolume(String volumeName) {
		return performOperation(volumeName, RESTConstants.FORM_PARAM_VALUE_START);
	}

	public Status stopVolume(String volumeName) {
		return performOperation(volumeName, RESTConstants.FORM_PARAM_VALUE_STOP);
	}

	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		if (usersClient.authenticate("gluster", "gluster")) {
			VolumesClient VC = new VolumesClient(usersClient.getSecurityToken());
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
			// vol.setDisks(disks);
			System.out.println(VC.createVolume(vol));
		}
	}
}
