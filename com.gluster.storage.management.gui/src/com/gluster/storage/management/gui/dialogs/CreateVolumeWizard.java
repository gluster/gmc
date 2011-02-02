package com.gluster.storage.management.gui.dialogs;

import org.eclipse.jface.wizard.Wizard;

public class CreateVolumeWizard extends Wizard {

	public CreateVolumeWizard() {
		setWindowTitle("Gluster Management Console - Create Volume");
		setHelpAvailable(false); // TODO: Introduce wizard help
	}

	@Override
	public void addPages() {
		addPage(new CreateVolumePage1());
	}

	@Override
	public boolean performFinish() {
		System.out.println("Finishing volume creation!");
		// TODO: Add code to create volume
		return true;
	}
}
