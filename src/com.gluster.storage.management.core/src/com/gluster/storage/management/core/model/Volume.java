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
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.gluster.storage.management.core.utils.StringUtil;

@XmlRootElement
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

	
	public static final int DEFAULT_REPLICA_COUNT = 2;
	public static final int DEFAULT_STRIPE_COUNT = 4;

	public static final String OPTION_AUTH_ALLOW = "auth.allow";

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
	private VolumeOptions options = new VolumeOptions();

	private double totalDiskSpace = 0;
	private List<String> disks = new ArrayList<String>();
	private List<Brick> bricks = new ArrayList<Brick>();

	public Volume() {
	}

	// GlusterFS and NFS export is always enabled
	private Set<NAS_PROTOCOL> nasProtocols = new LinkedHashSet<NAS_PROTOCOL>(Arrays.asList(new NAS_PROTOCOL[] {
			NAS_PROTOCOL.GLUSTERFS, NAS_PROTOCOL.NFS }));

	public String getVolumeTypeStr() {
		return getVolumeTypeStr(getVolumeType());
	}

	public static String getVolumeTypeStr(VOLUME_TYPE volumeType) {
		return VOLUME_TYPE_STR[volumeType.ordinal()];
	}
	
	public static VOLUME_TYPE getVolumeTypeByStr(String volumeTypeStr) {
		return VOLUME_TYPE.valueOf(volumeTypeStr);
	}
	
	public static TRANSPORT_TYPE getTransportTypeByStr(String transportTypeStr) {
		return TRANSPORT_TYPE.valueOf(transportTypeStr);
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
	
	public int getNumOfBricks() {
		return bricks.size();
	}

	public VOLUME_TYPE getVolumeType() {
		return volumeType;
	}

	public void setVolumeType(VOLUME_TYPE volumeType) {
		this.volumeType = volumeType;
		// TODO find a way to get the replica / strip count
		if (volumeType == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			setReplicaCount(0);
			setStripeCount(DEFAULT_STRIPE_COUNT);
		} else if (volumeType == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			setReplicaCount(DEFAULT_REPLICA_COUNT);
			setStripeCount(0);
		} else {
			setReplicaCount(0);
			setStripeCount(0);
		}
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

	@XmlTransient
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	@XmlElementWrapper(name = "nasProtocols")
	@XmlElement(name = "nasProtocol", type=NAS_PROTOCOL.class)
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

	@XmlTransient
	public String getAccessControlList() {
		return options.get(OPTION_AUTH_ALLOW);
	}

	public void setAccessControlList(String accessControlList) {
		setOption(OPTION_AUTH_ALLOW, accessControlList);
	}

	@XmlElement(name="options")
	public VolumeOptions getOptions() {
		return options;
	}

	public void setOption(String key, String value) {
		options.put(key, value);
	}

	public void setOptions(LinkedHashMap<String, String> options) {
		this.options.setOptionsMap(options);
	}

	public double getTotalDiskSpace() {
		return totalDiskSpace;
	}

	public List<String> getDisks() {
		return disks;
	}

	public void addDisk(String disk) {
		disks.add(disk);
	}

	public void addDisks(List<String> disks) {
		for (String disk : disks) {
			addDisk(disk);
		}
	}

	public void removeDisk(String disk) {
		disks.remove(disk);
	}

	public void removeAllDisks() {
		disks.clear();
		totalDiskSpace = 0;
	}

	public void addBrick(Brick brick) {
		bricks.add(brick);
	}
	
	public void addBricks(List<Brick> bricks) {
		this.bricks.addAll(bricks);
	}
	
	
	public void setBricks(List<Brick> bricks) {
		this.bricks = bricks;
	}
	
	public void removeBrick(Brick brick) {
		bricks.remove(brick);
	}

	@XmlElementWrapper(name = "bricks")
	@XmlElement(name = "brick", type=Brick.class)
	public List<Brick> getBricks() {
		return bricks;
	}

	public void setDisks(List<String> disks) {
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
		return StringUtil.filterString(getName() + getVolumeTypeStr() + getTransportTypeStr() + getStatusStr()
				+ getNumOfDisks(), filterString, caseSensitive);
	}
	
	public List<String> getBrickDirectories() {
		List<String> brickDirectories = new ArrayList<String>();
		for(Brick brick : getBricks()) {
			brickDirectories.add(brick.getQualifiedName());
		}
		return brickDirectories;
	}
}
