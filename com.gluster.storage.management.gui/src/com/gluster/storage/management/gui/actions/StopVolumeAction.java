package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;

public class StopVolumeAction extends AbstractActionDelegate {
	@Override
	public void run(IAction action) {
		System.out.println("Running [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}
}
