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
package org.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 */
@XmlRootElement(name="xport")
public class ServerStats {
	private StatsMetadata metadata;
	private List<ServerStatsRow> rows;

	public ServerStats() {
	}
	
	public ServerStats(ServerStats newStats) {
		copyFrom(newStats);
	}

	public void setRows(List<ServerStatsRow> rows) {
		this.rows = rows;
	}

	@XmlElementWrapper(name="data")
	@XmlElement(name="row", type=ServerStatsRow.class)
	public List<ServerStatsRow> getRows() {
		return rows;
	}

	public void setMetadata(StatsMetadata metadata) {
		this.metadata = metadata;
	}

	@XmlElement(name="meta")
	public StatsMetadata getMetadata() {
		return metadata;
	}
	
	public void copyFrom(ServerStats newStats) {
		setMetadata(newStats.getMetadata());
		
		List<ServerStatsRow> newRows = newStats.getRows();
		int rowCount = newRows.size();
		
		rows = new ArrayList<ServerStatsRow>(rowCount); 
		for(ServerStatsRow newRow : newRows) {
			rows.add(new ServerStatsRow(newRow));
		}
	}
}