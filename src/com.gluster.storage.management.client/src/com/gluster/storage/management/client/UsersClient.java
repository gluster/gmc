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
package com.gluster.storage.management.client;

import java.net.ConnectException;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.Base64;

public class UsersClient extends AbstractClient {
	private static final String RESOURCE_NAME = "users";
	private static final String FORM_PARAM_OLD_PASSWORD = "oldpassword";
	private static final String FORM_PARAM_NEW_PASSWORD = "newpassword";
	private static final Logger logger = Logger.getLogger(UsersClient.class);

	private String generateSecurityToken(String user, String password) {
		return new String(Base64.encode(user + ":" + password));
	}

	public UsersClient() {
		super();
	}

	public void authenticate(String user, String password) {
		setSecurityToken(generateSecurityToken(user, password));
		fetchSubResource(user, Status.class);
		// authentication successful. update security token in the data model manager
		GlusterDataModelManager.getInstance().setSecurityToken(getSecurityToken());
	}

	public void changePassword(String user, String oldPassword, String newPassword) {
		setSecurityToken(generateSecurityToken(user, oldPassword));

		Form form = new Form();
		form.add(FORM_PARAM_OLD_PASSWORD, oldPassword);
		form.add(FORM_PARAM_NEW_PASSWORD, newPassword);
		putRequest(user, form);
		
		// password changed. set the new security token
		setSecurityToken(generateSecurityToken(user, newPassword));
		GlusterDataModelManager.getInstance().setSecurityToken(getSecurityToken());
	}

	public static void main(String[] args) {
		UsersClient authClient = new UsersClient();

		// authenticate user
		authClient.authenticate("gluster", "gluster");

		// change password to gluster1
		authClient.changePassword("gluster", "gluster", "gluster1");

		// change it back to gluster
		authClient.changePassword("gluster", "gluster1", "gluster");
		
		System.out.println("success");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gluster.storage.management.client.AbstractClient#getResourceName()
	 */
	@Override
	public String getResourcePath() {
		return RESOURCE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gluster.storage.management.client.AbstractClient#getSecurityToken()
	 */
	@Override
	public String getSecurityToken() {
		return super.getSecurityToken();
	}
}
