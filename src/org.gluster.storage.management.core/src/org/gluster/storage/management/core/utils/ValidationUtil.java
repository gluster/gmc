package org.gluster.storage.management.core.utils;

import java.util.regex.Pattern;

public class ValidationUtil {

	// Access control may contains IP with wild card(*), hostname and/or multiple ip/hostnames
	public static boolean isValidAccessControl(String ac) {
		String access[] = ac.split(",");
		String ip;
		boolean isValidAccessControl = true;
		for (int i = 0; i < access.length && isValidAccessControl; i++) {
			ip = access[i].trim();
			isValidAccessControl = (isValidIpWithWC(ip) || isValidHostName(ip));
		}
		return isValidAccessControl;
	}

	public static String getInvalidIpOrHostname(String ac) {
		String access[] = ac.split(",");
		String ip;
		boolean isValidAccessControl = true;
		for (int i = 0; i < access.length && isValidAccessControl; i++) {
			ip = access[i].trim();
			if (!(isValidIpWithWC(ip) || isValidHostName(ip))) {
				return access[i];
			}
		}
		return "";
	}
	
	public static boolean isValidIpWithWC(String ip) {
		String ipAddress[] = ip.split("\\.");
		boolean isValid = true;

		if (ip.equals("0.0.0.0") || ip.equals("255.255.255.255")) { // Invalidate the special ip's
			isValid = false;
		}

		int iterator=ipAddress.length-1;
		
		if (ipAddress.length <= 4 && ipAddress[ipAddress.length - 1].equals("*")) {
			iterator = ipAddress.length - 2;
		} else if (ipAddress.length < 4 || ipAddress.length > 4 ){
			isValid = false;
			iterator = ipAddress.length - 1;
		}

		for (int i = 0; i <= iterator && isValid; i++) {
			if (ipAddress[i].equals("*")) {
				isValid = (i == ipAddress.length - 1) ? isValid : false;
			} else {
				isValid = isValidIpQuad(ipAddress[i]);
			}
		}
		return isValid;
	}

	public static boolean isValidIp(String ip) {
		String ipAddress[] = ip.split("\\.");
		boolean isValid = true;

		if (ip.equals("0.0.0.0") || ip.equals("255.255.255.255")) { // Invalidate the special ip's
			isValid = false;
		}
		if (ipAddress.length < 4) {
			isValid = false;
		}
		for (int i = 0; i < ipAddress.length && isValid; i++) {
			isValid = isValidIpQuad(ipAddress[i]);
		}
		return isValid;
	}

	private static boolean isValidIpQuad(String ipQuad) {
		Pattern pattern = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
		return pattern.matcher(ipQuad).matches();
	}

	public static boolean isValidHostName(String hostName) {
		Pattern pattern = Pattern
				.compile("^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$");
		return pattern.matcher(hostName).matches();
	}

	public static void main(String[] argv) {
		String ip = "0.0.0.0";
		// System.out.println("Is valid ip (" + ip + ")? " + isValidIp(ip));
		String hostName = "myhost.q";
		// System.out.println(isValidHostName(hostName));
		// System.out.println(isValidHostName(hostName));
		System.out.println(isValidAccessControl(hostName));
	}

}
