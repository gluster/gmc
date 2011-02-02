package com.gluster.storage.management.core.model;

public class NetworkInterface extends Entity {
	private String ipAddress;
	private String netMask;
	private String defaultGateway;
	private boolean isPreferred;
	
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
