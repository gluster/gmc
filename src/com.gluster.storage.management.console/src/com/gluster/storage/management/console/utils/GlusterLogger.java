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
package com.gluster.storage.management.console.utils;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;

import com.gluster.storage.management.console.Activator;

/**
 *
 */
public class GlusterLogger {
	private static final ILog log = Activator.getDefault().getLog();
	private static GlusterLogger instance = new GlusterLogger();
	
	private GlusterLogger() {
	}
	
	public static GlusterLogger getInstance() {
		return instance;
	}

	private void log(String message, int severity, Throwable t) {
		log.log(new Status(severity, Activator.PLUGIN_ID, message, t));
	}

	public void error(String message) {
		log(message, Status.ERROR, null);
	}

	public void error(String message, Throwable t) {
		log(message, Status.ERROR, t);
	}

	public void warn(String message) {
		log(message, Status.WARNING, null);
	}

	public void warn(String message, Throwable t) {
		log(message, Status.WARNING, t);
	}

	public void info(String message) {
		log(message, Status.INFO, null);
	}
	
	public void info(String message, Throwable t) {
		log(message, Status.INFO, t);
	}
}
