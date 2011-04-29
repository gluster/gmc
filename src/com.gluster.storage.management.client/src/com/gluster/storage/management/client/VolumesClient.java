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

import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.response.LogMessageListResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.StringUtil;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class VolumesClient extends AbstractClient {

	public VolumesClient(String securityToken) {
		super(securityToken);
	}

	@Override
	public String getResourceName() {
		return RESTConstants.RESOURCE_PATH_VOLUMES;
	}

	public Status createVolume(Volume volume) {
		return (Status) postObject(Status.class, volume);
	}

	private Status performOperation(String volumeName, String operation) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPERATION, operation);

		return (Status) putRequest(volumeName, Status.class, form);
	}

	public Status startVolume(String volumeName) {
		return performOperation(volumeName, RESTConstants.FORM_PARAM_VALUE_START);
	}

	public Status stopVolume(String volumeName) {
		return performOperation(volumeName, RESTConstants.FORM_PARAM_VALUE_STOP);
	}

	public Status setVolumeOption(String volume, String key, String value) {
		Form form = new Form();
		form.add(RESTConstants.FORM_PARAM_OPTION_KEY, key);
		form.add(RESTConstants.FORM_PARAM_OPTION_VALUE, value);
		return (Status) postRequest(volume + "/" + RESTConstants.SUBRESOURCE_OPTIONS, Status.class, form);
	}

	public Status resetVolumeOptions(String volume) {
		return (Status) putRequest(volume + "/" + RESTConstants.SUBRESOURCE_OPTIONS, Status.class);
	}

	public VolumeListResponse getAllVolumes() {
		return (VolumeListResponse) fetchResource(VolumeListResponse.class);
	}

	public Volume getVolume(String volumeName) {
		return (Volume) fetchSubResource(volumeName, Volume.class);
	}

	public Status deleteVolume(Volume volume, String deleteOption) {
		return (Status) deleteSubResource(volume.getName(), Status.class, volume.getName(), deleteOption);
	}

	public VolumeOptionInfoListResponse getVolumeOptionsDefaults() {
		return ((VolumeOptionInfoListResponse) fetchSubResource(RESTConstants.SUBRESOURCE_DEFAULT_OPTIONS,
				VolumeOptionInfoListResponse.class));
	}
	
	public Status addDisks(String volumeName, List<Disk> diskList) {
		String disks = StringUtil.ListToString( GlusterCoreUtil.getQualifiedDiskNames(diskList), ",");
		Form form = new Form();
		form.add(RESTConstants.QUERY_PARAM_DISKS, disks);
		return (Status) postRequest(volumeName + "/" + RESTConstants.SUBRESOURCE_DISKS, Status.class, form);
	}

	public Status addDisks(String volumeName, String disks) {
		Form form = new Form();
		form.add(RESTConstants.QUERY_PARAM_DISKS, disks);
		return (Status) postRequest(volumeName + "/" + RESTConstants.SUBRESOURCE_DISKS, Status.class, form);
	}

	public LogMessageListResponse getLogs(String volumeName, int lineCount) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add(RESTConstants.QUERY_PARAM_LINE_COUNT, "" + lineCount);
		// TODO: Add other filte criteria as query parameters
		return (LogMessageListResponse) fetchSubResource(volumeName + "/" + RESTConstants.SUBRESOURCE_LOGS,
				queryParams, LogMessageListResponse.class);

	}

	public static void main(String[] args) {
		UsersClient usersClient = new UsersClient();
		if (usersClient.authenticate("gluster", "gluster").isSuccess()) {
			VolumesClient client = new VolumesClient(usersClient.getSecurityToken());
//			List<Disk> disks = new ArrayList<Disk>();
//			Disk diskElement = new Disk();
//			diskElement.setName("sda1");
//			diskElement.setStatus(DISK_STATUS.READY);
//			disks.add(diskElement);
//			diskElement.setName("sda2");
//			diskElement.setStatus(DISK_STATUS.READY);
//			disks.add(diskElement);
//
//			Volume vol = new Volume("vol1", null, Volume.VOLUME_TYPE.PLAIN_DISTRIBUTE, Volume.TRANSPORT_TYPE.ETHERNET,
//					Volume.VOLUME_STATUS.ONLINE);
//			// vol.setDisks(disks);
//			System.out.println(client.createVolume(vol));
//			for (VolumeOptionInfo option : client.getVolumeOptionsDefaults()) {
//				System.out.println(option.getName() + "-" + option.getDescription() + "-" + option.getDefaultValue());
//			}
//			System.out.println(client.getVolume("Volume3").getOptions());
//			System.out.println(client.setVolumeOption("Volume3", "network.frame-timeout", "600").getMessage());
			List<Disk> disks = new ArrayList<Disk>(); 
			Disk disk = new Disk();
			disk.setServerName("server1");
			disk.setName("sda");
			disk.setStatus(DISK_STATUS.READY);
			disks.add(disk);
			
			Status status = client.addDisks("Volume3", disks);
			System.out.println(status.getMessage());
		}
	}
}
