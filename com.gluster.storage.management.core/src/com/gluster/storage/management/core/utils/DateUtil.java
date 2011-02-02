package com.gluster.storage.management.core.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static final String formatDate(Date inputDate) {
		DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		return formatter.format(inputDate);
	}
	
	public static final String formatTime(Date inputDate) {
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
		return formatter.format(inputDate);
	}
}
