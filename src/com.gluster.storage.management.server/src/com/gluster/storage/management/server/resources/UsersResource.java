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
package com.gluster.storage.management.server.resources;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.model.Status;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Component
@Path("/users")
public class UsersResource {
	@Autowired
	private JdbcUserDetailsManager jdbcUserService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Authenticates given user with given password for login on current system
	 * 
	 * @param user
	 * @param password
	 * @return true is user can be successfully authenticated using given password, else false
	 */
	/*
	 * NOTE: This method is no more required as user authentication is performed on every request by the spring security
	 * framework. Can be removed after testing.
	 */
	/*
	 * private boolean authenticate(String user, String password) { String tmpFileName = "tmp"; File saltFile = new
	 * File(tmpFileName); ProcessResult result = new ProcessUtil().executeCommand("get-user-password.py", user,
	 * tmpFileName); if (result.isSuccess()) { String salt = new FileUtil().readFileAsString(saltFile); String
	 * encryptedPassword = MD5Crypt.crypt(password, salt); return encryptedPassword.equals(salt); }
	 * 
	 * return false; }
	 */

	@Path("{user}")
	@GET
	@Produces(MediaType.TEXT_XML)
	public Status login(@PathParam("user") String user) {
		// success only if the user passed in query is same as the one passed in security header
		return (SecurityContextHolder.getContext().getAuthentication().getName().equals(user) ? Status.STATUS_SUCCESS
				: Status.STATUS_FAILURE);
	}

	@Path("{user}")
	@PUT
	@Produces(MediaType.TEXT_XML)
	public Status changePassword(@FormParam("oldpassword") String oldPassword,
			@FormParam("newpassword") String newPassword) {
		try {
			jdbcUserService.changePassword(oldPassword, passwordEncoder.encodePassword(newPassword, null));
		} catch (AuthenticationException ex) {
			ex.printStackTrace();
			return new Status(Status.STATUS_CODE_FAILURE, "Could not change password: [" + ex.getMessage() + "]");
		}
		return Status.STATUS_SUCCESS;
	}
}
