package com.gluster.storage.management.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="response")
public class ServerListResponse<T extends Server> extends AbstractServerResponse {
	private List<T> servers;
	
	@XmlElementWrapper(name="servers")
	@XmlElement(name="server", type=Server.class)
	public List<T> getServers() {
		return servers;
	}
	
	@Override
	public List<T> getData() {
		return getServers();
	}
	
	public void setServers(List<T> data) {
		this.servers = data;
	}
}
