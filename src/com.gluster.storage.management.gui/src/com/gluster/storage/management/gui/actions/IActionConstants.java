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
package com.gluster.storage.management.gui.actions;

public interface IActionConstants {
	public static final String ACTION_SET_CLUSTER = "com.gluster.storage.management.gui.actionsets.gluster";
	public static final String ACTION_SET_VOLUMES = "com.gluster.storage.management.gui.actionsets.volumes";
	public static final String ACTION_SET_VOLUME = "com.gluster.storage.management.gui.actionsets.volume";
	public static final String ACTION_SET_DISKS = "com.gluster.storage.management.gui.actionsets.disks";
	public static final String ACTION_SET_DISK = "com.gluster.storage.management.gui.actionsets.disk";
	public static final String ACTION_SET_GLUSTER_SERVERS = "com.gluster.storage.management.gui.actionsets.glusterservers";
	public static final String ACTION_SET_GLUSTER_SERVER = "com.gluster.storage.management.gui.actionsets.glusterserver";
	public static final String ACTION_SET_DISCOVERED_SERVERS = "com.gluster.storage.management.gui.actionsets.serversdiscovered";
	public static final String ACTION_SET_DISCOVERED_SERVER = "com.gluster.storage.management.gui.actionsets.serverdiscovered";
	
	public static final String ACTION_SET_EDIT = "com.gluster.storage.management.gui.actionsets.edit";
	
	public static final String COMMAND_CREATE_VOLUME = "com.gluster.storage.management.gui.commands.CreateVolume";
	public static final String COMMAND_ADD_SERVER = "com.gluster.storage.management.gui.commands.AddServer";
	public static final String VIEW_DISCOVERED_SERVER = "com.gluster.storage.management.gui.views.DiscoveredServerView";
}
