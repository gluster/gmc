/**
 * FsTypesListResponse.java
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
package com.gluster.storage.management.core.response;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fsTypes")
public class FsTypeListResponse  {
	private List<String> fsTypes;
	
	public FsTypeListResponse() {
	}
	
	public FsTypeListResponse(List<String> fsTypes) {
		setFsTypes(fsTypes);
	}
	
	@XmlElement(name = "fsType", type=String.class)
	public List<String> getFsTypes() {
		return fsTypes;
	}

	public void setFsTypes(List<String> fsTypes) {
		this.fsTypes = fsTypes;
	}
}
