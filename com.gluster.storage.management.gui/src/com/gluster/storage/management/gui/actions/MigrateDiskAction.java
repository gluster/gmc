package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.dialogs.MigrateDiskWizard;

public class MigrateDiskAction extends AbstractActionDelegate {
	private Volume volume;
	private Disk disk;

	@Override
	public void run(IAction action) {
//		MigrateDiskDialog dialog = new MigrateDiskDialog(window.getShell(), volume, disk);
// 		dialog.create();
// 		dialog.open();
		MigrateDiskWizard wizard = new MigrateDiskWizard(volume, disk);

		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(1024, 600);
		dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		if (selectedEntity instanceof Volume) {
			volume = (Volume) selectedEntity;
		}

		action.setEnabled(false);
		if (selectedEntity instanceof Disk) {
			disk = (Disk) selectedEntity;
			action.setEnabled(((StructuredSelection) selection).size() == 1);
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
