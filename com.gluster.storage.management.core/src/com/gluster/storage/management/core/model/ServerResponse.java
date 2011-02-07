package com.gluster.storage.management.core.model;

/**
 * Interface for server response. Any request handled by the Gluster Management
 * Server will result in a response that must be an instance of a class
 * implementing this interface.
 */
public interface ServerResponse {
	/**
	 * @return Status of request processing
	 */
	public Status getStatus();

	/**
	 * @return Data associated with the response. e.g. A "discover servers"
	 *         request will return the list of discovered servers.
	 */
	public Object getData();
}
