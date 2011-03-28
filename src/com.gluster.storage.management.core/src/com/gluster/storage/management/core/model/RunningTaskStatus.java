package com.gluster.storage.management.core.model;

public class RunningTaskStatus extends Status {

	private String status;
	private boolean isPercentageSupported;
	private float percentCompleted;
	private String description;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isPercentageSupported() {
		return isPercentageSupported;
	}

	public void setPercentageSupported(boolean isPercentageSupported) {
		this.isPercentageSupported = isPercentageSupported;
	}

	public float getPercentCompleted() {
		return percentCompleted;
	}
	
	public void getPercentCompleted(float percentCompleted) {
		this.percentCompleted = percentCompleted;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}