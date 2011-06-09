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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RunningTask {
	public enum TASK_TYPES {
		DISK_FORMAT, BRICK_MIGRATE, VOLUME_REBALANCE
	};

	public String[] TASK_TYPE_STR = { "Formatting Disk", "Brick Migration", "Volume Rebalance" };

	protected String id;
	protected TASK_TYPES type;
	protected String reference; // Server name / Volume name / Disk name
	protected String taskInfo;
	protected RunningTaskStatus status; // TODO redefine

	public RunningTask() {

	}

	public String getTaskType(TASK_TYPES type) {
		return TASK_TYPE_STR[type.ordinal()];
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TASK_TYPES getType() {
		return type;
	}

	public void setType(TASK_TYPES type) {
		this.type = type;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getTaskInfo() {
		return taskInfo;
	}

	public void setTaskInfo(String taskInfo) {
		this.taskInfo = taskInfo;
	}

	public RunningTaskStatus getStatus() {
		return status;
	}

	public void setStatus(RunningTaskStatus status) {
		this.status = status;
	}
}
