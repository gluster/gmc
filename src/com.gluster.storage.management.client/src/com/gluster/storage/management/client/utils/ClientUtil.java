package com.gluster.storage.management.client.utils;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.gluster.storage.management.client.constants.ClientConstants;

public class ClientUtil {

	public static URI getServerBaseURI() {
		return UriBuilder.fromUri(getBaseURL()).path(ClientConstants.REST_API_VERSION).build();
	}
	
	private static String getBaseURL() {
		// remove the platform path (e.g. /linux.gtk.x86_64) from the URL
		return System.getProperty(ClientConstants.SYS_PROP_SERVER_URL, ClientConstants.DEFAULT_SERVER_URL)
				.replaceAll(ClientConstants.CONTEXT_ROOT + "\\/.*", ClientConstants.CONTEXT_ROOT + "\\/");
	}
}
