package com.gluster.storage.management.client;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class RESTClientTest {
	public static void main(String args[]) {
		WebResource service = Client.create(new DefaultClientConfig()).resource(getBaseURI());
		String name = service.path("services").path("name").accept(MediaType.TEXT_PLAIN).get(String.class);
		System.out.println(name);
		name = service.path("services").path("name/xml").accept(MediaType.TEXT_XML).get(String.class);
		System.out.println(name);
	}
	
	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/glustermc").build();
	}
}
