/**
 * 
 */
package com.gluster.storage.management.gateway.filters;

import java.security.Principal;

import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * Resource filter for maintaining audit trail of resource access
 */
public class AuditFilter implements ResourceFilter, ContainerRequestFilter, ContainerResponseFilter {
	private static final Logger logger = Logger.getLogger(AuditFilter.class);
	
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
		Principal principal = req.getUserPrincipal();
		if(principal != null) {
			logger.info("REQUEST from [" +  principal.getName() + "] : [" + req.getMethod() + "][" + req.getPath() + "]");
		} else {
			logger.info("REQUEST [" + req.getMethod() + "][" + req.getPath() + "]");
		}
		return req;
	}

	@Override
	public ContainerResponse filter(ContainerRequest req, ContainerResponse response) {
		logger.info("RESPONSE: [" + req.getMethod() + "][" + req.getPath() + "]");
		return response;
	}
}
