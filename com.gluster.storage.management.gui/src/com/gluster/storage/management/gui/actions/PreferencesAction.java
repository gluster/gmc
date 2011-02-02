package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionFactory;

public class PreferencesAction extends AbstractActionDelegate {

	@Override
	public void dispose() {
		
	}

	@Override
	public void run(IAction action) {
		ActionFactory.PREFERENCES.create(window).run();
	}

}
