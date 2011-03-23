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
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.GenericResponse;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.GlusterUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.gluster.storage.management.server.constants.VolumeOptionsDefaults;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path(RESOURCE_PATH_VOLUMES)
public class VolumesResource {
	private final GlusterUtil glusterUtil = new GlusterUtil();

	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public GenericResponse<String> createVolume(Volume volume) {

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

		for (Disk disk : volume.getDisks()) {
			command.add(getBrickNotation(volume, disk));
		}

		ProcessResult result = new ProcessUtil().executeCommand(command);

		if (!result.isSuccess()) {
			return new GenericResponse<String>(Status.STATUS_FAILURE, "Volume creation failed: [" + result.getOutput()
					+ "]");
		}
		return new GenericResponse<String>(Status.STATUS_SUCCESS, "Volume created successfully!");
	}

	/**
	 * @param disk
	 * @return
	 */
	private String getBrickNotation(Volume vol, Disk disk) {
		// TODO: Figure out an appropriate directory INSIDE the DISK having given NAME (e.g. sda, sdb, etc)
		String dirName = "/export/" + vol.getName() + "/" + disk.getName();
		return disk.getServerName() + ":" + dirName;
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
	public Map<String, String> getDefaultOptions() {
		// TODO: Fetch all volume options with their default values from GlusterFS
		// whenever such a CLI command is made available in GlusterFS
		return VolumeOptionsDefaults.OPTIONS;
	}
}