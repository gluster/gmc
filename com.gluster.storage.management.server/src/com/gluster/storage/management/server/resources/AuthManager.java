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

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;

import com.gluster.storage.management.core.model.AuthStatus;
import com.gluster.storage.management.core.utils.FileUtil;
import com.gluster.storage.management.core.utils.MD5Crypt;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;

import com.sun.jersey.spi.container.ContainerRequest;

@Path("/login")
public class AuthManager {
	@Context
	private Request request;

	/**
	 * Authenticates given user with given password for login on current system
	 * @param user
	 * @param password
	 * @return true is user can be successfully authenticated using given password, else false
	 */
	private boolean authenticate(String user, String password) {
		String tmpFileName = "tmp";
		File saltFile = new File(tmpFileName);
		ProcessResult result = new ProcessUtil().executeCommand("get-user-password.py", user, tmpFileName);
		if (result.isSuccess()) {
			String salt = new FileUtil().readFileAsString(saltFile);
			String encryptedPassword = MD5Crypt.crypt(password, salt);
			return encryptedPassword.equals(salt);
		}

		return false;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public AuthStatus login() {
		String user = null;
		String password = null;

		if (request instanceof ContainerRequest) {
			ContainerRequest containerRequest = (ContainerRequest) request;
			MultivaluedMap<String, String> paramsMap = containerRequest.getQueryParameters();
			user = paramsMap.get("user").get(0);
			password = paramsMap.get("password").get(0);
		}

		AuthStatus authStatus = new AuthStatus();
		authStatus.setIsAuthenticated(authenticate(user, password));

		return authStatus;
	}
}
