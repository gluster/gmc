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

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.gluster.storage.management.console.Activator;


/**
 *
 */
public class ChartsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public ChartsPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Gluster Management Console");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		String[][] entryNamesAndValues = new String[][] {
				{ "1 day", "1d" }, { "1 week", "1w" }, { "1 month", "1m" }, { "1 year", "1y" } };
		addField(new ComboFieldEditor(PreferenceConstants.P_CPU_AGGREGATED_CHART_PERIOD, "Aggregated CPU Usage chart period", entryNamesAndValues,
				getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_NETWORK_AGGREGATED_CHART_PERIOD, "Aggregated Network Usage chart period", entryNamesAndValues,
				getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_CPU_CHART_PERIOD, "CPU Usage chart period", entryNamesAndValues,
				getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_MEM_CHART_PERIOD, "Memory Usage chart period", entryNamesAndValues,
				getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_NETWORK_CHART_PERIOD, "Network Usage chart period", entryNamesAndValues,
				getFieldEditorParent()));
	}

}
