package com.gluster.storage.management.client;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.client.utils.ClientUtil;
import com.gluster.storage.management.core.model.AuthStatus;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.Base64;

public abstract class AbstractClient {
	private static final String HTTP_HEADER_AUTH = "Authorization";

	protected WebResource resource;
	private String authHeader;

	public AbstractClient(String serverName, String user, String password) {
		URI baseURI = new ClientUtil().getServerBaseURI(serverName);
		resource = Client.create(new DefaultClientConfig()).resource(baseURI).path(getResourceName());
		authHeader = "Basic " + new String(Base64.encode(user + ":" + password));
	}

	/**
	 * Fetches the given resource by dispatching a GET request
	 * 
	 * @param res
	 *            Resource to be fetched
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object fetchResource(WebResource res, Class responseClass) {
		return res.header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.TEXT_XML).get(responseClass);
	}

	/**
	 * Fetches the default resource (the one returned by {@link AbstractClient#getResourceName()}) by dispatching a GET
	 * request on the resource
	 * 
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request
	 */
	@SuppressWarnings("rawtypes")
	protected Object fetchResource(Class responseClass) {
		return fetchResource(resource, responseClass);
	}

	/**
	 * Fetches the resource whose name is arrived at by appending the "subResourceName" parameter to the default
	 * resource (the one returned by {@link AbstractClient#getResourceName()})
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request on the sub-resource
	 */
	@SuppressWarnings("rawtypes")
	protected Object fetchSubResource(String subResourceName, Class responseClass) {
		return fetchResource(resource.path(subResourceName), responseClass);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object postRequest(String subResourceName, Class responseClass, Form form) {
		return resource.path(subResourceName).header("Authorization", authHeader).accept(MediaType.TEXT_XML)
				.post(responseClass, form);
	}

	public abstract String getResourceName();
}
