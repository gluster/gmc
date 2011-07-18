package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.gluster.storage.management.client.KeysClient;

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
