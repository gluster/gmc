package com.gluster.storage.management.console.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.gluster.storage.management.console.Activator;

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
