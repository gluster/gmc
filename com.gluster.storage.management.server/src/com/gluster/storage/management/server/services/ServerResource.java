package com.gluster.storage.management.server.services;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.core.utils.FileUtil;

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
}
