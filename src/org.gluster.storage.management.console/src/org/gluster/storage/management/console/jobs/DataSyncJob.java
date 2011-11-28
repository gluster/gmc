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
package org.gluster.storage.management.console.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.core.model.GlusterDataModel;


/**
 *
 */
public class DataSyncJob extends Job {

	public DataSyncJob(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		final GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
		
		// fetch the latest model
		final GlusterDataModel model = modelManager.fetchModel(monitor);
		if(model == null) {
			return Status.CANCEL_STATUS;
		}
		
		monitor.beginTask("Notify views", 1);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				modelManager.updateModel(model);
			}
		});
		monitor.worked(1);
		monitor.done();

		return Status.OK_STATUS;
	}
}