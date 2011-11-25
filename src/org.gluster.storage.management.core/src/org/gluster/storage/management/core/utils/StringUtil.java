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
package org.gluster.storage.management.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StringUtil {
	public static boolean filterString(String sourceString, String filterString, boolean caseSensitive) {
		return caseSensitive ? sourceString.contains(filterString) : sourceString.toLowerCase().contains(
				filterString.toLowerCase());
	}

	public static String removeSpaces(String str) {
		return str.replaceAll("\\s+", "");
	}

	public static String collectionToString(Collection<? extends Object> list, String delimiter) {
		if (list.size() == 0) {
			return "";
		}
		StringBuilder output = new StringBuilder();
		for (Object element : list) {
			output.append(element.toString()).append(delimiter);
		}
		String outputStr = output.toString();
		int endIndex = outputStr.length() - delimiter.length();
		return outputStr.substring(0, endIndex);
	}

	public static <T extends Enum<T>> List<String> enumToArray(T[] values) {
		List<String> enumAsArray = new ArrayList<String>();
		for (T value : values) {
			enumAsArray.add(value.toString());
		}
		return enumAsArray;
	}
	
	/**
	 * Extracts a list from a string by splitting it on given delimiter 
	 * @param input the input string
	 * @return A {@link List} of extracted tokens
	 */
	public static List<String> extractList(String input, String delim) {
		String[] arr = input.split(delim);
		List<String> output = new ArrayList<String>();
		for(String str : arr) {
			String brick = str.trim();
			if(!brick.isEmpty()) {
				output.add(brick);
			}
		}
		return output;
	}

	/**
	 * Extracts a map from a string by splitting it on the given primary and secondary delimiter. e.g. The input string
	 * <i>k1=v1,k2=v2,k3=v3</i> will yield the following map:<br>
	 * k1 -> v1<br>
	 * k2 -> v2<br>
	 * k3 -> v3<br>
	 * where <b>,</b> is the primary delimiter and <b>=</b> is the secondary delimiter.
	 * 
	 * @param input
	 * @param majorDelim
	 * @param minorDelim
	 * @return Map of key value pairs
	 */
	public static Map<String, String> extractMap(String input, String majorDelim, String minorDelim) {
		String[] arr = input.split(majorDelim);
		Map<String, String> output = new LinkedHashMap<String, String>();
		for(String str : arr) {
			String[] elements = str.split(minorDelim);
			if(elements.length == 2) {
				String key = elements[0].trim();
				String value = elements[1].trim();
				if(!key.isEmpty() && !value.isEmpty()) {
					output.put(key, value);
				}
			}
		}
		return output;
	}
	
	/**
	 * Extract value of given token from given line. It is assumed that the token, if present, will be of the following
	 * form: <code>token: value</code>
	 * 
	 * @param line
	 *            Line to be analyzed
	 * @param token
	 *            Token whose value is to be extracted
	 * @return Value of the token, if present in the line
	 */
	public static String extractToken(String line, String token) {
		if (line.contains(token)) {
			return line.split(token)[1].trim();
		}
		return null;
	}
}
