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
package com.gluster.storage.management.gateway.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.NetworkInterface;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;

/**
 *
 */
@Component
public class NetworkStatsFactory extends AbstractStatsFactory {
	private static final Logger logger = Logger.getLogger(NetworkStatsFactory.class);
	private static final String NETWORK_STATS_SCRIPT = "get_rrd_net_details.py";
	private int[][] dataCount;

	@Override
	public String getStatsScriptName() {
		return NETWORK_STATS_SCRIPT;
	}
	
	@Override
	protected ServerStats getFirstOnlineServerStats(List<String> serverNames, String period,
			boolean removeServerOnError, boolean removeOnlineServer) {
		ServerStats firstOnlineServerStats = null;
		for(int i = serverNames.size() - 1; i >= 0; i--) {
			String serverName = serverNames.get(i);
			Server server = new Server(serverName);
			serverUtil.fetchServerDetails(server);
			try {
				for(NetworkInterface networkInterface : server.getNetworkInterfaces()) {
					ServerStats stats = fetchStats(serverName, period, networkInterface.getName());
					if(firstOnlineServerStats == null) {
						firstOnlineServerStats = stats;
						int rowCount = firstOnlineServerStats.getMetadata().getRowCount();
						int columnCount = firstOnlineServerStats.getMetadata().getLegend().size();
						dataCount = initDataCountArray(rowCount, columnCount);
					} else {
						addServerStats(stats, firstOnlineServerStats, dataCount);
					}
				}
				
				if(removeOnlineServer) {
					serverNames.remove(serverName);
				}
				return firstOnlineServerStats;
			} catch(Exception e) {
				// server might be offline - continue with next one
				logger.warn("Couldn't fetch stats from server [" + serverName + "]!", e);
				if(removeServerOnError) {
					serverNames.remove(serverName);
				}
				continue;
			}
		}
		throw new GlusterRuntimeException("All servers offline!");
	}
	
	protected void aggregateStats(List<String> serverNames, ServerStats aggregatedStats, String period) {
		if(serverNames.isEmpty()) {
			return;
		}
		
		for (String serverName : serverNames) {
			try {
				Server server = new Server(serverName);
				serverUtil.fetchServerDetails(server);
				
				for (NetworkInterface networkInterface : server.getNetworkInterfaces()) {
					// fetch the stats and add to aggregated stats
					addServerStats(fetchStats(serverName, period, networkInterface.getName()), aggregatedStats, dataCount);
				}
			} catch(Exception e) {
				// server might be offline - continue with next one
				logger.warn("Couldn't fetch Network stats from server [" + serverName + "]!", e);
				continue;
			}
		}
		
		averageAggregatedStats(aggregatedStats, dataCount);
	}
	
	@Override
	public ServerStats fetchStats(String serverName, String period, String... args) {
		ServerStats stats = super.fetchStats(serverName, period, args);

		// the data returned by rrd contains "bytes/sec". Update the stats object to represent KiB/s
		for(ServerStatsRow row : stats.getRows()) {
			List<Double> data = row.getUsageData();
			for (int i = 0; i < data.size(); i++) {
				Double val = data.get(i);
				data.set(i, val / 1024);
			}
		}
		
		return stats;
	}
}
