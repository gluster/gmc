/**
 * ChangePasswordDialog.java
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

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.UsersClient;
import com.gluster.storage.management.core.model.ConnectionDetails;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.validators.StringRequiredValidator;

public class ChangePasswordDialog extends Dialog {
	public static final int RETURN_CODE_ERROR = 2;
	private Text oldPassword;
	private Text newPassword;
	private Text confirmPassword;
	private Button okButton;

	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Composite composite;

	private final ConnectionDetails connectionDetails = new ConnectionDetails("gluster", "");

	public ChangePasswordDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Gluster Management Console");
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

	private Text createPasswordText(Composite composite) {
		Text field = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		GridData layoutData = new GridData(SWT.FILL, GridData.FILL, true, false);
		layoutData.widthHint = convertWidthInCharsToPixels(32);
		field.setLayoutData(layoutData);
		return field;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackgroundImage(guiHelper.getImage(IImageKeys.DIALOG_SPLASH_IMAGE));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		composite = (Composite) super.createDialogArea(parent);
		configureDialogLayout(composite);

		createLabel(composite, "Old Password:");
		oldPassword = createPasswordText(composite);

		createLabel(composite, "New Password:");
		newPassword = createPasswordText(composite);
		
		createLabel(composite, "Confirm Password:");
		confirmPassword = createPasswordText(composite);
		
		createListeners();

		return composite;
	}

	/**
	 * 
	 */
	private void createListeners() {
		ModifyListener listener = new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				updateButtonStatus();
			}
		};
		
		oldPassword.addModifyListener(listener);
		newPassword.addModifyListener(listener);
		confirmPassword.addModifyListener(listener);
	}

	private void updateButtonStatus() {
		String oldPwd = oldPassword.getText();
		String newPwd = newPassword.getText();
		String confirmPwd = confirmPassword.getText();
		if(oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
			okButton.setEnabled(false);
			return;
		}
		
		if(!newPwd.equals(confirmPwd)) {
			okButton.setEnabled(false);
			return;
		}
		
		if (newPwd.length() < 4 ) { // Minimum  password length is 4
			okButton.setEnabled(false);
			return;
		}
		okButton.setEnabled(true);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&Change", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		setupDataBinding();
	}

	public class ConfirmPasswordValidator extends StringRequiredValidator {
		public ConfirmPasswordValidator(String errorText, ControlDecoration controlDecoration, Control linkedControl) {
			super(errorText, controlDecoration, linkedControl);
		}

		@Override
		public IStatus validate(Object value) {

			IStatus status = super.validate(value);
			if (status.isOK()) {
				if (!value.equals(newPassword.getText())) {
					return ValidationStatus.error("Passwords do not match");
				}
			}
			return status;
		}
	};

	private void setupDataBinding() {
		DataBindingContext dataBindingContext = new DataBindingContext(SWTObservables.getRealm(Display.getCurrent()));
		UpdateValueStrategy passwordBindingStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		UpdateValueStrategy newPwdBindingStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		UpdateValueStrategy confirmPwdBindingStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);

		// The Validator shows error decoration and disables OK button on
		// validation failure
		passwordBindingStrategy.setBeforeSetValidator(new StringRequiredValidator("Please enter old password!",
				guiHelper.createErrorDecoration(oldPassword), null));

		dataBindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(oldPassword),
				PojoProperties.value("password").observe(connectionDetails), passwordBindingStrategy,
				passwordBindingStrategy);

		newPwdBindingStrategy.setBeforeSetValidator(new StringRequiredValidator("Please enter new password!", guiHelper
				.createErrorDecoration(newPassword), null));

		dataBindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(newPassword),
				PojoProperties.value("newPassword").observe(connectionDetails), newPwdBindingStrategy,
				newPwdBindingStrategy);

		confirmPwdBindingStrategy.setBeforeSetValidator(new ConfirmPasswordValidator("Please enter confirm password!",
				guiHelper.createErrorDecoration(confirmPassword), null));

		dataBindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(confirmPassword),
				PojoProperties.value("confirmNewPassword").observe(connectionDetails), confirmPwdBindingStrategy,
				confirmPwdBindingStrategy);
	}

	protected void okPressed() {
		String user = connectionDetails.getUserId();
		String oldPassword = connectionDetails.getPassword();
		String newPassword = connectionDetails.getNewPassword();

		UsersClient usersClient = new UsersClient();
		try {
			usersClient.changePassword(user, oldPassword, newPassword);
			MessageDialog.openInformation(getShell(), "Change password", "Password changed successfully!");
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Change password Failed", e.getMessage());
			setReturnCode(RETURN_CODE_ERROR);
		}
		this.close();
	}
}
