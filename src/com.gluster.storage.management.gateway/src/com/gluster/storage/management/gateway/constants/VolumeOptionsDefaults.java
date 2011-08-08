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
package com.gluster.storage.management.gateway.constants;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.VolumeOptionInfo;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.utils.ServerUtil;
import com.sun.jersey.api.core.InjectParam;

@Component
public class VolumeOptionsDefaults {
	@Autowired
	private ServerUtil serverUtil;
	
	@InjectParam
	private ClusterService clusterService;

	public List<VolumeOptionInfo> options;
	
	
	public VolumeOptionsDefaults() {
	}

	/**
	 * @return list of volume option information objects
	 */
	public List<VolumeOptionInfo> getDefaults(String clusterName) {
		return getVolumeOptionsInfo(clusterName);
		// return getVolumeOptionsInfo();
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
				"Changes the log-level of the bricks (servers).", "INFO"));
		volumeOptionsInfo.add(new VolumeOptionInfo("diagnostics.client-log-level",
				"Changes the log-level of the clients.", "INFO"));
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
	
	
	public List<VolumeOptionInfo> getVolumeOptionsInfo(String clusterName) {
		String command = "gluster volume set help-xml";
		String output = "";
		VolumeOptionInfoListResponse options = new VolumeOptionInfoListResponse();
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);

		try {
			output = getVolumeOptionsInfo(onlineServer.getName(), command);
			options = parseXML(output); 
			return options.getOptions();
		} catch (ConnectionException e) {
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			output =  getVolumeOptionsInfo(onlineServer.getName(), command);
			options = parseXML(output);
			return options.getOptions();
		} catch (Exception e) {
			throw new GlusterRuntimeException("Fetching volume options default failed! [" + e.getMessage() + "]"); 
		}
	}
	
	private String getVolumeOptionsInfo(String serverName, String command) {
		return serverUtil.executeOnServer(true, serverName, command,
				String.class);
	}
	
	public VolumeOptionInfoListResponse parseXML(String xml) {
		xml = xml.replaceAll("<Description>", "<description>").replaceAll("</Description>", "</description>");
		return serverUtil.unmarshal(VolumeOptionInfoListResponse.class, xml);
	}
	
	
//	public String getHelpxml() {
//		return "<volumeOptionsDefaults><volumeOption><defaultValue>16</defaultValue><Description>Maximum number blocks per file for which self-heal process would be applied simultaneously.</Description><name>cluster.self-heal-window-size</name></volumeOption><volumeOption><defaultValue></defaultValue><Description>Select between &quot;full&quot;, &quot;diff&quot;. The &quot;full&quot; algorithm copies the entire file from source to sink. The &quot;diff&quot; algorithm copies to sink only those blocks whose checksums don't match with those of source.</Description><name>cluster.data-self-heal-algorithm</name></volumeOption><volumeOption><defaultValue>128KB</defaultValue><Description>Size of the stripe unit that would be read from or written to the striped servers.</Description><name>cluster.stripe-block-size</name></volumeOption><volumeOption><defaultValue>0</defaultValue><Description>Maximum file size which would be cached by the io-cache translator.</Description><name>performance.cache-max-file-size</name></volumeOption><volumeOption><defaultValue>0</defaultValue><Description>Minimum file size which would be cached by the io-cache translator.</Description><name>performance.cache-min-file-size</name></volumeOption><volumeOption><defaultValue>1</defaultValue><Description>The cached data for a file will be retained till 'cache-refresh-timeout' seconds, after which data re-validation is performed.</Description><name>performance.cache-refresh-timeout</name></volumeOption><volumeOption><defaultValue></defaultValue><Description>Assigns priority to filenames with specific patterns so that when a page needs to be ejected out of the cache, the page of a file whose priority is the lowest will be ejected earlier</Description><name>performance.cache-priority</name></volumeOption><volumeOption><defaultValue>on</defaultValue><Description>If this option is set ON, instructs write-behind translator to perform flush in background, by returning success (or any errors, if any of previous  writes were failed) to application even before flush is sent to backend filesystem. </Description><name>performance.flush-behind</name></volumeOption><volumeOption><defaultValue>16</defaultValue><Description>Number of threads in IO threads translator which perform concurrent IO operations</Description><name>performance.io-thread-count</name></volumeOption><volumeOption><defaultValue>1MB</defaultValue><Description>Size of the per-file write-behind buffer. </Description><name>performance.write-behind-window-size</name></volumeOption><volumeOption><defaultValue>off</defaultValue><Description>For nfs clients or apps that do not support 64-bit inode numbers, use this option to make NFS return 32-bit inode numbers instead. Disabled by default so NFS returns 64-bit inode numbers by default.</Description><name>nfs.enable-ino32</name></volumeOption><volumeOption><defaultValue>15</defaultValue><Description>Use this option to make NFS be faster on systems by using more memory. This option specifies a multiple that determines the total amount of memory used. Default value is 15. Increase to use more memory in order to improve performance for certain use cases.Please consult gluster-users list before using this option.</Description><name>nfs.mem-factor</name></volumeOption><volumeOption><defaultValue>on</defaultValue><Description>By default, all subvolumes of nfs are exported as individual exports. There are cases where a subdirectory or subdirectories in the volume need to be exported separately. Enabling this option allows any directory on a volumes to be exported separately. Directory exports are enabled by default.</Description><name>nfs.export-dirs</name></volumeOption><volumeOption><defaultValue>on</defaultValue><Description>Enable or disable exporting whole volumes, instead if used in conjunction with nfs3.export-dir, can allow setting up only subdirectories as exports. On by default.</Description><name>nfs.export-volumes</name></volumeOption><volumeOption><defaultValue>on</defaultValue><Description>Users have the option of turning off name lookup for incoming client connections using this option. In some setups, the name server can take too long to reply to DNS queries resulting in timeouts of mount requests. Use this option to turn off name lookups during address authentication. Note, turning this off will prevent you from using hostnames in rpc-auth.addr.* filters. By default,  name lookup is on.</Description><name>nfs.addr-namelookup</name></volumeOption><volumeOption><defaultValue>on</defaultValue><Description>For systems that need to run multiple nfs servers, weneed to prevent more than one from registering with portmap service. Use this option to turn off portmap registration for Gluster NFS. On by default</Description><name>nfs.register-with-portmap</name></volumeOption><volumeOption><defaultValue></defaultValue><Description>Use this option on systems that need Gluster NFS to be associated with a non-default port number.</Description><name>nfs.port</name></volumeOption><volumeOption><defaultValue>on</defaultValue><Description>Disable or enable the AUTH_UNIX authentication type for a particular exported volume over-riding defaults and general setting for AUTH_UNIX scheme. Must always be enabled for better interoperability.However, can be disabled if needed. Enabled bydefault.</Description><name>nfs.rpc-auth-unix</name></volumeOption><volumeOption><defaultValue></defaultValue><Description>Allow a comma separated list of addresses and/or hostnames to connect to the server. By default, all connections are disallowed. This allows users to define a rule for a specific exported volume.</Description><name>nfs.rpc-auth-allow</name></volumeOption><volumeOption><defaultValue></defaultValue><Description>Reject a comma separated list of addresses and/or hostnames from connecting to the server. By default, all connections are disallowed. This allows users todefine a rule for a specific exported volume.</Description><name>nfs.rpc-auth-reject</name></volumeOption><volumeOption><defaultValue></defaultValue><Description>Allow client connections from unprivileged ports. By default only privileged ports are allowed. Use this option to set enable or disable insecure ports for a specific subvolume and to over-ride global setting  set by the previous option.</Description><name>nfs.ports-insecure</name></volumeOption><volumeOption><defaultValue>off</defaultValue><Description>All writes and COMMIT requests are treated as async. This implies that no write requests are guaranteed to be on server disks when the write reply is received at the NFS client. Trusted sync includes  trusted-write behaviour. Off by default.</Description><name>nfs.trusted-sync</name></volumeOption><volumeOption><defaultValue>off</defaultValue><Description>On an UNSTABLE write from client, return STABLE flag to force client to not send a COMMIT request. In some environments, combined with a replicated GlusterFS setup, this option can improve write performance. This flag allows user to trust Gluster replication logic to sync data to the disks and recover when required. COMMIT requests if received will be handled in a default manner by fsyncing. STABLE writes are still handled in a sync manner. Off by default.</Description><name>nfs.trusted-write</name></volumeOption><volumeOption><defaultValue>read-write</defaultValue><Description>Type of access desired for this subvolume:  read-only, read-write(default)</Description><name>nfs.volume-access</name></volumeOption><volumeOption><defaultValue></defaultValue><Description>By default, all subvolumes of nfs are exported as individual exports. There are cases where a subdirectory or subdirectories in the volume need to be exported separately. This option can also be used in conjunction with nfs3.export-volumes option to restrict exports only to the subdirectories specified through this option. Must be an absolute path.</Description><name>nfs.export-dir</name></volumeOption><volumeOption><defaultValue>off</defaultValue><Description>This option is used to enable/disable NFS serverfor individual volume.</Description><name>nfs.disable</name></volumeOption></volumeOptionsDefaults>";
//	}
//	
//	
//	
//	public static void main(String[] args) {
//		VolumeOptionsDefaults vod = new VolumeOptionsDefaults();
//		String command = "gluster volume set help-xml";
//		String output = "";
//		ServerUtil serverUtil = new ServerUtil();
//		try {
////			Process p = Runtime.getRuntime().exec(command);
////			p.waitFor();
////			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
////			String line = reader.readLine();
////			while (line != null) {
////				output += line;
////				line = reader.readLine();
////			}
//			output = vod.getHelpxml();
//			output = output.replaceAll("<volumeOptionsDefaults>", "<options>")
//					.replaceAll("</volumeOptionsDefaults>", "</options>").replaceAll("<volumeOption>", "<option>")
//					.replaceAll("</volumeOption>", "</option>");
//		} catch (Exception e) {
//			System.out.println("Error in executing command ...." + e.getMessage());
//			return;
//		}
//		Object response = serverUtil.unmarshal(VolumeOptionInfoListResponse.class, output);
//		if (response instanceof Status) {
//			System.out.println("Error: " + response.toString());
//			return;
//		}
//		for (VolumeOptionInfo option : ((VolumeOptionInfoListResponse) response).getOptions()) {
//			System.out.println(option.getName() + " : " + option.getDefaultValue() );
//		}
//	}
}