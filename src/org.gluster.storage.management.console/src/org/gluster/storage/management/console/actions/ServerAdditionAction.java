package org.gluster.storage.management.console.actions;

import org.eclipse.jface.action.IAction;
import org.gluster.storage.management.console.dialogs.ServerAdditionDialog;
import org.gluster.storage.management.console.utils.GlusterLogger;


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
