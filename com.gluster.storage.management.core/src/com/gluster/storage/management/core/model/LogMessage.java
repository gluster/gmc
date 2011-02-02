package com.gluster.storage.management.core.model;

import java.util.Date;

import com.gluster.storage.management.core.utils.StringUtils;

public class LogMessage implements Filterable {
	private Date timestamp;
	private Disk disk;
	private String severity;
	private String message;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Disk getDisk() {
		return disk;
	}

	public void setDisk(Disk disk) {
		this.disk = disk;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LogMessage(Date timestamp, Disk disk, String severity, String message) {
		setTimestamp(timestamp);
		setDisk(disk);
		setSeverity(severity);
		setMessage(message);
	}

	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtils.filterString(getSeverity() + getTimestamp() + getDisk().getServer().getName()
				+ getDisk().getQualifiedName() + getMessage(), filterString, caseSensitive);
	}
}
