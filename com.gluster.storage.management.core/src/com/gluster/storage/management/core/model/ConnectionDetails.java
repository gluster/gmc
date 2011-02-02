/*******************************************************************************
 * Copyright (c) 2004, 2005 Jean-Michel Lemieux, Jeff McAffer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Hyperbola is an RCP application developed for the book 
 *     Eclipse Rich Client Platform - 
 *         Designing, Coding, and Packaging Java Applications 
 *
 * Contributors:
 *     Jean-Michel Lemieux and Jeff McAffer - initial implementation
 *******************************************************************************/
package com.gluster.storage.management.core.model;

public class ConnectionDetails {
	private String userId, server, password;

	public ConnectionDetails() {
		
	}
	
	public ConnectionDetails(String server, String userId, String password) {
		this.userId = userId;
		this.server = server;
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public String getServer() {
		return server;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
