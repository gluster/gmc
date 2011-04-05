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

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.GlusterUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.server.constants.VolumeOptionsDefaults;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path(RESOURCE_PATH_VOLUMES)
public class VolumesResource {
	
	private final GlusterUtil glusterUtil = new GlusterUtil();
	
	@InjectParam
	private VolumeOptionsDefaults volumeOptionsDefaults;

	@GET 
	@Produces(MediaType.TEXT_XML)
	public VolumeListResponse getAllVolumes() {
		try {
			return new VolumeListResponse(Status.STATUS_SUCCESS, glusterUtil.getAllVolumes());
		} catch(Exception e) {
			// TODO: log the error
			return new VolumeListResponse(new Status(Status.STATUS_CODE_FAILURE, e.getMessage()), null);
		}
	}
	
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public GenericResponse<String> createVolume(Volume volume) {
		ProcessResult response = glusterUtil.createVolume(volume);
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
		// TODO: Fetch all volume options with their default values from GlusterFS
		// whenever such a CLI command is made available in GlusterFS
		return new VolumeOptionInfoListResponse(Status.STATUS_SUCCESS, volumeOptionsDefaults.getDefaults());
	}
	
	
	public static void main(String[] args) {
		VolumesResource vr = new VolumesResource();
		VolumeListResponse response = vr.getAllVolumes(); 
		for(Volume volume : response.getVolumes() ) {
			System.out.println( "\nName:" + volume.getName() + "\nType: " + volume.getVolumeTypeStr() + "\nStatus: " + volume.getStatusStr());
		}
	}
}