package com.gluster.storage.management.gui.dialogs;

import org.eclipse.jface.wizard.Wizard;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Volume;

public class MigrateDiskWizard extends Wizard {
	private Volume volume;
	private Disk disk;

	public MigrateDiskWizard(Volume volume, Disk disk) {
		setWindowTitle("Gluster Management Console - Migrate Disk [" + volume.getName() + "]");
		this.volume = volume;
		this.disk = disk;
		setHelpAvailable(false); // TODO: Introduce wizard help
	}

	@Override
	public void addPages() {
		addPage(new MigrateDiskPage1(volume, disk));
	}

	@Override
	public boolean performFinish() {
		System.out.println("Triggered Disk Migration!");
		// TODO: Add code to migrate disk
		return true;
	}
}
