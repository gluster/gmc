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
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_START;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VALUE_STOP;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_VOLUME_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_VOLUMES;
import static com.gluster.storage.management.core.constants.RESTConstants.SUBRESOURCE_DEFAULT_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.SUBRESOURCE_OPTIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.GlusterUtil;
import com.gluster.storage.management.server.constants.VolumeOptionsDefaults;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path(RESOURCE_PATH_VOLUMES)
public class VolumesResource {
	private static final String PREPARE_BRICK_SCRIPT = "create_volume_directory.py";
	private static final String VOLUME_DIRECTORY_CLEANUP_SCRIPT = "clear_volume_directory.py";

	@InjectParam
	private static ServerUtil serverUtil;
	private final GlusterUtil glusterUtil = new GlusterUtil();

	@InjectParam
	private VolumeOptionsDefaults volumeOptionsDefaults;

	@GET
	@Produces(MediaType.TEXT_XML)
	public VolumeListResponse getAllVolumes() {
		try {
			return new VolumeListResponse(Status.STATUS_SUCCESS, glusterUtil.getAllVolumes());
		} catch (Exception e) {
			// TODO: log the error
			return new VolumeListResponse(new Status(Status.STATUS_CODE_FAILURE, e.getMessage()), null);
		}
	}

	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	@SuppressWarnings("rawtypes")
	public Status createVolume(Volume volume) {
		// Create the directories for the volume
		List<String> disks = volume.getDisks();
		GenericResponse result = createDirectory(disks, volume.getName());
		if (result.getStatus().isSuccess()) {
			List<String> bricks = Arrays.asList(result.getStatus().getMessage().split(", "));
			result.setStatus(glusterUtil.createVolume(volume, bricks));
			if (result.getStatus().isSuccess()) {
				result.setStatus(glusterUtil.createOptions(volume));
			} else {
				cleanupDirectory(disks, volume.getName(), disks.size());
			}
		}
		return result.getStatus();
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
		// TODO: Fetch all volume options with their default values from
		// GlusterFS
		// whenever such a CLI command is made available in GlusterFS
		return new VolumeOptionInfoListResponse(Status.STATUS_SUCCESS, volumeOptionsDefaults.getDefaults());
	}

	@SuppressWarnings("rawtypes")
	private GenericResponse prepareBrick(String disk, String volumeName) {
		System.out.println("Disk : " + disk);
		String serverName = disk.split(":")[0];
		String diskName = disk.split(":")[1];
		return (GenericResponse) serverUtil.executeOnServer(true, serverName, PREPARE_BRICK_SCRIPT + " " + diskName
				+ " " + volumeName, GenericResponse.class);
	}

	@SuppressWarnings({ "rawtypes" })
	private GenericResponse createDirectory(List<String> disks, String volumeName) {
		List<String> brickNotation = new ArrayList<String>();
		GenericResponse response = new GenericResponse();

		for (int i = 0; i < disks.size(); i++) {
			response = prepareBrick(disks.get(i), volumeName);
			if (response.getStatus().isSuccess()) {
				String brick =  response.getStatus().getMessage().trim().toString().replace("\n", "");
				brickNotation.add(disks.get(i).split(":")[0]+ ":" + brick);
			} else {
				Status status = cleanupDirectory(disks, volumeName, i + 1);
				if (!status.isSuccess()) {
					response.getStatus().setMessage(response.getStatus().getMessage() + "\n" + status.getMessage());
				}
				return response;
			}
		}
		response.getStatus().setMessage(constructBrickNotation(brickNotation));
		return response;
	}

	private String constructBrickNotation(List<String> bricks) {
		String brick = "";
		for (String brickInfo : bricks) {
			brick += brickInfo + " ";
		}
		return brick;
	}
	
	private Status cleanupDirectory(List<String> disks, String volumeName, int maxIndex) {
		String serverName, diskName, diskInfo[];
		Status result;
		for (int i = 0; i < maxIndex; i++) {
			// TODO: Call to delete the volume directory
			diskInfo = disks.get(i).split(":");
			serverName = diskInfo[0];
			diskName = diskInfo[1];
			result = (Status) serverUtil.executeOnServer(true, serverName, VOLUME_DIRECTORY_CLEANUP_SCRIPT + " "
					+ diskName + " " + volumeName, Status.class);
			if (!result.isSuccess()) {
				return result;
			}
		}
		return new Status(Status.STATUS_CODE_SUCCESS, "Directories cleanedup...");
	}

	public static void main(String[] args) {
		VolumesResource vr = new VolumesResource();
//		VolumeListResponse response = vr.getAllVolumes();
//		for (Volume volume : response.getVolumes()) {
//			System.out.println("\nName:" + volume.getName() + "\nType: " + volume.getVolumeTypeStr() + "\nStatus: "
//					+ volume.getStatusStr());
//		}
		Volume volume = new Volume();
		volume.setName("vol3");
		volume.setTransportType(TRANSPORT_TYPE.ETHERNET);
		List<String> disks = new ArrayList<String>();
		disks.add("192.168.1.210:sdb");
		volume.addDisks(disks);
		volume.setAccessControlList("192.168.*");
		Status status = vr.createVolume(volume);
		System.out.println(status.getMessage());
	}
}
