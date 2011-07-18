package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.gluster.storage.management.gui.dialogs.ChangePasswordDialog;
import com.gluster.storage.management.gui.dialogs.ServerAdditionDialog;

public class ServerAdditionAction extends AbstractActionDelegate {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void performAction(IAction action) {
		
		try {
			// To open a dialog for server addition
			ServerAdditionDialog dialog = new ServerAdditionDialog(getShell());
			dialog.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	

}
