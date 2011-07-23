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
package com.gluster.storage.management.gui.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {
	
	public static final String P_SHOW_CLUSTER_SELECTION_DIALOG = "show.cluster.selection.dialog";
	public static final String P_DEFAULT_CLUSTER_NAME = "default.cluster.name";
	public static final String P_DATA_SYNC_INTERVAL = "data.sync.interval";
	
	public static final String P_SERVER_CPU_CRITICAL_THRESHOLD = "server.cpu.threshold";
	public static final String P_SERVER_MEMORY_USAGE_THRESHOLD = "server.memory.threshold";
	public static final String P_DISK_SPACE_USAGE_THRESHOLD = "disk.space.threshold"; // in percentage

	public static final String P_CPU_CHART_PERIOD = "cpu.chart.period";
	public static final String P_MEM_CHART_PERIOD = "memory.chart.period";
	public static final String P_NETWORK_CHART_PERIOD = "network.chart.period";
}
