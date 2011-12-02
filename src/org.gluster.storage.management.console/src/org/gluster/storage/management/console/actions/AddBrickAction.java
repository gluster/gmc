/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package org.gluster.storage.management.console.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.gluster.storage.management.console.dialogs.AddBrickWizard;
import org.gluster.storage.management.core.model.Volume;


public class AddBrickAction extends AbstractActionDelegate {
	private Volume volume;
	
	@Override
	public void dispose() {
		window = null;
	}

	@Override
	protected void performAction(IAction action) {
		// TODO: open a dialog box
		// MessageDialog.openInformation(getShell(), "Action captured", action.getDescription() + "\n" +
		// volume.getName());
		AddBrickWizard wizard = new AddBrickWizard(volume); // Also add single page

		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(1024, 600);
		dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		if (selectedEntity instanceof Volume) {
			this.volume = (Volume) selectedEntity;
			// action.setEnabled(volume.getStatus() == VOLUME_STATUS.ONLINE);
		} 
	}
	
}
