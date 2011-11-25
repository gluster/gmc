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
package org.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.gluster.storage.management.core.utils.StringUtil;


@XmlRootElement(name = "glusterServer")
public class GlusterServer extends Server {
	private String uuid;

	public GlusterServer() {
	}

	public GlusterServer(String name) {
		super(name);
	}

	public GlusterServer(String name, Entity parent, SERVER_STATUS status, int numOfCPUs, double cpuUsage,
			double totalMemory, double memoryInUse) {
		super(name, parent, numOfCPUs, cpuUsage, totalMemory, memoryInUse);
		setStatus(status);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Filter matches if any of the properties name and status contains the filter string
	 */
	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtil.filterString(getName() + getStatusStr(), filterString, caseSensitive);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof GlusterServer)) {
			return false;
		}
		GlusterServer server = (GlusterServer)obj;
		
		if (super.equals(server) && getUuid().equals(server.getUuid()) && getStatus() == server.getStatus()) {
			return true;
		}
		
		return false;
	}
	
	public void copyFrom(GlusterServer server) {
		super.copyFrom(server);
		setUuid(server.getUuid());
		setStatus(server.getStatus());
	}
}