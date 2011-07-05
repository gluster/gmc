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
package com.gluster.storage.management.core.model;


public class InitDiskStatusResponse extends Status {

	public enum FORMAT_STATUS {
		IN_PROGRESS, COMPLETED, NOT_RUNNING
	};

	private String[] FORMAT_STATUS_STR = { "Inprogress", "Completed", "Notrunning" };

	private String device;
	private String message;
	private float total;
	private float completed;
	private FORMAT_STATUS status;

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
	
	public void setTotal(float total) {
		this.total = total;
	}

	public float getTotal() {
		return total;
	}

	public void setCompleted(float completed) {
		this.completed = completed;
	}

	public float getCompleted() {
		return completed;
	}

	public String getStatusStr() {
		return FORMAT_STATUS_STR[getStatus().ordinal()];
	}

	public FORMAT_STATUS getStatus() {
		return status;
	}

	public void setStatus(FORMAT_STATUS status) {
		this.status = status;
	}

}
