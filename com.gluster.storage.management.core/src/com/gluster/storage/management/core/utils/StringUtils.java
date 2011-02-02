package com.gluster.storage.management.core.utils;

public class StringUtils {
	public static boolean filterString(String sourceString,
			String filterString, boolean caseSensitive) {
		return caseSensitive ? sourceString.contains(filterString)
				: sourceString.toLowerCase().contains(
						filterString.toLowerCase());
	}
}
