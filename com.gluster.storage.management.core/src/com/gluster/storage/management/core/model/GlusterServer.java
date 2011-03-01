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

import com.gluster.storage.management.core.utils.StringUtil;

@XmlRootElement(name="glusterServer")
public class GlusterServer extends Server {
	public enum SERVER_STATUS {
		ONLINE, OFFLINE
	};
	private static final String[] STATUS_STR = new String[] { "Online", "Offline" };

	private SERVER_STATUS status;
	private NetworkInterface preferredNetworkInterface;
	private Cluster cluster;

	public GlusterServer(String name, Entity parent, SERVER_STATUS status, int numOfCPUs, double cpuUsage, double totalMemory,
			double memoryInUse) {
		super(name, parent, numOfCPUs, cpuUsage, totalMemory, memoryInUse);
		setStatus(status);
	}

	public GlusterServer(String name, Entity parent, SERVER_STATUS status, int numOfCPUs, double cpuUsage, double totalMemory,
			double memoryInUse, Cluster cluster) {
		this(name, parent, status, numOfCPUs, cpuUsage, totalMemory, memoryInUse);
		setCluster(cluster);
	}

	public String getStatusStr() {
		return STATUS_STR[getStatus().ordinal()];
	}

	public SERVER_STATUS getStatus() {
		return status;
	}
	
	public void setStatus(SERVER_STATUS status) {
		this.status = status;
	}

	public NetworkInterface getPreferredNetworkInterface() {
		return preferredNetworkInterface;
	}

	public void setPreferredNetworkInterface(NetworkInterface preferredNetworkInterface) {
		this.preferredNetworkInterface = preferredNetworkInterface;
		preferredNetworkInterface.setPreferred(true);
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * Filter matches if any of the properties name, status, preferred network interface, and primary/secondary/third
	 * DNS contains the filter string
	 */
	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtil.filterString(getName() + getStatusStr() + getPreferredNetworkInterface().getName(),
				filterString, caseSensitive);
	}
}
