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
package org.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "networkInterface")
public class NetworkInterface extends Entity {
	private String hwAddr;
	private String model;
	private String speed;
	private String ipAddress;
	private String netMask;
	private String defaultGateway;

	public NetworkInterface() {

	}
	
	public NetworkInterface(String name, Entity parent, String hwAddr, String model, String speed, String ipAddress,
			String netMask, String defaultGateway) {
		super(name, parent);
		setHwAddr(hwAddr);
		setModel(model);
		setSpeed(speed);
		setIpAddress(ipAddress);
		setNetMask(netMask);
		setDefaultGateway(defaultGateway);
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

	public String getHwAddr() {
		return hwAddr;
	}

	public void setHwAddr(String hwAddr) {
		this.hwAddr = hwAddr;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NetworkInterface)) {
			return false;
		}

		NetworkInterface networkInterface = (NetworkInterface) obj;
		if (getName().equals(networkInterface.getName()) && getHwAddr().equals(networkInterface.getHwAddr())
				&& getIpAddress().equals(networkInterface.getIpAddress())
				&& getDefaultGateway().equals(networkInterface.getDefaultGateway())
				&& getNetMask().equals(networkInterface.getNetMask()) && getSpeed().equals(networkInterface.getSpeed())
				&& getModel().equals(networkInterface.getModel())) {
			return true;
		}

		return false;
	}
}