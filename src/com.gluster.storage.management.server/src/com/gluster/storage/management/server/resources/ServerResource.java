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
package com.gluster.storage.management.server.resources;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.utils.FileUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;

@Path("/server")
public class ServerResource {
	// TODO: xml should be read from a "work" directory under the tomcat server.
	// Use relative path - do not hard code the absolute path.
	public static final String DISCOVERED_SERVERS_XML = "/GLUSTER/discovered-servers.xml";

	/**
	 * Discover newly available servers
	 * 
	 * @return list of discovered servers
	 */
	private String GetDiscoveredServers() {
		File discoveredServersFile = new File(DISCOVERED_SERVERS_XML);
		String serverNames = new FileUtil().readFileAsString(discoveredServersFile);
		return serverNames;
	}

	@Path("/discover")
	@GET
	@Produces(MediaType.TEXT_XML)
	public String discoveredServers() {
		return GetDiscoveredServers();
	}

	private String GetDetails() {
		ProcessResult result = new ProcessUtil().executeCommand("get-server-details.py");
		if (!result.isSuccess()) {
			//TODO:Generate error message and return
		}
		return result.getOutput();
	}

	@Path("/details")
	@GET
	@Produces(MediaType.TEXT_XML)
	public String serverDetails() {
		return GetDetails();
	}
	
}
