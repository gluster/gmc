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

import com.gluster.storage.management.core.model.Status;
import com.sun.jersey.api.representation.Form;

public class UsersClient extends AbstractClient {
	private static final String RESOURCE_NAME = "users";
	private static final String FORM_PARAM_OLD_PASSWORD = "oldpassword";
	private static final String FORM_PARAM_NEW_PASSWORD = "newpassword";
	
	private String user;

	public UsersClient(String serverName, String user, String password) {
		super(serverName, user, password);
		this.user = user;
	}

	public boolean authenticate() {
		try {
			Status authStatus = (Status) fetchSubResource(user, Status.class);
			return authStatus.isSuccess();
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}

		// Dummy authentication for demo application
		// return (connectionDetails.getPassword().equals("gluster") ? true : false);
	}

	public boolean changePassword(String user, String oldPassword, String newPassword) {
		Form form = new Form();
		form.add(FORM_PARAM_OLD_PASSWORD, oldPassword);
		form.add(FORM_PARAM_NEW_PASSWORD, newPassword);
		Status status = (Status) putRequest(user, Status.class, form);

		return status.isSuccess();
	}

	public static void main(String[] args) {
		UsersClient authClient = new UsersClient("localhost", "gluster", "gluster");
		
		// authenticate user
		System.out.println(authClient.authenticate());
		
		// change password to gluster1
		System.out.println(authClient.changePassword("gluster", "gluster", "gluster1"));
		
		// change it back to gluster
		authClient = new UsersClient("localhost", "gluster", "gluster1");
		System.out.println(authClient.changePassword("gluster", "gluster1", "gluster"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gluster.storage.management.client.AbstractClient#getResourceName()
	 */
	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}
}
