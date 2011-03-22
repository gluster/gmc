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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.gluster.storage.management.core.utils.ProcessResult;

@XmlRootElement(name = "status")
public class Status {
	public static final int STATUS_CODE_SUCCESS = 0;
	public static final int STATUS_CODE_FAILURE = 1;
	public static final Status STATUS_SUCCESS = new Status(STATUS_CODE_SUCCESS, "Success");
	public static final Status STATUS_FAILURE = new Status(STATUS_CODE_FAILURE, "Failure");

	// public static final Status

	private Integer code;
	private String message;

	public Status() {
	}

	public boolean isSuccess() {
		return code == STATUS_CODE_SUCCESS;
	}

	public Status(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Status(ProcessResult result) {
		this.code = result.getExitValue();
		this.message = result.getOutput();
	}

	@XmlElement(name = "code", type = Integer.class)
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer executionStatus) {
		this.code = executionStatus;
	}

	@XmlElement
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return (isSuccess() ? "Success" : "Failure [" + getCode() + "]") + ": " + getMessage();
	}
}