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
import org.gluster.storage.management.client.KeysClient;


/**
 * @author root
 *
 */
public class ExportSshKeysAction extends AbstractActionDelegate {
	
	@Override
	protected void performAction(IAction action) {
		final KeysClient client = new KeysClient();
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setFilterNames(new String[] { "Tar (*.tar)" });
		dialog.setFilterExtensions(new String[] { "*.tar" });
		String filePath = dialog.open();

		if (filePath == null) {
			return;
		}

		String title = "Export SSH Keys";
		try {
			client.exportSshKeys(filePath);
			showInfoDialog(title, "SSH keys exported successfully to [" + filePath + "]");
		} catch (Exception e) {
			showErrorDialog(title, e.getMessage());
		}
	}
	
	
	@Override
	public void dispose() {
	}

}
