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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.GenericResponse;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;

@Path("/cluster/volumes")
public class VolumesResource {

	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public GenericResponse<String> createVolume(Volume volume) {
		
		int count=1; // replica or stripe count
		String volumeType = null;
		VOLUME_TYPE volType = volume.getVolumeType(); 
		if(volType == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			volumeType = "replica";
			count = 2;
		} else if(volType == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
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
		if(volumeType != null) {
			command.add(volumeType);
			command.add("" + count);
		}
		command.add("transport");
		command.add(transportTypeStr);
		
		for(Disk disk : volume.getDisks()) {
			command.add(getBrickNotation(volume, disk));
		}
		
		ProcessResult result = new ProcessUtil().executeCommand(command);
		
		if (!result.isSuccess()) {
			return new GenericResponse<String>(Status.STATUS_FAILURE, "Volume creation failed: [" + result.getOutput() + "]");
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
}
