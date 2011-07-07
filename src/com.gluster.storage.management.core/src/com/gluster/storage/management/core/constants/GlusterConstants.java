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
package com.gluster.storage.management.core.constants;

import java.util.List;

import com.gluster.storage.management.core.utils.StringUtil;

/**
 *
 */
public class GlusterConstants {
	public enum VOLUME_LOG_LEVELS {
		EMERGENCY, ALERT, CRITICAL, ERROR, WARNING, NOTICE, INFO, DEBUG, TRACE
	};

	public static final List<String> VOLUME_LOG_LEVELS_ARR = StringUtil.enumToArray(VOLUME_LOG_LEVELS.values());
	public static final String FSTYPE_EXT_3 = "ext3";
	public static final String FSTYPE_EXT_4 = "ext4";
	public static final String FSTYPE_XFS = "xfs";
}
