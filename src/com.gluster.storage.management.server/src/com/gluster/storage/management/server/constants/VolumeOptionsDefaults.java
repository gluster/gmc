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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.VolumeOptionInfo;

@XmlRootElement
public class VolumeOptionsDefaults {
	@XmlElementWrapper(name = "volumeOptions")
	@XmlElement(name = "volumeOption", type = VolumeOptionInfo.class)
	public List<VolumeOptionInfo> options;
	
	public VolumeOptionsDefaults() {
	}
	
	public VolumeOptionsDefaults getDefaults() {
		options = getVolumeOptionsInfo();
		return this;
	}

	/**
	 * Fetches the list of all volume options with their information from GlusterFS and returns the same
	 * 
	 * @return List of volume option information objects
	 */
	private List<VolumeOptionInfo> getVolumeOptionsInfo() {
		List<VolumeOptionInfo> volumeOptionsInfo = new ArrayList<VolumeOptionInfo>();

		volumeOptionsInfo
				.add(new VolumeOptionInfo(
						"cluster.stripe-block-size",
						"This could be used in case of a stripe setup. Specifies the size of the stripe unit that will read from or written to the striped servers. "
								+ CoreConstants.NEWLINE
								+ "Optionally different stripe unit sizes can be specified for different fies, with the following pattern <filename-pattern:blk-size>. ",
						"*:128KB"));
		volumeOptionsInfo
				.add(new VolumeOptionInfo(
						"cluster.self-heal-window-size",
						"Specifies the number of maximum number blocks per file for which self-heal process would be applied simultaneously.",
						"16"));
		volumeOptionsInfo.add(new VolumeOptionInfo("cluster.data-self-heal-algorithm",
				"cluster.data-self-heal-algorithm", "auto"));
		volumeOptionsInfo
				.add(new VolumeOptionInfo(
						"network.frame-timeout",
						"The time frame after which the operation has to be declared as dead, if the server does not respond for a particular operation.",
						"1800"));
		volumeOptionsInfo.add(new VolumeOptionInfo("network.ping-timeout",
				"The time duration for which the client waits to check if the server is responsive.", "42"));
		volumeOptionsInfo.add(new VolumeOptionInfo("auth.allow",
				"'IP addresses/Host name' of the clients which should be allowed to access the the volume.", "*"));
		volumeOptionsInfo.add(new VolumeOptionInfo("auth.reject",
				"'IP addresses/Host name' of the clients which should be denied to access the volume.", "NONE"));
		volumeOptionsInfo
				.add(new VolumeOptionInfo(
						"performance.cache-refresh-timeout",
						"The cached data for a file will be retained till 'cache-refresh-timeout' seconds, after which data re-validation is performed.",
						"1"));
		volumeOptionsInfo.add(new VolumeOptionInfo("performance.cache-size", "Size of the read cache.", "32MB"));
		volumeOptionsInfo.add(new VolumeOptionInfo("performance.write-behind-window-size",
				"Size of the per-file write-behind buffer.", "1MB"));
		volumeOptionsInfo.add(new VolumeOptionInfo("performance.cache-max-file-size",
				"performance.cache-max-file-size", "-1"));
		volumeOptionsInfo.add(new VolumeOptionInfo("performance.cache-min-file-size",
				"performance.cache-min-file-size", "0"));
		volumeOptionsInfo
				.add(new VolumeOptionInfo(
						"performance.io-thread-count",
						" Number of threads in the thread-pool in the bricks to improve the concurrency in I/O s of server side.",
						"16"));
		volumeOptionsInfo
				.add(new VolumeOptionInfo(
						"diagnostics.latency-measurement",
						"Statistics related to the latency of each operation would be tracked inside GlusterFS data-structures.",
						"off"));
		volumeOptionsInfo.add(new VolumeOptionInfo("diagnostics.dump-fd-stats",
				"Statistics related to file-operations would be tracked inside GlusterFS data-structures.", "off"));
		volumeOptionsInfo.add(new VolumeOptionInfo("diagnostics.brick-log-level",
				"Changes the log-level of the bricks (servers).", "NORMAL"));
		volumeOptionsInfo.add(new VolumeOptionInfo("diagnostics.client-log-level",
				"Changes the log-level of the clients.", "NORMAL"));
		volumeOptionsInfo.add(new VolumeOptionInfo("nfs.enable-ino32",
				"Use this option from the CLI to make Gluster NFS return 32-bit inode numbers instead of 64-bit.",
				"off"));
		volumeOptionsInfo
				.add(new VolumeOptionInfo(
						"nfs.mem-factor",
						"This option specifies a multiple that determines the total amount of memory used. Increases this increases the performance of NFS.",
						"15"));
		volumeOptionsInfo.add(new VolumeOptionInfo("transport.keepalive", "transport.keepalive", "on"));

		return volumeOptionsInfo;
	}
}