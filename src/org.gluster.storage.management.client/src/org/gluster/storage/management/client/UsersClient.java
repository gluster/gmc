/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.client;

import static org.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_NEW_PASSWORD;
import static org.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OLD_PASSWORD;

import org.gluster.storage.management.core.constants.RESTConstants;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.model.Status;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.Base64;


public class UsersClient extends AbstractClient {
	private String generateSecurityToken(String user, String password) {
		return new String(Base64.encode(user + ":" + password));
	}

	public UsersClient() {
		super();
	}

	public void authenticate(String user, String password) {
		setSecurityToken(generateSecurityToken(user, password));
		fetchSubResource(user, Status.class);
	}

	public void changePassword(String user, String oldPassword, String newPassword) {
		String oldSecurityToken = getSecurityToken();
		String newSecurityToken = generateSecurityToken(user, oldPassword); 
		if(!oldSecurityToken.equals(newSecurityToken)) {
			throw new GlusterRuntimeException("Invalid old password!");
		}

		Form form = new Form();
		form.add(FORM_PARAM_OLD_PASSWORD, oldPassword);
		form.add(FORM_PARAM_NEW_PASSWORD, newPassword);
		putRequest(user, form);
		
		// password changed. set the new security token
		setSecurityToken(generateSecurityToken(user, newPassword));
		//authenticate(user, newPassword);
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
	 * @see org.gluster.storage.management.client.AbstractClient#getResourceName()
	 */
	@Override
	public String getResourcePath() {
		return RESTConstants.RESOURCE_USERS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gluster.storage.management.client.AbstractClient#getSecurityToken()
	 */
	@Override
	public String getSecurityToken() {
		return super.getSecurityToken();
	}
}
