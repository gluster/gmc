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
package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "response")
public class ServerListResponse extends AbstractResponse {
	private List<Server> servers = new ArrayList<Server>();

	public ServerListResponse() {
	}

	public ServerListResponse(Status status, List<Server> servers) {
		setStatus(status);
		setServers(servers);
	}

	@XmlElementWrapper(name = "servers")
	@XmlElement(name = "server", type=Server.class)
	public List<Server> getServers() {
		return servers;
	}

	/**
	 * @param servers
	 *            the servers to set
	 */
	public void setServers(List<Server> servers) {
		this.servers = servers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gluster.storage.management.core.model.Response#getData()
	 */
	@Override
	@XmlTransient
	public List<Server> getData() {
		return getServers();
	}
}
