/**
 * TaskStatus.java
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
 */
package com.gluster.storage.management.core.model;

public class TaskStatus extends Status {

	private boolean isPercentageSupported;
	private float percentCompleted;
	private String description;

	public TaskStatus() {

	}

	public TaskStatus(Status status) {
		super(status.getCode(), status.getMessage());
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

	public void setPercentCompleted(float percentCompleted) {
		this.percentCompleted = percentCompleted;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
