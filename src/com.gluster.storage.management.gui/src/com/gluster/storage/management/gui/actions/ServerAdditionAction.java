package com.gluster.storage.management.gui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.gluster.storage.management.gui.dialogs.ChangePasswordDialog;
import com.gluster.storage.management.gui.dialogs.ServerAdditionDialog;
import com.gluster.storage.management.gui.views.pages.AbstractDisksPage;

public class ServerAdditionAction extends AbstractActionDelegate {
	private static final Logger logger = Logger.getLogger(ServerAdditionAction.class);
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
			logger.error("Error in Manual server addition", e);
			e.printStackTrace();
		}
	}

}
