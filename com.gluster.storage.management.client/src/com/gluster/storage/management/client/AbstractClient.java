package com.gluster.storage.management.client;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.client.utils.ClientUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public abstract class AbstractClient {
	private static final String HTTP_HEADER_AUTH = "Authorization";
	protected static final MultivaluedMap<String, String> NO_PARAMS = new MultivaluedMapImpl();

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
	 * @param queryParams Query parameters to be sent for the GET request
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object fetchResource(WebResource res, MultivaluedMap<String, String> queryParams, Class responseClass) {
		return res.queryParams(queryParams).header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.TEXT_XML)
				.get(responseClass);
	}

	/**
	 * Fetches the default resource (the one returned by {@link AbstractClient#getResourceName()}) by dispatching a GET
	 * request on the resource
	 * 
	 * @param queryParams Query parameters to be sent for the GET request
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request
	 */
	@SuppressWarnings("rawtypes")
	protected Object fetchResource(MultivaluedMap<String, String> queryParams, Class responseClass) {
		return fetchResource(resource, queryParams, responseClass);
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
		return fetchResource(resource, NO_PARAMS, responseClass);
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
		return fetchResource(resource.path(subResourceName), NO_PARAMS, responseClass);
	}

	protected Object postRequest(Class responseClass, Form form) {
		return resource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).header("Authorization", authHeader)
				.accept(MediaType.TEXT_XML).post(responseClass, form);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object postRequest(String subResourceName, Class responseClass, Form form) {
		return resource.path(subResourceName).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.header("Authorization", authHeader).accept(MediaType.TEXT_XML).post(responseClass, form);
	}

	public abstract String getResourceName();
}
