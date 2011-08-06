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
package com.gluster.storage.management.console.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
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

import com.gluster.storage.management.client.ClustersClient;
import com.gluster.storage.management.client.UsersClient;
import com.gluster.storage.management.console.Activator;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.IImageKeys;
import com.gluster.storage.management.console.dialogs.ClusterSelectionDialog.CLUSTER_MODE;
import com.gluster.storage.management.console.preferences.PreferenceConstants;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.validators.StringRequiredValidator;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.ConnectionDetails;

/**
 * Login dialog, which prompts for the user's account info, and has Login and Cancel buttons.
 */
public class LoginDialog extends Dialog {
	public static final int RETURN_CODE_ERROR = 2;
	private Text userIdText = null;
	private Text passwordText = null;
	private Button okButton;

	private final ConnectionDetails connectionDetails = new ConnectionDetails("gluster", "");
	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Composite composite;

	public LoginDialog(Shell parentShell) {
		super(parentShell);
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

	private void createUserIdLabel(Composite composite) {
		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&User ID:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}

	private void createUserIdText(Composite composite) {
		userIdText = new Text(composite, SWT.BORDER);
		userIdText.setText("gluster");
		userIdText.setEnabled(false);

		GridData layoutData = new GridData(SWT.FILL, GridData.FILL, true, false);
		;
		layoutData.widthHint = convertWidthInCharsToPixels(32);
		userIdText.setLayoutData(layoutData);
	}

	private void createPasswordLabel(Composite composite) {
		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("&Password:");
		passwordLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}

	private void createPasswordText(Composite composite) {
		passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);

		GridData layoutData = new GridData(SWT.FILL, GridData.FILL, true, false);
		;
		layoutData.widthHint = convertWidthInCharsToPixels(32);
		passwordText.setLayoutData(layoutData);
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

	/**
	 * Overriding to make sure that the dialog is centered in screen
	 */
	@Override
	protected void initializeBounds() {
		super.initializeBounds();

		guiHelper.centerShellInScreen(getShell());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setBackgroundImage(guiHelper.getImage(IImageKeys.DIALOG_SPLASH_IMAGE));
		parent.setBackgroundMode(SWT.INHERIT_FORCE); // Makes sure that child
														// composites inherit
														// the same background

		composite = (Composite) super.createDialogArea(parent);
		configureDialogLayout(composite);

		createUserIdLabel(composite);
		createUserIdText(composite);

		createPasswordLabel(composite);
		createPasswordText(composite);

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, "&Login", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		setupDataBinding();
	}

	/**
	 * Sets up data binding between the text fields and the connection details object. Also attaches a "string required"
	 * validator to the "password" text field. This validator is configured to do the following on validation failure<br>
	 * <li>show an ERROR decorator</li><li>disable the "Login" button
	 */
	private void setupDataBinding() {
		DataBindingContext dataBindingContext = new DataBindingContext(SWTObservables.getRealm(Display.getCurrent()));
		UpdateValueStrategy passwordBindingStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);

		// The Validator shows error decoration and disables OK button on
		// validation failure
		passwordBindingStrategy.setBeforeSetValidator(new StringRequiredValidator("Please enter password!", guiHelper
				.createErrorDecoration(passwordText), okButton));

		dataBindingContext.bindValue(WidgetProperties.text(SWT.Modify).observe(passwordText),
				PojoProperties.value("password").observe(connectionDetails), passwordBindingStrategy,
				passwordBindingStrategy);

		UpdateValueStrategy userIdBindingStrategy = new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE);
		dataBindingContext
				.bindValue(WidgetProperties.text(SWT.Modify).observe(userIdText), PojoProperties.value("userId")
						.observe(connectionDetails), userIdBindingStrategy, userIdBindingStrategy);
	}

	protected void okPressed() {
		String user = connectionDetails.getUserId();
		String password = connectionDetails.getPassword();

		UsersClient usersClient = new UsersClient();
		try {
			usersClient.authenticate(user, password);
		} catch(Exception e) {
			MessageDialog.openError(getShell(), "Authentication Failed", e.getMessage());
			setReturnCode(RETURN_CODE_ERROR);
			return;
		}
		
		// authentication successful. close the login dialog and open the next one.
		close();

		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		boolean showClusterSelectionDialog = preferenceStore.getBoolean(PreferenceConstants.P_SHOW_CLUSTER_SELECTION_DIALOG);

		// If the password is default, Let user to change the password
		if (password.equalsIgnoreCase(CoreConstants.DEFAULT_PASSWORD)) {
			String oldSecurityTokeString = GlusterDataModelManager.getInstance().getSecurityToken();
			ChangePasswordDialog dialog = new ChangePasswordDialog(getShell());

			if (dialog.open() == Dialog.CANCEL) {
				MessageDialog.openError(getShell(), "Change password Cancelled",
						"Password must be changed on first login!" + CoreConstants.NEWLINE + "Application will close.");
				cancelPressed();
				return;
			}
			
			// after first login, cluster selection dialog must be shown.
			showClusterSelectionDialog = true;
		}

		ClustersClient clustersClient = new ClustersClient();

		CLUSTER_MODE mode;
		String clusterName = null;
		if (!showClusterSelectionDialog) {
			clusterName = preferenceStore.getString(PreferenceConstants.P_DEFAULT_CLUSTER_NAME);
			if (clusterName == null || clusterName.isEmpty()) {
				// Cluster name not available in preferences. Hence we must show the cluster selection dialog.
				showClusterSelectionDialog = true;
			} else {
				mode = CLUSTER_MODE.SELECT;
			}
		}

		String serverName = null;

		if (showClusterSelectionDialog) {
			ClusterSelectionDialog clusterDialog = new ClusterSelectionDialog(getParentShell(),
					clustersClient.getClusterNames());
			int userAction = clusterDialog.open();
			if (userAction == Window.CANCEL) {
				MessageDialog.openError(getShell(), "Login Cancelled",
						"User cancelled login at cluster selection. Application will close!");
				cancelPressed();
				return;
			}
			mode = clusterDialog.getClusterMode();
			clusterName = clusterDialog.getClusterName();
			serverName = clusterDialog.getServerName();
		} else {
			mode = CLUSTER_MODE.SELECT;
		}

		try {
			createOrRegisterCluster(clustersClient, clusterName, serverName, mode);
			
			final String clusterName1 =  clusterName;
			new ProgressMonitorDialog(getShell()).run(true, false, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					GlusterDataModelManager.getInstance().initializeModel(clusterName1, monitor);
				}
			});
			super.okPressed();
		} catch (Exception e) {
			String errMsg;
			if(e instanceof InvocationTargetException) {
				errMsg = ((InvocationTargetException) e).getTargetException().getMessage();
			} else {
				errMsg = e.getMessage();
			}
			setReturnCode(RETURN_CODE_ERROR);
			MessageDialog.openError(getShell(), "Gluster Management Console", errMsg);
		}
	}

	public void createOrRegisterCluster(ClustersClient clustersClient, String clusterName, String serverName,
			CLUSTER_MODE mode) {
		switch (mode) {
		case SELECT:
			return;
		case CREATE:
			clustersClient.createCluster(clusterName);
			break;
		case REGISTER:
			clustersClient.registerCluster(clusterName, serverName);
			break;
		}		
	}
}
