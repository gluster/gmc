package com.gluster.storage.management.client.utils;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.WebResource;

public class ClientUtil {
	private static final String SERVER_PORT = "8080";
	private static final String WEB_CONTEXT = "/glustermc";
	private static final String WEB_RESOURCE_BASE_PATH = "/resources";

	public URI getServerBaseURI(String serverName) {
		return UriBuilder.fromUri("http://" + serverName + ":" + SERVER_PORT + WEB_CONTEXT + WEB_RESOURCE_BASE_PATH)
				.build();
	}
	
}
