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
 * Interface for a cluster listener. Every registered listener will be notified
 * on various events happening on the cluster.
 */
public interface ClusterListener {
	public void discoveredServerAdded(Server server);
	
	public void discoveredServerRemoved(Server server);
	
	public void serverAdded(GlusterServer server);

	public void serverRemoved(GlusterServer server);

	public void serverChanged(GlusterServer server, Event event);

	public void volumeAdded(Volume volume);

	public void volumeRemoved(Volume volume);

	public void volumeChanged(Volume volume, Event event);

	public void volumeCreated(Volume volume);

	public void volumeDeleted(Volume volume);
}
