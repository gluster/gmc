/**
 * AddDiskAction.java
 *
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.dialogs.AddDiskWizard;

public class AddDiskAction extends AbstractActionDelegate {
	private Volume volume;
	
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
				AddDiskWizard wizard = new AddDiskWizard(volume); // Also add single page
				
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
