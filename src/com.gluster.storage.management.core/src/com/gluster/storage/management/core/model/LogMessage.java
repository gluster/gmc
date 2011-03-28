/*******************************************************************************
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.gluster.storage.management.core.model;

import java.util.Date;

import com.gluster.storage.management.core.utils.StringUtil;

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
		return StringUtil.filterString(getSeverity() + getTimestamp() + getDisk().getServerName()
				+ getDisk().getQualifiedName() + getMessage(), filterString, caseSensitive);
	}
}