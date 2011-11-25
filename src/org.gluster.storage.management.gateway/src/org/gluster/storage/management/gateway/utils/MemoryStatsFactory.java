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
package org.gluster.storage.management.gateway.utils;

import java.util.List;

import org.gluster.storage.management.core.model.ServerStats;
import org.gluster.storage.management.core.model.ServerStatsRow;
import org.springframework.stereotype.Component;


/**
 *
 */
@Component
public class MemoryStatsFactory extends AbstractStatsFactory {
	
	private static final String MEM_STATS_SCRIPT = "get_rrd_memory_details.py";

	@Override
	public String getStatsScriptName() {
		return MEM_STATS_SCRIPT;
	}
	
	@Override
	public ServerStats fetchStats(String serverName, String period, String... args) {
		ServerStats stats = super.fetchStats(serverName, period, args);
		
		// stats returned by rrd script contains five columns - user, free, cache, buffer, total
		// out of this, the "user" memory includes cached and buffer. We remove them to get the 
		// actual memory used by "user"
		for(ServerStatsRow row : stats.getRows()) {
			List<Double> data = row.getUsageData();
			Double user = data.get(0);
			Double free = data.get(1);
			Double cache = data.get(2);
			Double buffer = data.get(3);
			Double total = data.get(4);
			
			Double actualUser = user - cache - buffer;
			
			// convert all figures from bytes to percentages
			data.set(0, (actualUser * 100) / total);
			data.set(1, (free * 100) / total);
			data.set(2, (cache * 100) / total);
			data.set(3, (buffer * 100) / total);
			data.set(4, (total * 100) / total);
		}
		
		return stats;
	}
}
