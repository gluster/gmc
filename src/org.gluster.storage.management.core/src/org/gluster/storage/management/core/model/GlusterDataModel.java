/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.core.model;

import java.util.ArrayList;
import java.util.List;

public class GlusterDataModel extends Entity {
	public GlusterDataModel(String name, List<Cluster> clusters) {
		super(name, null);
		children.addAll(clusters);
	}

	public GlusterDataModel(String name) {
		this(name, new ArrayList<Cluster>());
	}

	public void setClusters(List<Cluster> clusters) {
		children.clear();
		children.addAll(clusters);
	}

	public void addCluster(Cluster cluster) {
		children.add(cluster);
	}
	
	public Cluster getCluster() {
		return (Cluster) children.get(0);
	}
}
