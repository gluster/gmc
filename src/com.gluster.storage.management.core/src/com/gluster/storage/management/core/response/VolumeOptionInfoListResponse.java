/*******************************************************************************
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
 *******************************************************************************/
package com.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.VolumeOptionInfo;

@XmlRootElement(name = "options")
public class VolumeOptionInfoListResponse {
	private List<VolumeOptionInfo> options = new ArrayList<VolumeOptionInfo>();

	public VolumeOptionInfoListResponse() {
	}

	public VolumeOptionInfoListResponse(Status status, List<VolumeOptionInfo> options) {
		setOptions(options);
	}

	@XmlElement(name = "option", type=VolumeOptionInfo.class)
	public List<VolumeOptionInfo> getOptions() {
		return options;
	}

	public void setOptions(List<VolumeOptionInfo> options) {
		this.options = options;
	}
}
