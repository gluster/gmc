/**
 * ServerAdditionDialog.java
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
package org.gluster.storage.management.console.dialogs;

import java.net.URI;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gluster.storage.management.client.GlusterServersClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.model.GlusterServer;


public class ServerAdditionDialog extends Dialog {
	public static final int RETURN_CODE_ERROR = 2;
	private Text serverName;
	private Button addButton;

	private GUIHelper guiHelper = GUIHelper.getInstance();
	private ControlDecoration errDecoration;

	private Composite composite;

	public ServerAdditionDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Gluster Management Console - Add Server");
		addEscapeListener(newShell);
	}

	private void addEscapeListener(Shell shell) {
		shell.addTraverseListener(new TraverseListener() {

			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.keyCode == SWT.ESC) {
					cancelPressed();
				}
			}
		});
	}

	/**
	 * Overriding to make sure that the dialog is centered in screen
	 */
	@Override
	protected void initializeBounds() {
		super.initializeBounds();

		guiHelper.centerShellInScreen(getShell());
	}

	private void configureDialogLayout(Composite composite) {
		GridLayout layout = (GridLayout) composite.getLayout();
		layout.numColumns = 2;
		layout.marginLeft = 20;
		layout.marginRight = 20;
		layout.marginTop = 20;
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 20;
	}

	// ------------------------------------------

	private void createLabel(Composite composite, String label) {
		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText(label);
		passwordLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}

	private Text createServerNameText(Composite composite) {
		Text field = new Text(composite, SWT.BORDER );
		GridData layoutData = new GridData(SWT.FILL, GridData.FILL, true, false);
		layoutData.widthHint = convertWidthInCharsToPixels(32);
		field.setLayoutData(layoutData);
		return field;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		composite = (Composite) super.createDialogArea(parent);
		configureDialogLayout(composite);

		createLabel(composite, "Server Name:");
		serverName = createServerNameText(composite);
		errDecoration = guiHelper.createErrorDecoration(serverName);

		createListeners();

		return composite;
	}

	private void createListeners() {
		ModifyListener listener = new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				updateButtonStatus();
			}
		};
		
		serverName.addModifyListener(listener);
	}

	private void updateButtonStatus() {
		addButton.setEnabled(true);
		errDecoration.hide();

		if(!serverExists(serverName.getText())) {
			addButton.setEnabled(false);
			errDecoration.setDescriptionText("Server name already exists.");
			errDecoration.show();
		}
		
		if(serverName.getText().isEmpty()) {
			addButton.setEnabled(false);
			errDecoration.setDescriptionText("Please enter server name!");
			errDecoration.show();
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		addButton = createButton(parent, IDialogConstants.OK_ID, "&Add Server", true);
		addButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	public Boolean serverExists(String serverName) {
		List<GlusterServer> servers = GlusterDataModelManager.getInstance().getModel().getCluster().getServers();
		for (GlusterServer server : servers) {
			if (server.getName().equalsIgnoreCase(serverName)) {
				return false;
			}
		}
		return true;
	}

	protected void okPressed() {
		GlusterServersClient serversClient = new GlusterServersClient();
		GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

		try {
			String serverNameText = serverName.getText();
			URI newServerURI = serversClient.addServer(serverNameText);

			modelManager.addGlusterServer(serversClient.getGlusterServer(newServerURI));

			MessageDialog
					.openInformation(getShell(), "Add Server", "Server " + serverNameText + " added successfully!");
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Server addition Failed", e.getMessage());
			setReturnCode(RETURN_CODE_ERROR);
		}
		this.close();
	}
}
