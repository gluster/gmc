/*******************************************************************************
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
 *******************************************************************************/
package com.gluster.storage.management.console.actions;

import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.gluster.storage.management.console.dialogs.MigrateBrickWizard;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Volume;

public class MigrateBrickAction extends AbstractActionDelegate {
	private Volume volume;
	private Brick brick;

	@Override
	protected void performAction(IAction action) {
		MigrateBrickWizard wizard = new MigrateBrickWizard(volume, brick);

		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize(1024, 600);
		dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		Set<Brick> bricks;
		if (selectedEntity instanceof Volume) {
			volume = (Volume) selectedEntity;
		}

		action.setEnabled(false);
		if (selectedEntity instanceof Brick) {
			bricks = GUIHelper.getInstance().getSelectedEntities(getWindow(), Brick.class);
			brick = bricks.iterator().next();
			action.setEnabled(brick != null);
		}
	}

	@Override
	public void dispose() {
	}
}
