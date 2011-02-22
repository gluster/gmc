package com.gluster.storage.management.client;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import com.gluster.storage.management.client.utils.ClientUtil;
import com.gluster.storage.management.core.model.ServerListResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public abstract class AbstractClient {
	protected WebResource resource;

	public AbstractClient() {
		
	}
	
	public AbstractClient(String serverName) {
		URI baseURI = new ClientUtil().getServerBaseURI(serverName);
		resource = Client.create(new DefaultClientConfig()).resource(baseURI).path(getResourceName());
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
		return res.accept(MediaType.TEXT_XML).get(responseClass);
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

	public abstract String getResourceName();
}
