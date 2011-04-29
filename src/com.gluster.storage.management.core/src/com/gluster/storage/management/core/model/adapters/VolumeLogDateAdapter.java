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

package com.gluster.storage.management.core.model.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter class used for converting timestamp from Gluster volume log files to Date object.
 */
public class VolumeLogDateAdapter extends XmlAdapter<String, Date> {
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Date unmarshal(String input) throws Exception {
		input = input.trim();
		if(input.length() > DATE_FORMAT.length()) {
			input = input.substring(0, DATE_FORMAT.length());
		}
		return dateFormatter.parse(input);
	}

	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public String marshal(Date input) throws Exception {
		return dateFormatter.format(input);
	}
}
