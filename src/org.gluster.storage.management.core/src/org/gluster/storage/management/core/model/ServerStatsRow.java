/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement(name="row")
public class ServerStatsRow {
	private Long timestamp;
	private List<Double> usageData;

	public ServerStatsRow() {
	}
	
	public ServerStatsRow(ServerStatsRow newRow) {
		copyFrom(newRow);
	}

	private void copyFrom(ServerStatsRow newRow) {
		setTimestamp(newRow.getTimestamp());

		List<Double> myData = new ArrayList<Double>(newRow.getUsageData().size());
		for(Double dataElement : newRow.getUsageData()) {
			myData.add(dataElement);
		}
		setUsageData(myData);
	}

	@XmlElement(name="t")
	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public void setUsageData(List<Double> usageData) {
		this.usageData = usageData;
	}

	@XmlElement(name="v")
	public List<Double> getUsageData() {
		return usageData;
	}
}
