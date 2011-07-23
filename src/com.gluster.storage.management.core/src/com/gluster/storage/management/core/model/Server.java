/*******************************************************************************
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.utils.GlusterCoreUtil;
import com.gluster.storage.management.core.utils.StringUtil;

@XmlRootElement(name = "server")
public class Server extends Entity {
	public enum SERVER_STATUS {
		ONLINE, OFFLINE
	};

	protected static final String[] STATUS_STR = new String[] { "Online", "Offline" };
	
	private int numOfCPUs;
	private double cpuUsage;
	private double totalMemory;
	private double memoryInUse;
	private List<Disk> disks = new ArrayList<Disk>();
	private List<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
	private SERVER_STATUS status;

	public Server() {

	}

	public Server(String name) {
		super(name, null);
	}

	public Server(String name, Entity parent, int numOfCPUs, double cpuUsage, double totalMemory, double memoryInUse) {
		super(name, parent);
		setNumOfCPUs(numOfCPUs);
		setCpuUsage(cpuUsage);
		setTotalMemory(totalMemory);
		setMemoryInUse(memoryInUse);
	}

	public int getNumOfCPUs() {
		return numOfCPUs;
	}

	public void setNumOfCPUs(int numOfCPUs) {
		this.numOfCPUs = numOfCPUs;
	}

	public double getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public double getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(double totalMemory) {
		this.totalMemory = totalMemory;
	}

	public double getMemoryInUse() {
		return memoryInUse;
	}

	public void setMemoryInUse(double memoryInUse) {
		this.memoryInUse = memoryInUse;
	}

	public double getTotalDiskSpace() {
		double totalDiskSpace = 0;
		for(Disk disk : getDisks()) {
			if(disk.isReady()) {
				totalDiskSpace += disk.getSpace();
			}
		}
		return totalDiskSpace;
	}

	public double getDiskSpaceInUse() {
		double diskSpaceInUse = 0;
		for(Disk disk : getDisks()) {
			if(disk.isReady()) {
				diskSpaceInUse += disk.getSpaceInUse();
			}
		}
		return diskSpaceInUse;
	}
	
	public double getFreeDiskSpace() {
		return getTotalDiskSpace() - getDiskSpaceInUse();
	}
	
	@XmlElementWrapper(name = "networkInterfaces")
	@XmlElement(name = "networkInterface", type = NetworkInterface.class)
	public List<NetworkInterface> getNetworkInterfaces() {
		return networkInterfaces;
	}

	public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {
		this.networkInterfaces = networkInterfaces;
	}

	@XmlElementWrapper(name = "disks")
	@XmlElement(name = "disk", type = Disk.class)
	public List<Disk> getDisks() {
		return disks;
	}

	public void addNetworkInterface(NetworkInterface networkInterface) {
		networkInterfaces.add(networkInterface);
	}

	public void addDisk(Disk disk) {
		disks.add(disk);
	}

	public void addDisks(Collection<Disk> disks) {
		for (Disk disk : disks) {
			addDisk(disk);
		}
	}

	public void removeDisk(Disk disk) {
		disks.remove(disk);
	}

	public void removeAllDisks() {
		disks.clear();
	}

	public void setDisks(List<Disk> disks) {
		removeAllDisks();
		addDisks(disks);
	}

	public int getNumOfDisks() {
		return disks.size();
	}

	public String getIpAddressesAsString() {
		String ipAddresses = "";
		for (NetworkInterface networkInterface : getNetworkInterfaces()) {
			String ipAddr = networkInterface.getIpAddress();
			if(!ipAddr.equals("127.0.0.1")) {
				ipAddresses += (ipAddresses.isEmpty() ? ipAddr : ", " + ipAddr);
			}
		}
		return ipAddresses;
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
	
	public Boolean isOnline() {
		return getStatus() == SERVER_STATUS.ONLINE;
	}

	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtil.filterString(getName() + getIpAddressesAsString(), filterString, caseSensitive);
	}

	/**
	 * Note: this method doesn't copy the disks. Clients should write separate code to identify added/removed/modified
	 * disks and update the server disks appropriately.
	 * 
	 * @param server
	 */
	@SuppressWarnings("unchecked")
	public void copyFrom(Server server) {
		setName(server.getName());
		setParent(server.getParent());
		setChildren((List<Entity>) server.getChildren());
		setNetworkInterfaces(server.getNetworkInterfaces());
		setNumOfCPUs(server.getNumOfCPUs());
		setCpuUsage(server.getCpuUsage());
		setTotalMemory(server.getTotalMemory());
		setMemoryInUse(server.getMemoryInUse());
		setStatus(server.getStatus());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(!(obj instanceof Server)) {
			return false;
		}
		Server server = (Server)obj;
		
		if (!(getName().equals(server.getName()) && getNumOfCPUs() == server.getNumOfCPUs()
				&& getCpuUsage() == server.getCpuUsage() && getTotalMemory() == server.getTotalMemory()
				&& getMemoryInUse() == server.getMemoryInUse() && getDisks().size() == server.getDisks().size() && getNetworkInterfaces()
				.size() == server.getNetworkInterfaces().size())) {
			return false;
		}
		
		for(Disk disk : getDisks()) {
			if (!disk.equals(GlusterCoreUtil.getEntity(server.getDisks(), disk.getName(), false))) {
				return false;
			}
		}
		
		for (NetworkInterface networkInterface : getNetworkInterfaces()) {
			if (!networkInterface.equals(GlusterCoreUtil.getEntity(server.getNetworkInterfaces(),
					networkInterface.getName(), false))) {
				return false;
			}
		}

		return true;
	}
}