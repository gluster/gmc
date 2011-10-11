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
package com.gluster.storage.management.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;

public class DateUtil {

	/**
	 * Formats given date in pure date format (without time component) using default format
	 * {@link CoreConstants#PURE_DATE_FORMAT}
	 * 
	 * @param inputDate
	 *            Date to be formatted
	 * @return Formatted String representation of the given date
	 */
	public static final String formatDate(Date inputDate) {
		return dateToString(inputDate, CoreConstants.PURE_DATE_FORMAT);
	}

	/**
	 * Formats given date in pure time format (without date component) using default format
	 * {@link CoreConstants#PURE_TIME_FORMAT}
	 * 
	 * @param inputDate
	 *            Date to be formatted
	 * @return Formatted String representation of the given date
	 */
	public static final String formatTime(Date inputDate) {
		return dateToString(inputDate, CoreConstants.PURE_TIME_FORMAT);
	}

	/**
	 * Converts given date object to string by formatting it in given format
	 * 
	 * @param date
	 *            Date to be formatted
	 * @param dateFormat
	 *            Date format
	 * @return String representation of the given Date
	 */
	public static final String dateToString(Date date, String dateFormat) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		return dateFormatter.format(date);
	}

	/**
	 * Converts given date object to string by formatting it using default date format
	 * {@link CoreConstants#DATE_WITH_TIME_FORMAT}
	 * 
	 * @param date
	 *            Date to be formatted
	 * @param dateFormat
	 *            Date format
	 * @return String representation of the given Date
	 */
	public static final String dateToString(Date date) {
		return dateToString(date, CoreConstants.DATE_WITH_TIME_FORMAT);
	}

	/**
	 * Converts given string to date using the given date format
	 * 
	 * @param input
	 *            Input string
	 * @param dateFormat
	 *            The date format to be used
	 * @return Date object
	 */
	public static final Date stringToDate(String input, String dateFormat) {
		try {
			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
			return dateFormatter.parse(input);
		} catch (ParseException e) {
			throw new GlusterRuntimeException("Error trying to parse string [" + input + "] in to date using format ["
					+ dateFormat + "]", e);
		}
	}

	/**
	 * Converts given string to date using the default date format {@link CoreConstants#DATE_WITH_TIME_FORMAT}
	 * 
	 * @param input
	 *            Input string
	 * @return Date object
	 */
	public static final Date stringToDate(String input) {
		return stringToDate(input, CoreConstants.DATE_WITH_TIME_FORMAT);
	}
	
	public static final Date getDate(int year, int month, int day, int hour, int min, int sec, int msec) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month-1, day, hour, min, sec);
		calendar.set(Calendar.MILLISECOND, msec);
		return calendar.getTime();
	}
}
