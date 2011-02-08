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
package com.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="networkInterface")
public class NetworkInterface extends Entity {
	private String ipAddress;
	private String netMask;
	private String defaultGateway;
	private boolean isPreferred;
	
	public NetworkInterface() {
		
	}
	
	public boolean isPreferred() {
		return isPreferred;
	}

	public void setPreferred(boolean isPreferred) {
		this.isPreferred = isPreferred;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getNetMask() {
		return netMask;
	}

	public void setNetMask(String netMask) {
		this.netMask = netMask;
	}

	public String getDefaultGateway() {
		return defaultGateway;
	}

	public void setDefaultGateway(String defaultGateway) {
		this.defaultGateway = defaultGateway;
	}

	public NetworkInterface(String name, Entity parent, String ipAddress, String netMask, String defaultGateway) {
		super(name, parent);
		setIpAddress(ipAddress);
		setNetMask(netMask);
		setDefaultGateway(defaultGateway);
	}
	
}
