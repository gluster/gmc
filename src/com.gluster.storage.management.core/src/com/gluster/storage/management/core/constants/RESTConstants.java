/**
 * RESTConstants.java
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
package com.gluster.storage.management.core.constants;

/**
 * All constants related to the Gluster REST server and client
 */
public class RESTConstants {
	// REST Resource paths
	public static final String RESOURCE_PATH_USERS = "/users";
	public static final String RESOURCE_PATH_CLUSTERS = "/clusters";
	public static final String RESOURCE_PATH_DISCOVERED_SERVERS = "/discoveredservers";
	public static final String RESOURCE_PATH_KEYS = "/keys";
	
	// REST Resource names
	public static final String RESOURCE_VOLUMES = "volumes";
	public static final String RESOURCE_DEFAULT_OPTIONS = "options";
	public static final String RESOURCE_OPTIONS = "options";
	public static final String RESOURCE_LOGS = "logs";
	public static final String RESOURCE_DOWNLOAD = "download";
	public static final String RESOURCE_BRICKS = "bricks";
	public static final String RESOURCE_DISKS = "disks";
	public static final String RESOURCE_ALERTS = "alerts";
	public static final String RESOURCE_SERVERS = "servers";
	public static final String RESOURCE_TASKS = "tasks";
	public static final String RESOURCE_KEYS = "keys";
	public static final String RESOURCE_STATISTICS = "statistics";
	public static final String RESOURCE_USERS = "users";
	
	public static final String TASK_START = "start";
	public static final String TASK_PAUSE = "pause";
	public static final String TASK_RESUME = "resume";
	public static final String TASK_STOP = "stop";
	public static final String TASK_COMMIT = "commit";
	public static final String TASK_STATUS = "status";
	public static final String TASK_DELETE = "delete";
	public static final String TASK_REBALANCE_START = "rebalanceStart";
	public static final String TASK_REBALANCE_STATUS = "rebalanceStatus";
	public static final String TASK_REBALANCE_STOP = "rebalanceStop";

	public static final String FORM_PARAM_VOLUME_NAME = "name";
	public static final String FORM_PARAM_VOLUME_TYPE = "volumeType";
	public static final String FORM_PARAM_TRANSPORT_TYPE = "transportType";
	public static final String FORM_PARAM_REPLICA_COUNT = "replicaCount";
	public static final String FORM_PARAM_STRIPE_COUNT = "stripeCount";
	public static final String FORM_PARAM_BRICKS = "bricks";
	public static final String FORM_PARAM_ACCESS_PROTOCOLS = "accessProtocols";
	public static final String FORM_PARAM_VOLUME_OPTIONS = "options";
	public static final String FORM_PARAM_FSTYPE = "fsType";
	public static final String FORM_PARAM_CIFS_ENABLE = "enableCifs";
	public static final String FORM_PARAM_CIFS_USERS = "cifsUsers";
	public static final String FORM_PARAM_CIFS_CONFIG = "cifsConfig";
	
	public static final String FORM_PARAM_CLUSTER_NAME = "clusterName";
	public static final String FORM_PARAM_SERVER_NAME = "serverName";
	public static final String FORM_PARAM_DISKS = "disks";
	public static final String FORM_PARAM_OPERATION = "operation";
	public static final String FORM_PARAM_VALUE_STATUS = "status";
	public static final String FORM_PARAM_OPTION_KEY = "key";
	public static final String FORM_PARAM_OPTION_VALUE = "value";
	public static final String FORM_PARAM_SOURCE = "source";
	public static final String FORM_PARAM_TARGET = "target";
	public static final String FORM_PARAM_AUTO_COMMIT = "autoCommit";
	public static final String FORM_PARAM_FIX_LAYOUT = "fix-layout";
	public static final String FORM_PARAM_MIGRATE_DATA = "migrate-data";
	public static final String FORM_PARAM_FORCED_DATA_MIGRATE = "forced-data-migrate";
	public static final String FORM_PARAM_OLD_PASSWORD = "oldPassword";
	public static final String FORM_PARAM_NEW_PASSWORD = "newPassword";
	
	public static final String PATH_PARAM_FORMAT = "format";
	public static final String PATH_PARAM_VOLUME_NAME = "volumeName";
	public static final String PATH_PARAM_CLUSTER_NAME = "clusterName";
	public static final String PATH_PARAM_SERVER_NAME = "serverName";
	public static final String PATH_PARAM_TASK_ID = "taskId";
	public static final String PATH_PARAM_DISK_NAME = "diskName";
	public static final String PATH_PARAM_USER = "user";
	
	public static final String QUERY_PARAM_BRICK_NAME = "brickName";
	public static final String QUERY_PARAM_DISKS = "disks";
	public static final String QUERY_PARAM_BRICKS = "bricks";
	public static final String QUERY_PARAM_LINE_COUNT = "lineCount";
	public static final String QUERY_PARAM_VOLUME_NAME = "volumeName";
	public static final String QUERY_PARAM_DELETE_OPTION = "deleteOption";
	public static final String QUERY_PARAM_LOG_SEVERITY = "severity";
	public static final String QUERY_PARAM_FROM_TIMESTAMP = "fromTimestamp";
	public static final String QUERY_PARAM_TO_TIMESTAMP = "toTimestamp";
	public static final String QUERY_PARAM_DOWNLOAD = "download";
	public static final String QUERY_PARAM_SERVER_NAME = "serverName";
	public static final String QUERY_PARAM_DETAILS = "details";
	public static final String QUERY_PARAM_TYPE = "type";
	public static final String QUERY_PARAM_PERIOD = "period";
	public static final String QUERY_PARAM_INTERFACE = "interface";
	public static final String QUERY_PARAM_MAX_COUNT = "maxCount";
	public static final String QUERY_PARAM_NEXT_TO = "nextTo";
	
	public static final String STATISTICS_TYPE_CPU = "cpu";
	public static final String STATISTICS_TYPE_NETWORK = "network";
	public static final String STATISTICS_TYPE_MEMORY = "memory";
	
	public static final String FORMAT_XML = "xml";
	public static final String FORMAT_JSON = "json";
}
