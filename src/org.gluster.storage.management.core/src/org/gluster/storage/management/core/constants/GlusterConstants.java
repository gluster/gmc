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
package org.gluster.storage.management.core.constants;

import java.util.List;

import org.gluster.storage.management.core.utils.StringUtil;


/**
 *
 */
public class GlusterConstants {
	public enum VOLUME_LOG_LEVELS {
		EMERGENCY, ALERT, CRITICAL, ERROR, WARNING, NOTICE, INFO, DEBUG, TRACE
	};

	public static final List<String> VOLUME_LOG_LEVELS_ARR = StringUtil.enumToArray(VOLUME_LOG_LEVELS.values());
	public static final String FSTYPE_DEFAULT = "default";
	public static final String FSTYPE_EXT_3 = "ext3";
	public static final String FSTYPE_EXT_4 = "ext4";
	public static final String FSTYPE_XFS = "xfs";
	
	public static final String ON = "on";
	public static final String OFF = "off";
	public static final String NONE = "none";
	
	public static final String STATS_PERIOD_1DAY = "1d";
	public static final String STATS_PERIOD_1WEEK = "1w";
	public static final String STATS_PERIOD_1MONTH = "1m";
	public static final String STATS_PERIOD_1YEAR = "1y";
}
