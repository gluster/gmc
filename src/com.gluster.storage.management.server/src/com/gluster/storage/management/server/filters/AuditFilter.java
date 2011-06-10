/**
 * 
 */
package com.gluster.storage.management.server.filters;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * Resource filter for maintaining audit trail of resource access
 */
public class AuditFilter implements ResourceFilter, ContainerRequestFilter, ContainerResponseFilter {

	@Override
	public ContainerRequestFilter getRequestFilter() {
		return this;
	}

	@Override
	public ContainerResponseFilter getResponseFilter() {
		return this;
	}

	@Override
	public ContainerRequest filter(ContainerRequest req) {
		System.out.println("REQUEST: [" + req.getMethod() + "][" + req.getPath() + "]");
		return req;
	}

	@Override
	public ContainerResponse filter(ContainerRequest req, ContainerResponse response) {
		System.out.println("RESPONSE: [" + req.getMethod() + "][" + req.getPath() + "]");
		return response;
	}
}
