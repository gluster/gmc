package com.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.model.Volume;

@XmlRootElement(name = "volumes")
public class VolumeListResponse {
	private List<Volume> volumes = new ArrayList<Volume>();

	public VolumeListResponse() {

	}

	public VolumeListResponse(List<Volume> volumes) {
		setVolumes(volumes);
	}

	@XmlElement(name = "volume", type = Volume.class)
	public List<Volume> getVolumes() {
		return this.volumes;
	}

	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
	}
}
