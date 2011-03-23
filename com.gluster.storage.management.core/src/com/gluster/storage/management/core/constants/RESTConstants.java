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
	// Constants related to Volumes Resource
	public static final String PATH_RESOURCE_VOLUMES = "/cluster/volumes";
	public static final String FORM_PARAM_OPERATION = "operation";
	public static final String FORM_PARAM_VALUE_START = "start";
	public static final String FORM_PARAM_VALUE_STOP = "stop";
	public static final String PATH_PARAM_VOLUME_NAME = "volumeName";
	public static final String PATH_PARAM_RUNNING_TASKS = "/cluster/runningtasks";
	public static final String SUBRESOURCE_DEFAULT_OPTIONS = "defaultoptions";
}