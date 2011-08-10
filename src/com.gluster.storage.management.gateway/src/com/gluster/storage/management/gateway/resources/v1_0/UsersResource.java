/*******************************************************************************
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.gluster.storage.management.gateway.resources.v1_0;

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_NEW_PASSWORD;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OLD_PASSWORD;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_USER;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_USERS;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.Status;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Component
@Path(RESOURCE_PATH_USERS)
public class UsersResource extends AbstractResource {
	@Autowired
	private JdbcUserDetailsManager jdbcUserService;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private SaltSource saltSource;

	@Autowired
	private UserDetailsService userDetailsService;
	
	private static final Logger logger = Logger.getLogger(UsersResource.class);

	@Path("{" + PATH_PARAM_USER + "}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response authenticateXML(@PathParam("user") String user) {
		// success only if the user passed in query is same as the one passed in security header
		// spring security would have already authenticated the user credentials
		return getAuthenticationResponse(user, MediaType.APPLICATION_XML);
	}

	@Path("{" + PATH_PARAM_USER + "}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response authenticateJSON(@PathParam("user") String user) {
		// success only if the user passed in query is same as the one passed in security header
		// spring security would have already authenticated the user credentials
		return getAuthenticationResponse(user, MediaType.APPLICATION_JSON);
	}

	public Response getAuthenticationResponse(String user, String mediaType) {
		return (SecurityContextHolder.getContext().getAuthentication().getName().equals(user) ? okResponse(
				Status.STATUS_SUCCESS, mediaType) : unauthorizedResponse());
	}

	@Path("{" + PATH_PARAM_USER + "}")
	@PUT
	public Response changePassword(@PathParam(PATH_PARAM_USER) String username,
			@FormParam(FORM_PARAM_OLD_PASSWORD) String oldPassword,
			@FormParam(FORM_PARAM_NEW_PASSWORD) String newPassword) {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			String loggedInUser = ((UserDetails)auth.getPrincipal()).getUsername();
			if(!loggedInUser.equals(username)) {
				// Temporary check as we currently have only one user.
				throw new GlusterValidationException("User [" + loggedInUser
						+ "] is not allowed to change password of user [" + username + "]!");
			}

			UserDetails user = userDetailsService.loadUserByUsername(username);
			Object salt = saltSource.getSalt(user);

			String actualOldPasswordEncoded = ((UserDetails)auth.getPrincipal()).getPassword();
			String oldPasswordEncoded = passwordEncoder.encodePassword(oldPassword, salt);
			if(!oldPasswordEncoded.equals(actualOldPasswordEncoded)) {
				throw new GlusterValidationException("Invalid old password!");
			}
			
			String encodedNewPassword = passwordEncoder.encodePassword(newPassword, salt);
			jdbcUserService.changePassword(oldPassword, encodedNewPassword);
		} catch (Exception ex) {
			String errMsg = "Could not change password. Error: [" + ex.getMessage() + "]";
			logger.error(errMsg, ex);
			throw new GlusterRuntimeException(errMsg);
		}
		return noContentResponse();
	}
}
