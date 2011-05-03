package com.gluster.storage.management.client;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import com.gluster.storage.management.client.utils.ClientUtil;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public abstract class AbstractClient {
	private static final String HTTP_HEADER_AUTH = "Authorization";
	protected static final MultivaluedMap<String, String> NO_PARAMS = new MultivaluedMapImpl();

	protected WebResource resource;
	private String securityToken;
	private String authHeader;

	public AbstractClient() {
		URI baseURI = new ClientUtil().getServerBaseURI();
		resource = Client.create(new DefaultClientConfig()).resource(baseURI).path(getResourceName());
	}

	public AbstractClient(String securityToken) {
		this();
		setSecurityToken(securityToken);
	}

	/**
	 * Fetches the given resource by dispatching a GET request
	 * 
	 * @param res
	 *            Resource to be fetched
	 * @param queryParams
	 *            Query parameters to be sent for the GET request
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object fetchResource(WebResource res, MultivaluedMap<String, String> queryParams, Class responseClass) {
		return res.queryParams(queryParams).header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.TEXT_XML)
				.get(responseClass);
	}
	
	private Object downloadResource(WebResource res, MultivaluedMap<String, String> queryParams, Class responseClass) {
		return res.queryParams(queryParams).header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.TEXT_XML)
				.get(responseClass);
	}
	
	protected Object downloadResource(WebResource res) {
		ClientResponse response = res.header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.APPLICATION_OCTET_STREAM).get(ClientResponse.class);
		return response;
	}

	/**
	 * Fetches the default resource (the one returned by {@link AbstractClient#getResourceName()}) by dispatching a GET
	 * request on the resource
	 * 
	 * @param queryParams
	 *            Query parameters to be sent for the GET request
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
		Object response = fetchResource(resource, NO_PARAMS, responseClass);
		return response;
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

	protected Object downloadSubResource(String subResourceName) {
		return downloadResource(resource.path(subResourceName));
	}

	/**
	 * Fetches the resource whose name is arrived at by appending the "subResourceName" parameter to the default
	 * resource (the one returned by {@link AbstractClient#getResourceName()})
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource
	 * @param queryParams
	 *            Query parameters to be sent for the GET request
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request on the sub-resource
	 */
	@SuppressWarnings("rawtypes")
	protected Object fetchSubResource(String subResourceName, MultivaluedMap<String, String> queryParams,
			Class responseClass) {
		return fetchResource(resource.path(subResourceName), queryParams, responseClass);
	}
	
	/**
	 * Submits given Form using POST method to the resource and returns the object received as response
	 * 
	 * @param responseClass
	 *            Class of the object expected as response
	 * @param form
	 *            Form to be submitted
	 * @return Object of given class received as response
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object postRequest(Class responseClass, Form form) {
		return resource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).header("Authorization", authHeader)
				.accept(MediaType.TEXT_XML).post(responseClass, form);
	}

	/**
	 * Submits given Form using POST method to the given sub-resource and returns the object received as response
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource to which the request is to be posted
	 * @param responseClass
	 *            Class of the object expected as response
	 * @param form
	 *            Form to be submitted
	 * @return Object of given class received as response
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object postRequest(String subResourceName, Class responseClass, Form form) {
		return resource.path(subResourceName).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.header("Authorization", authHeader).accept(MediaType.TEXT_XML).post(responseClass, form);
	}

	/**
	 * Submits given Form using PUT method to the given sub-resource and returns the object received as response
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource to which the request is to be posted
	 * @param responseClass
	 *            Class of the object expected as response
	 * @param form
	 *            Form to be submitted
	 * @return Object of given class received as response
	 */
	protected Object putRequest(String subResourceName, Class responseClass, Form form) {
		return resource.path(subResourceName).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.header("Authorization", authHeader).accept(MediaType.TEXT_XML).put(responseClass, form);
	}

	/**
	 * Submits given Form using PUT method to the given sub-resource and returns the object received as response
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource to which the request is to be posted
	 * @param responseClass
	 *            Class of the object expected as response
	 * @return Object of given class received as response
	 */
	protected Object putRequest(String subResourceName, Class responseClass) {
		return resource.path(subResourceName).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.header("Authorization", authHeader).accept(MediaType.TEXT_XML).put(responseClass);
	}

	/**
	 * Submits given object to the resource and returns the object received as response
	 * 
	 * @param responseClass
	 *            Class of the object expected as response
	 * @param requestObject
	 *            the Object to be submitted
	 * @return Object of given class received as response
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object postObject(Class responseClass, Object requestObject) {
		return resource.type(MediaType.TEXT_XML).header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.TEXT_XML)
				.post(responseClass, requestObject);
	}

	@SuppressWarnings("unchecked")
	protected Object deleteResource(Class responseClass, Form form) {
		return resource.header(HTTP_HEADER_AUTH, authHeader).delete(responseClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object deleteSubResource(String subResourceName, Class responseClass, String volumeName,
			String deleteOption) {
		return resource.path(subResourceName).queryParam(RESTConstants.QUERY_PARAM_VOLUME_NAME, volumeName)
				.queryParam(RESTConstants.QUERY_PARAM_DELETE_OPTION, deleteOption).header(HTTP_HEADER_AUTH, authHeader)
				.delete(responseClass);
	}

	public abstract String getResourceName();

	/**
	 * @return the securityToken
	 */
	protected String getSecurityToken() {
		return securityToken;
	}

	/**
	 * @param securityToken
	 *            the securityToken to set
	 */
	protected void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
		authHeader = "Basic " + securityToken;
	}
}
