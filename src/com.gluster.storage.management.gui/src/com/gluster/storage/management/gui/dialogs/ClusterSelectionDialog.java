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
package com.gluster.storage.management.gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;

/**
 * Cluster selection dialog, which prompts for the cluster name to be managed
 */
public class ClusterSelectionDialog extends Dialog {
	protected enum CLUSTER_MODE { SELECT, CREATE, REGISTER };
	
	private Combo clusterNameCombo = null;
	private Text newClusterNameText = null;
	private Text existingClusterNameText = null;
	private Text serverNameText = null;
	private Button okButton;

	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Composite composite;
	private ControlDecoration newClusterNameErrorDecoration;
	private ControlDecoration existingClusterNameErrorDecoration;
	private ControlDecoration serverNameErrorDecoration;
	private List<String> clusters;
	private Button selectButton;
	private Button createButton;
	private Button registerButton;
	private Composite clusterSelectionComposite;
	private Composite clusterCreationComposite;
	private Composite clusterRegisterComposite;
	private StackLayout stackLayout;

	private String clusterName;
	private CLUSTER_MODE clusterMode;
	private String serverName;

	public ClusterSelectionDialog(Shell parentShell, List<String> clusters) {
		super(parentShell);
		this.clusters = clusters;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Gluster Management Console - Select Cluster");
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

	private void createClusterNameLabel(Composite composite) {
		Label clusterNameLabel = new Label(composite, SWT.NONE);
		clusterNameLabel.setText("Cluster &Name:");
		clusterNameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}

	private void createClusterNameCombo(Composite composite) {
		clusterNameCombo = new Combo(composite, SWT.READ_ONLY);
		clusterNameCombo.setItems(clusters.toArray(new String[0]));
		clusterNameCombo.select(0);
	}

	private void configureDialogLayout(Composite composite) {
		GridLayout layout = (GridLayout) composite.getLayout();
		layout.numColumns = 3;
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
		// Makes sure that child composites inherit the same background
		parent.setBackgroundMode(SWT.INHERIT_FORCE); 

		composite = (Composite) super.createDialogArea(parent);
		configureDialogLayout(composite);

		createRadioButtons();
		createSubComposites();

		return composite;
	}

	private void createSubComposites() {
		Composite subComposite = new Composite(composite, SWT.NONE);
		GridData data = new GridData();
		data.horizontalSpan = 3;
		subComposite.setLayoutData(data);
		stackLayout = new StackLayout();
		subComposite.setLayout(stackLayout);
		
		createClusterSelectionComposite(subComposite, stackLayout);
		createClusterCreationComposite(subComposite);
		createClusterRegisterComposite(subComposite);
		
		createRadioButtonListeners(subComposite);
		if(clusters.size() > 0) {
			selectButton.setSelection(true);
			stackLayout.topControl = clusterSelectionComposite;
		} else {
			createButton.setSelection(true);
			stackLayout.topControl = clusterCreationComposite;
		}
		subComposite.layout();
	}

	private void createClusterRegisterComposite(Composite composite) {
		clusterRegisterComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		clusterRegisterComposite.setLayout(layout);

		createClusterNameLabel(clusterRegisterComposite);
		existingClusterNameText = createText(clusterRegisterComposite);
		existingClusterNameText.setToolTipText("Enter a name for the cluster being registered.");
		existingClusterNameErrorDecoration = createErrorDecoration(existingClusterNameText, "Please enter a cluster name!");
		existingClusterNameErrorDecoration.show();
		
		createClusterServerLabel(clusterRegisterComposite);
		serverNameText = createText(clusterRegisterComposite);
		serverNameText.setToolTipText("Enter host name / IP address of one of the servers of the cluster.");
		serverNameErrorDecoration = createErrorDecoration(serverNameText, "Please enter a server name!");
		serverNameErrorDecoration.show();
	}

	private void createClusterServerLabel(Composite composite) {
		Label serverNameLabel = new Label(composite, SWT.NONE);
		serverNameLabel.setText("Server Na&me:");
		serverNameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}

	private void createClusterCreationComposite(Composite subComposite) {
		clusterCreationComposite = new Composite(subComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		clusterCreationComposite.setLayout(layout);

		createClusterNameLabel(clusterCreationComposite);
		newClusterNameText = createText(clusterCreationComposite);
		newClusterNameText.setToolTipText("Enter name of the cluster to be created");
		newClusterNameErrorDecoration = createErrorDecoration(newClusterNameText, "Please enter cluster name!");
		newClusterNameErrorDecoration.show();
	}

	private Text createText(Composite parent) {
		Text text = new Text(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, GridData.FILL, true, false);
		int width = convertWidthInCharsToPixels(32);
		layoutData.widthHint = width;
		layoutData.minimumWidth = width;
		text.setLayoutData(layoutData);
		
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		
		return text;
	}

	private void createClusterSelectionComposite(Composite subComposite, StackLayout stackLayout) {
		clusterSelectionComposite = new Composite(subComposite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		clusterSelectionComposite.setLayout(layout);
		createClusterNameLabel(clusterSelectionComposite);
		createClusterNameCombo(clusterSelectionComposite);
		stackLayout.topControl = clusterSelectionComposite;
	}

	private void createRadioButtons() {
		{
			if (clusters.size() > 0) {
				selectButton = new Button(composite, SWT.RADIO);
				selectButton.setText("&Select");
			}
		}
		{
			createButton = new Button(composite, SWT.RADIO);
			createButton.setText("&Create");
		}
		{
			registerButton = new Button(composite, SWT.RADIO);
			registerButton.setText("&Register");
		}
	}
	
	private void validate() {
		okButton.setEnabled(false);
		
		if(selectButton.getSelection()) {
			okButton.setEnabled(true);
			return;
		}
		
		if(createButton.getSelection()) {
			String newClusterName = newClusterNameText.getText().trim(); 
			if(newClusterName.isEmpty()) {
				newClusterNameErrorDecoration.setDescriptionText("Please enter a cluster name!");
				newClusterNameErrorDecoration.show();
			} else if(clusters.contains(newClusterName)) {
				newClusterNameErrorDecoration.setDescriptionText("Cluster [" + newClusterName + "] already exists!");
				newClusterNameErrorDecoration.show();
			} else {
				okButton.setEnabled(true);
				newClusterNameErrorDecoration.hide();
			}
		}
		
		if(registerButton.getSelection()) {
			okButton.setEnabled(true);
			String clusterName = existingClusterNameText.getText().trim(); 
			if(existingClusterNameText.getText().trim().isEmpty()) {
				existingClusterNameErrorDecoration.setDescriptionText("Please enter a cluster name!");
				existingClusterNameErrorDecoration.show();
				okButton.setEnabled(false);
			} else if(clusters.contains(clusterName)) {
				existingClusterNameErrorDecoration.setDescriptionText("Cluster [" + clusterName + "] already exists!");
				existingClusterNameErrorDecoration.show();
				okButton.setEnabled(false);
			} else {
				existingClusterNameErrorDecoration.hide();
			}
			
			if(serverNameText.getText().trim().isEmpty()) {
				serverNameErrorDecoration.show();
				okButton.setEnabled(false);
			} else {
				serverNameErrorDecoration.hide();
			}
		}
	}

	private void createRadioButtonListeners(final Composite parent) {
		if (clusters.size() > 0) {
			selectButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					stackLayout.topControl = clusterSelectionComposite;
					clusterNameCombo.select(0);
					validate();
					parent.layout();
					clusterNameCombo.setFocus();
				}
			});
		}
		createButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stackLayout.topControl = clusterCreationComposite;
				validate();
				parent.layout();
				newClusterNameText.setFocus();
			}
		});
		registerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stackLayout.topControl = clusterRegisterComposite;
				validate();
				parent.layout();
				existingClusterNameText.setFocus();
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		setupDataBinding();
	}
	
	private ControlDecoration createErrorDecoration(Text text, String message) {
		ControlDecoration errorDecoration = guiHelper.createErrorDecoration(text);
		errorDecoration.setDescriptionText(message);
		errorDecoration.hide();
		return errorDecoration;
	}

	/**
	 * Sets up data binding between the text fields and the connection details object. Also attaches a "string required"
	 * validator to the "password" text field. This validator is configured to do the following on validation failure<br>
	 * <li>show an ERROR decorator</li><li>disable the "Login" button
	 */
	private void setupDataBinding() {
		clusterNameCombo.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(clusterNameCombo.getText().trim().isEmpty()) {
					okButton.setEnabled(false);
					newClusterNameErrorDecoration.show();
				} else {
					okButton.setEnabled(true);
					newClusterNameErrorDecoration.hide();
				}
			}
		});
	}
	
	@Override
	protected void okPressed() {
		if(selectButton.getSelection()) {
			clusterMode = CLUSTER_MODE.SELECT;
			clusterName = clusterNameCombo.getText();
		} else if(createButton.getSelection()) {
			clusterMode = CLUSTER_MODE.CREATE;
			clusterName = newClusterNameText.getText().trim();
		} else if(registerButton.getSelection()) {
			clusterMode = CLUSTER_MODE.REGISTER;
			clusterName = existingClusterNameText.getText().trim();
			serverName = serverNameText.getText().trim();
		}
		super.okPressed();
	}

	public String getClusterName() {
		return clusterName;
	}

	public CLUSTER_MODE getClusterMode() {
		return clusterMode;
	}

	public String getServerName() {
		return serverName;
	}
}
