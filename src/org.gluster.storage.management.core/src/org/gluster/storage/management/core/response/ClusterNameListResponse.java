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
package org.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response object for transferring cluster names during REST communication. This is just a wrapper over a list of
 * Strings, and is required because the jersey rest framework can't transfer lists directly.
 */
@XmlRootElement(name="clusters")
public class ClusterNameListResponse {
private List<String> clusterNames = new ArrayList<String>();
	
	public ClusterNameListResponse() {
	}
	
	public ClusterNameListResponse(List<String> clusterNames) {
		this.clusterNames = clusterNames;
	}
	
	@XmlElement(name = "cluster", type = String.class)
	public List<String> getClusterNames() {
		return clusterNames;
	}
}
