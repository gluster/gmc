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
import org.eclipse.swt.widgets.Display;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.model.Volume;


public class ResetVolumeOptionsAction extends AbstractActionDelegate {
	private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	public void dispose() {
	}

	@Override
	protected void performAction(final IAction action) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				final String actionDesc = action.getDescription();

				boolean confirmed = showConfirmDialog(actionDesc,
						"Are you sure you want to reset all options of the volume [" + volume.getName() + "] ?");
				if (!confirmed) {
					return;
				}

				try {
					new VolumesClient().resetVolumeOptions(volume.getName());
					showInfoDialog(actionDesc, "Volume options for [" + volume.getName() + "] reset successfully!");
					modelManager.resetVolumeOptions(volume);
				} catch (Exception e) {
					showErrorDialog(actionDesc, "Volume options for [" + volume.getName()
							+ "] could not be reset! Error: [" + e.getMessage() + "]");
				}
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gluster.storage.management.console.actions.AbstractActionDelegate#selectionChanged(org.eclipse.jface.action.IAction
	 * , org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		volume = GUIHelper.getInstance().getSelectedEntity(getWindow(), Volume.class);
		if (volume != null) {
			action.setEnabled(volume.getOptions().size() > 0);
		} else {
			action.setEnabled(false);
		}
	}
}
