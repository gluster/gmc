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
package com.gluster.storage.management.gui.dialogs;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.client.UsersClient;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.ConnectionDetails;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.validators.StringRequiredValidator;

public class ServerAdditionDialog extends Dialog {
	public static final int RETURN_CODE_ERROR = 2;
	private Text serverName;
	private Button addButton;

	private final GUIHelper guiHelper = GUIHelper.getInstance();
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
//		parent.setBackgroundImage(guiHelper.getImage(IImageKeys.DIALOG_SPLASH_IMAGE));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		composite = (Composite) super.createDialogArea(parent);
		configureDialogLayout(composite);

		createLabel(composite, "Server Name:");
		serverName = createServerNameText(composite);

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
		if(serverName.getText().isEmpty()) {
			addButton.setEnabled(false);
			return;
		}
		
		addButton.setEnabled(true);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		addButton = createButton(parent, IDialogConstants.OK_ID, "&Add Server", true);
		addButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		setupDataBinding();
	}

	private void setupDataBinding() {
		DataBindingContext dataBindingContext = new DataBindingContext(SWTObservables.getRealm(Display.getCurrent()));
		UpdateValueStrategy serverNameBindingStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);

		// The Validator shows error decoration and disables OK button on
		// validation failure
		serverNameBindingStrategy.setBeforeSetValidator(new StringRequiredValidator("Please enter server name!",
				guiHelper.createErrorDecoration(serverName), null));

		dataBindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(serverName),
				PojoProperties.value("serverName").observe(serverName.getText()), serverNameBindingStrategy,
				serverNameBindingStrategy);

	}

	protected void okPressed() {
		GlusterServersClient serversClient = new GlusterServersClient();
		try {
			 serversClient.addServer(serverName.getText());
			
			MessageDialog.openInformation(getShell(), "Add Server", "Server added successfully!");
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Server addition Failed", e.getMessage());
			setReturnCode(RETURN_CODE_ERROR);
		}
		this.close();
	}
}
