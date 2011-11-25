package org.gluster.storage.management.core.response;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.gluster.storage.management.core.model.GlusterServer;
import org.gluster.storage.management.core.model.Status;


@XmlRootElement(name = "response")
public class GlusterServerResponse extends AbstractResponse {
	private GlusterServer glusterServer;
	
	public GlusterServerResponse() {
	}
	
	public GlusterServerResponse(Status status, GlusterServer server) {
		setStatus(status);
		setGlusterServer(server);
	}
	
	public GlusterServer getGlusterServer() {
		return glusterServer;
	}

	public void setGlusterServer(GlusterServer glusterServer) {
		this.glusterServer = glusterServer;
	}

	@XmlTransient
	@Override
	public GlusterServer getData() {
		return getGlusterServer();
	}
	
}
