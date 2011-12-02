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
package org.gluster.storage.management.console;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.gluster.storage.management.console.jobs.DataSyncJob;
import org.gluster.storage.management.console.preferences.PreferenceConstants;


/**
 * This workbench advisor creates the window advisor, and specifies
 * the perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
	private Job syncJob;
	private static final IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
	private long JOB_INTERVAL = preferenceStore.getLong(PreferenceConstants.P_DATA_SYNC_INTERVAL) * 1000;
	private IPropertyChangeListener propertyChangeListener;
	
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

	public String getInitialWindowPerspectiveId() {
		return Perspective.ID;
	} 
	
	@Override
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		configurer.setSaveAndRestore(false); // we don't need save/restore as of now
		
		createPropertyChangeListener();
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
	}
	
	private void createPropertyChangeListener() {
		propertyChangeListener = new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if(event.getProperty().equals(PreferenceConstants.P_DATA_SYNC_INTERVAL)) {
					JOB_INTERVAL = (Integer)event.getNewValue() * 1000L;
				}
			}
		};
	}

	@Override
	public void postStartup() {
		super.postStartup();
		setupBackgroundJobs();
	}
	
	private void setupBackgroundJobs() {
		syncJob = new DataSyncJob("Retrieving Management Info");
		syncJob.schedule(JOB_INTERVAL);
		syncJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				super.done(event);
				
				// job done. schedule again after the pre-defined interval
				syncJob.schedule(JOB_INTERVAL);
			}
		});
	}
	
	@Override
	public boolean preShutdown() {
		return syncJob.cancel();
	}
}
