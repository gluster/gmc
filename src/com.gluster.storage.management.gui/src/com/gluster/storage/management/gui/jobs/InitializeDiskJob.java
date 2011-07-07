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
package com.gluster.storage.management.gui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IProgressConstants2;

import com.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class InitializeDiskJob extends Job {
	private Disk disk;
	private Application app = Application.getApplication();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	public InitializeDiskJob(Disk disk) {
		super("Initialize Disk [" + disk.getQualifiedName() + "]");
		this.disk = disk;
		setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		setProperty(IProgressConstants.ICON_PROPERTY, guiHelper.getImageDescriptor(IImageKeys.WORK_IN_PROGRESS));
	}

	private void updateStatus(final DEVICE_STATUS status) {
		disk.setStatus(status);
		disk.setSpaceInUse(0d);
		app.entityChanged(disk, new String[] { "status", "spaceInUse" });
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Formatting the disk...", 100);
		boolean cancelRequested = false;
		String msgPrefix = "";
		try {
			for (int i = 1; i <= 5; i++) {
				if (!cancelRequested && monitor.isCanceled()) {
					cancelRequested = true; // come only once here
					
					msgPrefix = "This task cannot be cancelled! ";
					monitor.setTaskName(msgPrefix);
//					setProperty(IProgressConstants.ICON_PROPERTY, guiHelper.getImageDescriptor(IImageKeys.STATUS_CANCELLED));
//
//					updateStatus(DISK_STATUS.UNINITIALIZED);
//					return new Status(Status.CANCEL, Application.PLUGIN_ID, "Cancelled");
				}
				Thread.sleep(2000);
				monitor.worked(20);
				monitor.setTaskName(msgPrefix + "[" + 20 * i + "%] completed");
			}
			monitor.done();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		updateStatus(DEVICE_STATUS.INITIALIZED);
		setProperty(IProgressConstants.ICON_PROPERTY, guiHelper.getImageDescriptor(IImageKeys.STATUS_SUCCESS));

		return new Status(Status.OK, Application.PLUGIN_ID, "Task Completed!");
	}
}
