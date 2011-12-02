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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.model.Volume;


/**
 *
 */
public class DownloadVolumeLogsAction extends AbstractActionDelegate {
	private GUIHelper guiHelper = GUIHelper.getInstance();

	@Override
	public void dispose() {
	}

	@Override
	protected void performAction(IAction action) {
		final VolumesClient client = new VolumesClient();
		Volume volume = guiHelper.getSelectedEntity(getWindow(), Volume.class);

		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setFilterNames(new String[] { "GZipped Tar (*.tar.gz)" });
		dialog.setFilterExtensions(new String[] { "*.tar.gz" });
		String filePath = dialog.open();

		if (filePath == null) {
			return;
		}

		String title = "Download Volume Logs [" + volume.getName() + "]";
		try {
			client.downloadLogs(volume.getName(), filePath);
			showInfoDialog(title, "Volume logs downloaded successfully to [" + filePath + "]");
		} catch (Exception e) {
			showErrorDialog(title, e.getMessage());
		}
	}
}
