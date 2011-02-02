package com.gluster.storage.management.core.utils;

import java.text.NumberFormat;

public class NumberUtil {
	public static final String formatNumber(double num) {
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumFractionDigits(2);
		formatter.setMaximumFractionDigits(2);
		return formatter.format(num);
	}
}
