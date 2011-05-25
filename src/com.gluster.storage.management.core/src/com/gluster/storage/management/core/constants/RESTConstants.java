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
	// Volumes Resource
	public static final String RESOURCE_PATH_VOLUMES = "/cluster/volumes";
	public static final String SUBRESOURCE_DEFAULT_OPTIONS = "defaultoptions";
	public static final String SUBRESOURCE_OPTIONS = "options";
	public static final String SUBRESOURCE_LOGS = "logs";
	public static final String SUBRESOURCE_DOWNLOAD = "download";
	public static final String SUBRESOURCE_DISKS = "disks";
	
	
	public static final String FORM_PARAM_DISKS = "disks";
	public static final String FORM_PARAM_OPERATION = "operation";
	public static final String FORM_PARAM_VALUE_START = "start";
	public static final String FORM_PARAM_VALUE_STOP = "stop";
	public static final String FORM_PARAM_VALUE_PAUSE = "pause";
	public static final String FORM_PARAM_VALUE_STATUS = "status";
	public static final String FORM_PARAM_OPTION_KEY = "key";
	public static final String FORM_PARAM_OPTION_VALUE = "value";
	public static final String FORM_PARAM_SOURCE = "source";
	public static final String FORM_PARAM_TARGET = "target";
	public static final String FORM_PARAM_BRICKS = "bricks";
	
	public static final String PATH_PARAM_VOLUME_NAME = "volumeName";
	public static final String FORM_PARAM_DELETE_OPTION = "value";
	
	public static final String QUERY_PARAM_DISK_NAME = "diskName";
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
	public static final String QUERY_PARAM_KNOWN_SERVER = "knownServer";

	// Running tasks resource
	public static final String RESOURCE_PATH_RUNNING_TASKS = "/cluster/runningtasks";
	public static final String RESOURCE_PATH_ALERTS = "/cluster/alerts";
}
