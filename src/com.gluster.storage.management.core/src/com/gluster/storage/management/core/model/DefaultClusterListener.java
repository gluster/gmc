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

/**
 * Default listener - doesn't do anything. Sub-class and override the method for
 * the event you want to handle.
 */
public class DefaultClusterListener implements ClusterListener {

	@Override
	public void serverAdded(GlusterServer server) {
		clusterChanged();
	}

	@Override
	public void serverRemoved(GlusterServer server) {
		clusterChanged();
	}

	@Override
	public void serverChanged(GlusterServer server, Event event) {
		clusterChanged();
	}

	@Override
	public void volumeAdded(Volume volume) {
		clusterChanged();
	}

	@Override
	public void volumeRemoved(Volume volume) {
		clusterChanged();
	}

	@Override
	public void volumeChanged(Volume volume, Event event) {		
		clusterChanged();
	}

	@Override
	public void discoveredServerAdded(Server server) {
		clusterChanged();
	}

	@Override
	public void discoveredServerRemoved(Server server) {
		clusterChanged();
	}

	@Override
	public void volumeCreated(Volume volume) {
		clusterChanged();
	}
	
	@Override
	public void volumeDeleted(Volume volume) {
		clusterChanged();
	}
	
	/**
	 * This method is called by every other event method. Thus, if a view/listener is interested in performing the same
	 * task on any change happening in the cluster data model, it can simply override this method and implement the
	 * logic. e.g. A view may simply refresh its tree/table viewer whenever the cluster data model changes.
	 */
	public void clusterChanged() {
		
	}
}
