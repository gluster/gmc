package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "response")
public class VolumeListResponse extends AbstractResponse {
	private List<Volume> volumes = new ArrayList<Volume>();

	public VolumeListResponse() {

	}

	public VolumeListResponse(Status status, List<Volume> volumes) {
		setStatus(status);
		setVolumes(volumes);
	}

	@XmlElementWrapper(name = "volumes")
	@XmlElement(name = "volume", type = Volume.class)
	public List<Volume> getVolumes() {
		return this.volumes;
	}

	/**
	 * @param volumes
	 *            volumes to set
	 */
	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
	}

	@Override
	@XmlTransient
	public Object getData() {
		return this.volumes;
	}
}
