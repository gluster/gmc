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
package org.gluster.storage.management.console.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.gluster.storage.management.console.Activator;


public class AlertsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public AlertsPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Gluster Management Console - Alerts");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		addField(new IntegerFieldEditor(PreferenceConstants.P_SERVER_CPU_CRITICAL_THRESHOLD,
				"&Server CPU usage threshold:", getFieldEditorParent()));

		addField(new IntegerFieldEditor(PreferenceConstants.P_SERVER_MEMORY_USAGE_THRESHOLD,
				"&Server memory usage threshold (%):", getFieldEditorParent()));
		
		addField(new IntegerFieldEditor(PreferenceConstants.P_DISK_SPACE_USAGE_THRESHOLD,
				"&Disk space usage threshold (%):", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}
}
