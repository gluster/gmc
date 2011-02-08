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
package com.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.core.utils.StringUtils;

public class Volume extends Entity {
	public enum VOLUME_STATUS {
		ONLINE, OFFLINE
	};

	public enum VOLUME_TYPE {
		PLAIN_DISTRIBUTE, DISTRIBUTED_MIRROR, DISTRIBUTED_STRIPE
	};

	public enum TRANSPORT_TYPE {
		ETHERNET, INFINIBAND
	};

	public enum NAS_PROTOCOL {
		GLUSTERFS, NFS
	};

	private static final String[] VOLUME_TYPE_STR = new String[] { "Plain Distribute", "Distributed Mirror",
			"Distributed Stripe" };
	private static final String[] TRANSPORT_TYPE_STR = new String[] { "Ethernet", "Infiniband" };
	private static final String[] STATUS_STR = new String[] { "Online", "Offline" };
	private static final String[] NAS_PROTOCOL_STR = new String[] { "Gluster", "NFS" };

	private Cluster cluster;
	private VOLUME_TYPE volumeType;
	private TRANSPORT_TYPE transportType;
	private VOLUME_STATUS status;
	private int replicaCount;
	private int stripeCount;
	private Map<String, String> options = new LinkedHashMap<String, String>(); 

	private double totalDiskSpace = 0;
	private List<Disk> disks = new ArrayList<Disk>();

	// GlusterFS export is always enabled
	private Set<NAS_PROTOCOL> nasProtocols = new LinkedHashSet<NAS_PROTOCOL>(
			Arrays.asList(new NAS_PROTOCOL[] { NAS_PROTOCOL.GLUSTERFS }));

	private String accessControlList = "*";

	public String getVolumeTypeStr() {
		return getVolumeTypeStr(getVolumeType());
	}
	
	public static String getVolumeTypeStr(VOLUME_TYPE volumeType) {
		return VOLUME_TYPE_STR[volumeType.ordinal()];
	}

	public String getTransportTypeStr() {
		return TRANSPORT_TYPE_STR[getTransportType().ordinal()];
	}

	public String getStatusStr() {
		return STATUS_STR[getStatus().ordinal()];
	}

	public int getNumOfDisks() {
		return disks.size();
	}

	public VOLUME_TYPE getVolumeType() {
		return volumeType;
	}

	public void setVolumeType(VOLUME_TYPE volumeType) {
		this.volumeType = volumeType;
	}

	public TRANSPORT_TYPE getTransportType() {
		return transportType;
	}

	public void setTransportType(TRANSPORT_TYPE transportType) {
		this.transportType = transportType;
	}

	public VOLUME_STATUS getStatus() {
		return status;
	}

	public int getReplicaCount() {
		return replicaCount;
	}

	public void setReplicaCount(int replicaCount) {
		this.replicaCount = replicaCount;
	}

	public int getStripeCount() {
		return stripeCount;
	}

	public void setStripeCount(int stripeCount) {
		this.stripeCount = stripeCount;
	}

	public void setStatus(VOLUME_STATUS status) {
		this.status = status;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Set<NAS_PROTOCOL> getNASProtocols() {
		return nasProtocols;
	}

	public void setNASProtocols(Set<NAS_PROTOCOL> nasProtocols) {
		this.nasProtocols = nasProtocols;
	}

	public String getNASProtocolsStr() {
		String protocolsStr = "";
		for (NAS_PROTOCOL protocol : nasProtocols) {
			String protocolStr = NAS_PROTOCOL_STR[protocol.ordinal()];
			protocolsStr += (protocolsStr.isEmpty() ? protocolStr : ", " + protocolStr);
		}
		return protocolsStr;
	}

	public String getAccessControlList() {
		return accessControlList;
	}

	public void setAccessControlList(String accessControlList) {
		this.accessControlList = accessControlList;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOption(String key, String value) {
		options.put(key, value);
	}
	
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

	public double getTotalDiskSpace() {
		return totalDiskSpace;
	}

	public List<Disk> getDisks() {
		return disks;
	}

	public void addDisk(Disk disk) {
		if (disks.add(disk) && disk.getStatus() != DISK_STATUS.OFFLINE) {
			totalDiskSpace += disk.getSpace();
		}
	}

	public void addDisks(List<Disk> disks) {
		for (Disk disk : disks) {
			addDisk(disk);
		}
	}

	public void removeDisk(Disk disk) {
		if (disks.remove(disk)) {
			totalDiskSpace -= disk.getSpace();
		}
	}

	public void removeAllDisks() {
		disks.clear();
		totalDiskSpace = 0;
	}

	public void setDisks(List<Disk> disks) {
		removeAllDisks();
		addDisks(disks);
	}

	public void enableNFS() {
		nasProtocols.add(NAS_PROTOCOL.NFS);
	}

	public void disableNFS() {
		nasProtocols.remove(NAS_PROTOCOL.NFS);
	}

	public Volume(String name, Entity parent, VOLUME_TYPE volumeType, TRANSPORT_TYPE transportType, VOLUME_STATUS status) {
		super(name, parent);
		setVolumeType(volumeType);
		setTransportType(transportType);
		setStatus(status);
	}

	public Volume(String name, Entity parent, Cluster cluster, VOLUME_TYPE volumeType, TRANSPORT_TYPE transportType,
			VOLUME_STATUS status) {
		this(name, parent, volumeType, transportType, status);

		setCluster(cluster);
	}

	/**
	 * Filter matches if any of the properties name, volume type, transport type, status and number of disks contains
	 * the filter string
	 */
	@Override
	public boolean filter(String filterString, boolean caseSensitive) {
		return StringUtils.filterString(getName() + getVolumeTypeStr() + getTransportTypeStr() + getStatusStr()
				+ getNumOfDisks(), filterString, caseSensitive);
	}
}
