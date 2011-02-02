package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.dialogs.CreateVolumeWizard;

public class CreateVolumeAction extends AbstractActionDelegate {
	@Override
	public void run(IAction action) {
		CreateVolumeWizard wizard = new CreateVolumeWizard();
		
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.create();
        dialog.getShell().setSize(500, 550);	
        dialog.open();
	}

	@Override
	public void dispose() {
		window = null;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		
		action.setEnabled(true);
		if(selectedEntity instanceof EntityGroup && ((EntityGroup)selectedEntity).getEntityType() != Volume.class) {
			// selected entity is either "servers" or "discovered servers".
			action.setEnabled(false);
		}
	}
}
