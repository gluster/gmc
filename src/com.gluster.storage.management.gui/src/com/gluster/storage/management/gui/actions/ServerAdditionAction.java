package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;

import com.gluster.storage.management.gui.dialogs.ServerAdditionDialog;
import com.gluster.storage.management.gui.utils.GlusterLogger;

public class ServerAdditionAction extends AbstractActionDelegate {
	private static final GlusterLogger logger = GlusterLogger.getInstance();
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
