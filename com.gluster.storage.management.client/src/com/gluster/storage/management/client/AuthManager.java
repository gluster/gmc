package com.gluster.storage.management.client;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.gluster.storage.management.core.model.ConnectionDetails;

public class AuthManager {
	public boolean authenticate(ConnectionDetails connectionDetails) {
//		WebResource service = Client.create(new DefaultClientConfig()).resource(getBaseURI());
//
//		AuthStatus authStatus = service.path("services").path("login")
//				.queryParam("user", connectionDetails.getUserId())
//				.queryParam("password", connectionDetails.getPassword()).accept(MediaType.TEXT_XML)
//				.get(AuthStatus.class);
//
//		return authStatus.getIsAuthenticated();
		
		// Dummy authentication for demo application
		return (connectionDetails.getPassword().equals("gluster") ? true : false);
	}

	public static void main(String[] args) {
		AuthManager authManager = new AuthManager();
		System.out.println(authManager.authenticate(new ConnectionDetails("", "gluster", "gluster")));
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost:8080/glustersp").build();
	}
}
