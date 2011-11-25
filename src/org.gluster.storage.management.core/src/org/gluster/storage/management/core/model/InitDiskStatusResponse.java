/*******************************************************************************
 * InitDiskStatusResponse.java
 *
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
package org.gluster.storage.management.core.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="response")
public class InitDiskStatusResponse {

	public enum FORMAT_STATUS {
		IN_PROGRESS, COMPLETED, NOT_RUNNING
	};

	private String[] FORMAT_STATUS_STR = { "In Progress", "Completed", "Not Running" };

	private String device;
	private String message;
	private float totalBlocks;
	private float completedBlocks;
	private FORMAT_STATUS formatStatus;

	public InitDiskStatusResponse() {

	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getDevice() {
		return device;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

	public void setTotalBlocks(float totalBlocks) {
		this.totalBlocks = totalBlocks;
	}

	public float getTotalBlocks() {
		return totalBlocks;
	}

	public void setCompletedBlocks(float completedBlocks) {
		this.completedBlocks = completedBlocks;
	}

	public float getCompletedBlocks() {
		return completedBlocks;
	}

	public String getFormatStatusStr() {
		return FORMAT_STATUS_STR[getFormatStatus().ordinal()];
	}

	public FORMAT_STATUS getFormatStatus() {
		return formatStatus;
	}

	public void setFormatStatus(FORMAT_STATUS status) {
		this.formatStatus = status;
	}

}
