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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.gluster.storage.management.client.KeysClient;


public class ImportSshKeysAction extends AbstractActionDelegate {

	@Override
	protected void performAction(IAction action) {
		final KeysClient client = new KeysClient();

		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText("Open");
				dialog.setFilterNames(new String[] { "ssh-keys (*.tar)" });
				dialog.setFilterExtensions(new String[] { "*.tar" });

				String selectedFile = dialog.open();
				if (selectedFile == null) {
					return;
				}

				String title = "Import SSH Keys";
				try {
					client.importSshKeys(selectedFile);
					showInfoDialog(title, "SSH keys imported successfully!");
				} catch (Exception e) {
					showErrorDialog(title, e.getMessage());
				}
			}
		});		
	}			

	@Override
	public void dispose() {
	}
}
