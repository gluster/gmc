package com.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="response")
public class ServerDetailsResponse<T extends Server> extends AbstractServerResponse {
	@XmlElement(name="server", type=Server.class)
	private Server server;

	public Server getServer() {
		return server;
	}
	
	@Override
	public Server getData() {
		return getServer();
	}
}
