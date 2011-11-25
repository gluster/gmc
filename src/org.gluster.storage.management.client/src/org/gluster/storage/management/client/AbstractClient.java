package org.gluster.storage.management.client;

import static org.gluster.storage.management.client.constants.ClientConstants.ALGORITHM_SUNX509;
import static org.gluster.storage.management.client.constants.ClientConstants.KEYSTORE_TYPE_JKS;
import static org.gluster.storage.management.client.constants.ClientConstants.PROTOCOL_TLS;
import static org.gluster.storage.management.client.constants.ClientConstants.TRUSTED_KEYSTORE;
import static org.gluster.storage.management.client.constants.ClientConstants.TRUSTED_KEYSTORE_ACCESS;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.gluster.storage.management.client.utils.ClientUtil;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataMultiPart;

public abstract class AbstractClient {
	private static final String HTTP_HEADER_AUTH = "Authorization";
	protected static final MultivaluedMap<String, String> NO_PARAMS = new MultivaluedMapImpl();
	protected static String clusterName;
	protected static String securityToken;
	protected WebResource resource;
	private String authHeader;
	private Client client;

	/**
	 * This constructor will work only after the data model manager has been initialized.
	 */
	public AbstractClient() {
		this(securityToken, clusterName);
	}

	/**
	 * This constructor will work only after the data model manager has been initialized.
	 */
	public AbstractClient(String clusterName) {
		this(securityToken, clusterName);
	}

	public AbstractClient(String securityToken, String clusterName) {
		AbstractClient.clusterName = clusterName;
		setSecurityToken(securityToken);

		createClient();

		// this must be after setting clusterName as sub-classes may refer to cluster name in the getResourcePath method
		resource = client.resource(ClientUtil.getServerBaseURI()).path(getResourcePath());
	}

	private void createClient() {
		SSLContext context = initializeSSLContext();
		DefaultClientConfig config = createClientConfig(context);
		client = Client.create(config);
	}

	private DefaultClientConfig createClientConfig(SSLContext context) {
		DefaultClientConfig config = new DefaultClientConfig();
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(createHostnameVerifier(), context));
		return config;
	}

	private HostnameVerifier createHostnameVerifier() {
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		return hostnameVerifier;
	}

	private SSLContext initializeSSLContext() {
		SSLContext context = null;
		try {
			context = SSLContext.getInstance(PROTOCOL_TLS);

			KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE_JKS);
			keyStore.load(loadResource(TRUSTED_KEYSTORE), TRUSTED_KEYSTORE_ACCESS.toCharArray());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(ALGORITHM_SUNX509);
			keyManagerFactory.init(keyStore, TRUSTED_KEYSTORE_ACCESS.toCharArray());

			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(ALGORITHM_SUNX509);
			trustManagerFactory.init(keyStore);

			context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		} catch (Exception e) {
			throw new GlusterRuntimeException(
					"Couldn't initialize SSL Context with Gluster Management Gateway! Error: " + e, e);
		}
		return context;
	}

	private InputStream loadResource(String resourcePath) {
		return this.getClass().getClassLoader().getResourceAsStream(resourcePath);
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
	private <T> T fetchResource(WebResource res, MultivaluedMap<String, String> queryParams, Class<T> responseClass) {
		try {
			return res.queryParams(queryParams).header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.APPLICATION_XML)
					.get(responseClass);
		} catch (Exception e1) {
			throw createGlusterException(e1);
		}
	}

	private GlusterRuntimeException createGlusterException(Exception e) {
		if (e instanceof GlusterRuntimeException) {
			return (GlusterRuntimeException) e;
		}

		if (e instanceof UniformInterfaceException) {
			UniformInterfaceException uie = (UniformInterfaceException) e;
			if ((uie.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())) {
				// authentication failed. clear security token.
				setSecurityToken(null);
				return new GlusterRuntimeException("Invalid credentials!");
			} else {
				return new GlusterRuntimeException("[" + uie.getResponse().getStatus() + "]["
						+ uie.getResponse().getEntity(String.class) + "]");
			}
		} else {
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof ConnectException) {
				return new GlusterRuntimeException("Couldn't connect to Gluster Management Gateway!");
			}

			return new GlusterRuntimeException("Exception in REST communication! [" + e.getMessage() + "]", e);
		}
	}

	protected void downloadResource(WebResource res, String filePath) {
		ClientResponse response = null;
		try {
			response = res.header(HTTP_HEADER_AUTH, authHeader).accept(MediaType.APPLICATION_OCTET_STREAM)
				.get(ClientResponse.class);
			checkResponseStatus(response);
		} catch (Exception e1) {
			throw createGlusterException(e1);
		}
		
		try {
			if (!response.hasEntity()) {
				throw new GlusterRuntimeException("No entity in response!");
			}

			InputStream inputStream = response.getEntityInputStream();
			FileOutputStream outputStream = new FileOutputStream(filePath);

			int c;
			while ((c = inputStream.read()) != -1) {
				outputStream.write(c);
			}
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			throw new GlusterRuntimeException("Error while downloading resource [" + res.getURI().getPath() + "]", e);
		}
	}

	public void uploadResource(WebResource res, FormDataMultiPart form) {
		try {
			res.header(HTTP_HEADER_AUTH, authHeader).type(MediaType.MULTIPART_FORM_DATA_TYPE).post(String.class, form);
		} catch (Exception e) {
			throw createGlusterException(e);
		}
	}

	/**
	 * Fetches the default resource (the one returned by {@link AbstractClient#getResourcePath()}) by dispatching a GET
	 * request on the resource
	 * 
	 * @param queryParams
	 *            Query parameters to be sent for the GET request
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request
	 */
	protected <T> T fetchResource(MultivaluedMap<String, String> queryParams, Class<T> responseClass) {
		return fetchResource(resource, queryParams, responseClass);
	}

	/**
	 * Fetches the default resource (the one returned by {@link AbstractClient#getResourcePath()}) by dispatching a GET
	 * request on the resource
	 * 
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request
	 */
	protected <T> T fetchResource(Class<T> responseClass) {
		return fetchResource(resource, NO_PARAMS, responseClass);
	}

	/**
	 * Fetches the resource whose name is arrived at by appending the "subResourceName" parameter to the default
	 * resource (the one returned by {@link AbstractClient#getResourcePath()})
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request on the sub-resource
	 */
	protected <T> T fetchSubResource(String subResourceName, Class<T> responseClass) {
		return fetchResource(resource.path(subResourceName), NO_PARAMS, responseClass);
	}

	protected void downloadSubResource(String subResourceName, String filePath) {
		downloadResource(resource.path(subResourceName), filePath);
	}

	/**
	 * Fetches the resource whose name is arrived at by appending the "subResourceName" parameter to the default
	 * resource (the one returned by {@link AbstractClient#getResourcePath()})
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource
	 * @param queryParams
	 *            Query parameters to be sent for the GET request
	 * @param responseClass
	 *            Expected class of the response
	 * @return Object of responseClass received as a result of the GET request on the sub-resource
	 */
	protected <T> T fetchSubResource(String subResourceName, MultivaluedMap<String, String> queryParams,
			Class<T> responseClass) {
		return fetchResource(resource.path(subResourceName), queryParams, responseClass);
	}

	private ClientResponse postRequest(WebResource resource, Form form) {
		try {
			ClientResponse response = prepareFormRequestBuilder(resource).post(ClientResponse.class, form);
			checkResponseStatus(response);
			return response;
		} catch (UniformInterfaceException e) {
			throw new GlusterRuntimeException(e.getResponse().getEntity(String.class));
		}
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
	protected <T> T postObject(Class<T> responseClass, Object requestObject) {
		return resource.type(MediaType.APPLICATION_XML).header(HTTP_HEADER_AUTH, authHeader)
				.accept(MediaType.APPLICATION_XML).post(responseClass, requestObject);
	}

	/**
	 * Submits given Form using POST method to the resource and returns the object received as response
	 * 
	 * @param form
	 *            Form to be submitted
	 */
	protected URI postRequest(Form form) {
		return postRequest(resource, form).getLocation();
	}

	/**
	 * Submits given Form using POST method to the given sub-resource and returns the object received as response
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource to which the request is to be posted
	 * @param form
	 *            Form to be submitted
	 */
	protected void postRequest(String subResourceName, Form form) {
		postRequest(resource.path(subResourceName), form);
	}

	private ClientResponse putRequest(WebResource resource, Form form) {
		try {
			ClientResponse response = prepareFormRequestBuilder(resource).put(ClientResponse.class, form);
			checkResponseStatus(response);
			return response;
		} catch (Exception e) {
			throw createGlusterException(e);
		}
	}

	private void checkResponseStatus(ClientResponse response) {
		if ((response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())) {
			// authentication failed. clear security token.
			setSecurityToken(null);
			throw new GlusterRuntimeException("Invalid credentials!");
		}
		if (response.getStatus() >= 300) {
			throw new GlusterRuntimeException(response.getEntity(String.class));
		}
	}

	public Builder prepareFormRequestBuilder(WebResource resource) {
		return resource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).header(HTTP_HEADER_AUTH, authHeader)
				.accept(MediaType.APPLICATION_XML);
	}

	/**
	 * Submits given Form using PUT method to the given sub-resource and returns the object received as response
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource to which the request is to be posted
	 * @param form
	 *            Form to be submitted
	 */
	protected void putRequest(String subResourceName, Form form) {
		putRequest(resource.path(subResourceName), form);
	}

	protected URI putRequestURI(String subResourceName, Form form) {
		ClientResponse response = putRequest(resource.path(subResourceName), form);
		return response.getLocation();
	}

	/**
	 * Submits given Form using PUT method to the given sub-resource and returns the object received as response
	 * 
	 * @param form
	 *            Form to be submitted
	 */
	protected void putRequest(Form form) {
		putRequest(resource, form);
	}

	/**
	 * Submits given Form using PUT method to the given sub-resource and returns the object received as response
	 * 
	 * @param subResourceName
	 *            Name of the sub-resource to which the request is to be posted
	 */
	protected void putRequest(String subResourceName) {
		try {
			prepareFormRequestBuilder(resource.path(subResourceName)).put();
		} catch (UniformInterfaceException e) {
			throw new GlusterRuntimeException(e.getResponse().getEntity(String.class));
		}
	}

	private void deleteResource(WebResource resource, MultivaluedMap<String, String> queryParams) {
		try {
			resource.queryParams(queryParams).header(HTTP_HEADER_AUTH, authHeader).delete();
		} catch (UniformInterfaceException e) {
			throw new GlusterRuntimeException(e.getResponse().getEntity(String.class));
		}
	}

	protected void deleteResource(MultivaluedMap<String, String> queryParams) {
		deleteResource(resource, queryParams);
	}

	protected void deleteSubResource(String subResourceName, MultivaluedMap<String, String> queryParams) {
		deleteResource(resource.path(subResourceName), queryParams);
	}

	protected void deleteSubResource(String subResourceName) {
		try {
			resource.path(subResourceName).header(HTTP_HEADER_AUTH, authHeader).delete();
		} catch (UniformInterfaceException e) {
			throw new GlusterRuntimeException(e.getResponse().getEntity(String.class));
		}
	}

	public abstract String getResourcePath();

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
		AbstractClient.securityToken = securityToken;
		authHeader = "Basic " + securityToken;
	}

	/**
	 * @param uri
	 *            The URI to be fetched using GET API
	 * @param responseClass
	 *            Expected type of response object
	 * @return Object of the given class
	 */
	protected <T> T fetchResource(URI uri, Class<T> responseClass) {
		return fetchResource(client.resource(uri), NO_PARAMS, responseClass);
	}
}
