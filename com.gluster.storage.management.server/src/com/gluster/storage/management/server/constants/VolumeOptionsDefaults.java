/**
 * DefaultVolumeOptions.java
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
package com.gluster.storage.management.server.constants;

import java.util.HashMap;
import java.util.Map;

public class VolumeOptionsDefaults {
	public static final Map<String, String> OPTIONS = new HashMap<String, String>();
	
	static {
		OPTIONS.put("cluster.stripe-block-size", "*:128KB");
		OPTIONS.put("cluster.self-heal-window-size", "16");
		OPTIONS.put("cluster.data-self-heal-algorithm", "full/diff");
		OPTIONS.put("network.frame-timeout", "1800");
		OPTIONS.put("network.ping-timeout", "42");
		OPTIONS.put("auth.allow", "*");
		OPTIONS.put("auth.reject", "NONE");
		OPTIONS.put("performance.cache-refresh-timeout", "1");
		OPTIONS.put("performance.cache-size", "32MB");
		OPTIONS.put("performance.write-behind-window-size", "1MB");
		OPTIONS.put("performance.cache-max-file-size", "?");
		OPTIONS.put("performance.cache-min-file-size", "?");
		OPTIONS.put("performance.io-thread-count", "?");
		OPTIONS.put("diagnostics.latency-measurement", "off");
		OPTIONS.put("diagnostics.dump-fd-stats", "off");
		OPTIONS.put("diagnostics.brick-log-level", "NORMAL");
		OPTIONS.put("diagnostics.client-log-level", "NORMAL");
		OPTIONS.put("nfs.enable-ino32", "off");
		OPTIONS.put("nfs.mem-factor", "15");
		OPTIONS.put("transport.keepalive", "?");
	}
}