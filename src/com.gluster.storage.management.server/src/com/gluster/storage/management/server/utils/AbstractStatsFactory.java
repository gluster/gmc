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
package com.gluster.storage.management.server.utils;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;
import com.gluster.storage.management.core.model.Status;

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
		for (int rowNum = 0; rowNum < serverStatsRows.size()
				&& rowNum < targetStats.getMetadata().getRowCount(); rowNum++) {
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
		Object output = serverUtil.executeOnServer(true, serverName, getStatsScriptName() + argsStr + " " + period, ServerStats.class);
		//String cpuUsageData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <xport> <meta> <start>1310468100</start> <step>300</step> <end>1310471700</end> <rows>13</rows> <columns>3</columns> <legend> <entry>user</entry> <entry>system</entry> <entry>total</entry> </legend> </meta> <data> <row><t>1310468100</t><v>2.23802952e-1</v><v>4.3747778209e-01</v><v>6.6128073384e-01</v></row> <row><t>1310468400</t><v>2.3387347338e-01</v><v>4.4642717442e-01</v><v>6.8030064780e-01</v></row> <row><t>1310468700</t><v>5.5043873220e+00</v><v>6.2462376636e+00</v><v>1.1750624986e+01</v></row> <row><t>1310469000</t><v>2.4350593653e+01</v><v>2.6214585217e+01</v><v>5.0565178869e+01</v></row> <row><t>1310469300</t><v>4.0786489953e+01</v><v>4.6784713828e+01</v><v>8.7571203781e+01</v></row> <row><t>1310469600</t><v>4.1459955508e+01</v><v>5.2546309044e+01</v><v>9.4006264551e+01</v></row> <row><t>1310469900</t><v>4.2312286165e+01</v><v>5.2390588332e+01</v><v>9.4702874497e+01</v></row> <row><t>1310470200</t><v>4.2603794982e+01</v><v>5.1598861493e+01</v><v>9.4202656475e+01</v></row> <row><t>1310470500</t><v>3.8238751290e+01</v><v>4.5312089966e+01</v><v>8.3550841256e+01</v></row> <row><t>1310470800</t><v>1.7949961224e+01</v><v>2.1282058418e+01</v><v>3.9232019642e+01</v></row> <row><t>1310471100</t><v>1.2330371421e-01</v><v>4.6347832868e-01</v><v>5.8678204289e-01</v></row> <row><t>1310471400</t><v>1.6313260492e-01</v><v>5.4088119561e-01</v><v>7.0401380052e-01</v></row> <row><t>1310471700</t><v>NaN</v><v>NaN</v><v>NaN</v></row> </data> </xport>";
		//Object output = unmarshal(ServerStats.class, cpuUsageData, false);
		if(output instanceof Status) {
			throw new GlusterRuntimeException(((Status)output).toString());
		}
		return (ServerStats) output;
	}

	public abstract String getStatsScriptName();
}
