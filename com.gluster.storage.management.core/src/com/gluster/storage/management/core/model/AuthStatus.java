package com.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthStatus {
	private boolean isAuthenticated;

	public boolean getIsAuthenticated() {
		return isAuthenticated;
	}

	public void setIsAuthenticated(boolean authenticated) {
		this.isAuthenticated = authenticated;
	}
	
}
