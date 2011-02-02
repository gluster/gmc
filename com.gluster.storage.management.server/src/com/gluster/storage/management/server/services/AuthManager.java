package com.gluster.storage.management.server.services;

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
