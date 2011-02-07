package com.gluster.storage.management.core.model;

public abstract class AbstractServerResponse implements ServerResponse {
	private Status status;

	@Override
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}