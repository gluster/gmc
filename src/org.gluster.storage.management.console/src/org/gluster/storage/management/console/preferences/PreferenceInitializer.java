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
package org.gluster.storage.management.console.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.gluster.storage.management.console.Activator;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.setDefault(PreferenceConstants.P_SHOW_CLUSTER_SELECTION_DIALOG, true);
		
		// default data sync interval = 5 minutes
		store.setDefault(PreferenceConstants.P_DATA_SYNC_INTERVAL, 300);
		
		// Default CPU utilisation threshold 
		store.setDefault(PreferenceConstants.P_SERVER_CPU_CRITICAL_THRESHOLD, 80);
		
		// Default Memory threshold
		store.setDefault(PreferenceConstants.P_SERVER_MEMORY_USAGE_THRESHOLD, 80);
		
		// Default disk free space threshold 
		store.setDefault(PreferenceConstants.P_DISK_SPACE_USAGE_THRESHOLD, 80);
		
		// Default period for server statistics charts
		store.setDefault(PreferenceConstants.P_CPU_CHART_PERIOD, "1d");
		store.setDefault(PreferenceConstants.P_MEM_CHART_PERIOD, "1d");
		store.setDefault(PreferenceConstants.P_NETWORK_CHART_PERIOD, "1d");
		store.setDefault(PreferenceConstants.P_CPU_AGGREGATED_CHART_PERIOD, "1d");
		store.setDefault(PreferenceConstants.P_NETWORK_AGGREGATED_CHART_PERIOD, "1d");
	}
}
