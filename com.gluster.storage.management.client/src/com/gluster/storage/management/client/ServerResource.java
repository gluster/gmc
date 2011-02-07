package com.gluster.storage.management.client;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.gluster.storage.management.core.model.ServerListResponse;
import com.gluster.storage.management.core.model.Server;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class ServerResource {
	private final URI BASE_URI = UriBuilder.fromUri("http://localhost:8080/glustermc").build();

	public List<Server> discoverServers() {
		WebResource service = Client.create(new DefaultClientConfig()).resource(BASE_URI);
		
		@SuppressWarnings("unchecked")
		ServerListResponse<Server> response = service.path("services").path("server").path("discover")
				.accept(MediaType.TEXT_XML).get(ServerListResponse.class);
		
		return response.getData();
	}

	public static void main(String[] args) {
		ServerResource ServerResource = new ServerResource();
		List<Server> discoveredServers = ServerResource.discoverServers();
		System.out.println(discoveredServers.size());
	}
}
