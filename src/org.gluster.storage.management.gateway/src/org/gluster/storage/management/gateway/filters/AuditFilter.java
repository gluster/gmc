/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Gateway.
 *
 * Gluster Management Gateway is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Gateway is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * 
 */
package org.gluster.storage.management.gateway.filters;

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
