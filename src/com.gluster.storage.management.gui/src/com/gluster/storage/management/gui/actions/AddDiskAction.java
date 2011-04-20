package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.dialogs.AddDiskWizard;

public class AddDiskAction extends AbstractActionDelegate {
	private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		window = null;
	}

	/* (non-Javadoc)
	 * @see com.gluster.storage.management.gui.actions.AbstractActionDelegate#performAction(org.eclipse.jface.action.IAction)
	 */
	@Override
	protected void performAction(IAction action) {
		//TODO: open a dialog box 
		// MessageDialog.openInformation(getShell(), "Action captured", action.getDescription() + "\n" + volume.getName());
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				AddDiskWizard wizard = new AddDiskWizard();
				wizard.addPages(volume);
				
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
		        dialog.create();
		        dialog.getShell().setSize(1024, 600);	
		        dialog.open();
			}
		});
		
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gluster.storage.management.gui.actions.AbstractActionDelegate#selectionChanged(org.eclipse.jface.action.IAction
	 * , org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		if (selectedEntity instanceof Volume) {
			this.volume = (Volume) selectedEntity;
			// action.setEnabled(volume.getStatus() == VOLUME_STATUS.ONLINE);
		} 
	}
	
}
