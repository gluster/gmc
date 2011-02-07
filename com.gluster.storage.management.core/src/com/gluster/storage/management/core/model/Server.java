package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.utils.StringUtils;

@XmlRootElement(name="server")
public class Server extends Entity {
	private List<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
	private int numOfCPUs;
	private double cpuUsage;
	private double totalMemory;
	private double memoryInUse;
	private double totalDiskSpace = 0;
	private double diskSpaceInUse = 0;
	private List<Disk> disks = new ArrayList<Disk>();

	public Server() {
		
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
		return totalDiskSpace;
	}

	public double getDiskSpaceInUse() {
		return diskSpaceInUse;
	}

	@XmlElementWrapper(name="networkInterfaces")
	@XmlElement(name="networkInterface", type=NetworkInterface.class)
	public List<NetworkInterface> getNetworkInterfaces() {
		return networkInterfaces;
	}

	public void setNetworkInterfaces(List<NetworkInterface> networkInterfaces) {
		this.networkInterfaces = networkInterfaces;
	}

	@XmlElementWrapper(name="disks")
	@XmlElement(name="disk", type=Disk.class)
	public List<Disk> getDisks() {
		return disks;
	}

	public void addDisk(Disk disk) {
		if (disks.add(disk)) {
			totalDiskSpace += disk.getSpace();
			diskSpaceInUse += disk.getSpaceInUse();
		}
	}

	public void addDisks(List<Disk> disks) {
		for(Disk disk : disks) {
			addDisk(disk);
		}
	}

	public void removeDisk(Disk disk) {
		if (disks.remove(disk)) {
			totalDiskSpace -= disk.getSpace();
			diskSpaceInUse -= disk.getSpaceInUse();
		}
	}

	public void removeAllDisks() {
		disks.clear();
		totalDiskSpace = 0;
		diskSpaceInUse = 0;
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
			ipAddresses += (ipAddresses.isEmpty() ? ipAddr : ", " + ipAddr);
		}
		return ipAddresses;
	}

	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtils.filterString(getName() + getIpAddressesAsString(), filterString, caseSensitive);
	}
}
