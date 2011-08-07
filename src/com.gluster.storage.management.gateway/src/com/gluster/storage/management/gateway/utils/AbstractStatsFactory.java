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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;

/**
 *
 */
@Component
public abstract class AbstractStatsFactory implements StatsFactory {
	@Autowired
	protected ServerUtil serverUtil;

	private Logger logger = Logger.getLogger(AbstractStatsFactory.class);
	
	protected ServerStats getFirstOnlineServerStats(List<String> serverNames, String period,
			boolean removeServerOnError, boolean removeOnlineServer) {
		for(int i = serverNames.size() - 1; i >= 0; i--) {
			String serverName = serverNames.get(i);
			try {
				ServerStats stats = fetchStats(serverName, period);
				if(removeOnlineServer) {
					serverNames.remove(serverName);
				}
				return stats;
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
		
		int rowCount = aggregatedStats.getMetadata().getRowCount();
		int columnCount = aggregatedStats.getMetadata().getLegend().size();
		int[][] dataCount = initDataCountArray(rowCount, columnCount);
		
		for (String serverName : serverNames) {
			try {
				// fetch the stats and add to aggregated stats
				addServerStats(fetchStats(serverName, period), aggregatedStats, dataCount);
			} catch(Exception e) {
				// server might be offline - continue with next one
				logger.warn("Couldn't fetch performance stats from server [" + serverName + "]!", e);
				continue;
			}
		}
		
		averageAggregatedStats(aggregatedStats, dataCount);
	}

	/**
	 * 
	 * @param statsToBeAdded
	 * @param targetStats
	 * @param dataCount Each element of this matrix will be incremented for every valid element added
	 * @return
	 */
	protected List<ServerStatsRow> addServerStats(ServerStats statsToBeAdded, ServerStats targetStats, int[][] dataCount) {
		List<ServerStatsRow> serverStatsRows = statsToBeAdded.getRows();
		for (int rowNum = 0; rowNum < serverStatsRows.size() && rowNum < targetStats.getMetadata().getRowCount()
				&& rowNum < dataCount.length; rowNum++) {
			ServerStatsRow row = serverStatsRows.get(rowNum);
			List<Double> rowData = row.getUsageData();
			
			List<Double> aggregatedStatsRowData = targetStats.getRows().get(rowNum).getUsageData();
			for(int i = 1; i < targetStats.getMetadata().getLegend().size(); i++) {
				// Add the data
				Double data = rowData.get(i);
				if(!data.isNaN()) {
					// data is available. add it.
					aggregatedStatsRowData.set(i, aggregatedStatsRowData.get(i) + data);
					// increment record count. this will be used for calculating average of aggregated data.
					dataCount[rowNum][i]++;
				}
			}
		}
		return serverStatsRows;
	}

	protected void averageAggregatedStats(ServerStats aggregatedStats, int[][] dataCount) {
		List<ServerStatsRow> rows = aggregatedStats.getRows();
		for(int rowNum = 0; rowNum < rows.size(); rowNum++) {
			List<Double> data = rows.get(rowNum).getUsageData();
			for(int columnNum = 0; columnNum < data.size(); columnNum++) {
				data.set(columnNum, data.get(columnNum) / dataCount[rowNum][columnNum]);
			}
		}
	}

	protected int[][] initDataCountArray(int rowCount, int columnCount) {
		int[][] dataCount = new int[rowCount][columnCount];
		// initialize all data counts to 1
		for(int rowNum = 0; rowNum < rowCount; rowNum++) {
			for(int columnNum = 0; columnNum < columnCount; columnNum++) {
				dataCount[rowNum][columnNum] = 1;
			}
		}
		return dataCount;
	}
	
	@Override
	public ServerStats fetchAggregatedStats(List<String> serverNames, String period) {
		if(serverNames == null || serverNames.size() == 0) {
			throw new GlusterRuntimeException("No server names passed to fetchAggregaredStats!");
		}
		
		ServerStats firstServerStats = getFirstOnlineServerStats(serverNames, period, true, true);

		ServerStats aggregatedStats = new ServerStats(firstServerStats);
		aggregateStats(serverNames, aggregatedStats, period);
		return aggregatedStats;
	}
	
	@Override
	public ServerStats fetchStats(String serverName, String period, String...args) {
		String argsStr = "";
		for (String arg : args) {
			if(arg != null) {
				argsStr += " " + arg;
			}
		}
		return serverUtil.executeScriptOnServer(true, serverName, getStatsScriptName() + argsStr + " "
				+ period, ServerStats.class);
	}

	public abstract String getStatsScriptName();
}
