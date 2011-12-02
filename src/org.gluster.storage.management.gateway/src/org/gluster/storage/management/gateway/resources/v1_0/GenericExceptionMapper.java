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
package org.gluster.storage.management.gateway.resources.v1_0;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.gluster.storage.management.core.exceptions.GlusterValidationException;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
	private static final Logger logger = Logger.getLogger(GenericExceptionMapper.class);

	/* (non-Javadoc)
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
	 */
	@Override
	public Response toResponse(Exception exception) {
		ResponseBuilder builder;
		if (exception instanceof GlusterValidationException) {
			builder = Response.status(Response.Status.BAD_REQUEST);
		} else {
			builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
		}
		
		String errMsg = exception.getMessage();
		if(errMsg == null) {
			errMsg = "Following exception occurred : " + exception.getClass().getName();
			StackTraceElement[] stackTrace = exception.getStackTrace();
			if(stackTrace.length > 0) {
				errMsg += " at [" + stackTrace[0].getClassName() + "][" + stackTrace[0].getLineNumber() + "]";
			}
		}
		
		logger.error(errMsg, exception);
		
		return builder.entity(errMsg).type(MediaType.TEXT_PLAIN).build();
	}
}
