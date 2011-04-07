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

import java.util.ArrayList;
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

import org.springframework.beans.factory.annotation.Autowired;

import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.GlusterUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.server.constants.VolumeOptionsDefaults;
import com.gluster.storage.management.server.utils.ServerUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path(RESOURCE_PATH_VOLUMES)
public class VolumesResource {
	private static final String SCRIPT_NAME = "CreateVolumeExportDirectory.py";
	
	@Autowired
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
	public GenericResponse<String> createVolume(Volume volume) {
		//Create the directories for the volume
		List<String> bricks = new ArrayList<String>();
		for(String disk : volume.getDisks()) {
			
			String brickNotation = getBrickNotation(volume, disk);
			if (brickNotation != null) {
				bricks.add(brickNotation);
			} else {
				return new GenericResponse<String>(Status.STATUS_FAILURE, "Disk is not mounted properly. Pls mount the disk.");
			}
		}
		
		ProcessResult response = glusterUtil.createVolume(volume, bricks);
		if (!response.isSuccess()) {
			return new GenericResponse<String>(Status.STATUS_FAILURE, "Volume creation failed: ["
					+ response.getOutput() + "]");
		}

		response = glusterUtil.setVolumeAccessControl(volume);

		return new GenericResponse<String>(Status.STATUS_SUCCESS, response.getOutput());
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	@Produces(MediaType.TEXT_XML)
	public Status performOperation(@FormParam(FORM_PARAM_OPERATION) String operation,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {

		if (operation.equals(FORM_PARAM_VALUE_START)) {
			return new Status(glusterUtil.startVolume(volumeName));
		}
		if (operation.equals(FORM_PARAM_VALUE_STOP)) {
			return new Status(glusterUtil.stopVolume(volumeName));
		}
		return new Status(Status.STATUS_CODE_FAILURE, "Invalid operation code [" + operation + "]");
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
	
	private String getBrickNotation(Volume vol, String disk) {
		String serverName = disk.split(":")[0];
		String exportDirectory = disk.split(":")[1];
		Status result  =  serverUtil.executeOnServer(true, serverName, "python " + SCRIPT_NAME +" " + exportDirectory + " " + vol.getName());
		
		if(result.getCode() == 0) {	
			String dirName = "/export/" + disk + "/" + vol.getName() ;
			return serverName + ":" + dirName;
		} else {
			return null;
		}
		
	}
	
	public static void main(String args[]) {
		// Disk disk = null;
		serverUtil.executeOnServer(true, "localhost", "CreateVolumeExportDirectory.py md0 testvol");
	}
}