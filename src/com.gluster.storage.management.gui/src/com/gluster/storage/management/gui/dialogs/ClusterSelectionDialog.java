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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;

/**
 * Cluster selection dialog, which prompts for the cluster name to be managed
 */
public class ClusterSelectionDialog extends Dialog {
	private Combo clusterNameCombo = null;
	private Button okButton;

	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Composite composite;
	private ControlDecoration errorDecoration;
	private List<String> clusters;
	private String clusterName;

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
		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&Cluster to Manage:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}

	private void createClusterNameCombo(Composite composite) {
		clusterNameCombo = new Combo(composite, SWT.BORDER);
		clusterNameCombo.setItems(clusters.toArray(new String[0]));
		clusterNameCombo.select(0);

//		GridData layoutData = new GridData(SWT.FILL, GridData.FILL, true, false);
//		layoutData.widthHint = convertWidthInCharsToPixels(32);
//		clusterNameCombo.setLayoutData(layoutData);
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
		// Makes sure that child composites inherit the same background
		parent.setBackgroundMode(SWT.INHERIT_FORCE); 

		composite = (Composite) super.createDialogArea(parent);
		configureDialogLayout(composite);

		createClusterNameLabel(composite);
		createClusterNameCombo(composite);
		createErrorDecoration();

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		setupDataBinding();
	}
	
	private void createErrorDecoration() {
		errorDecoration = guiHelper.createErrorDecoration(clusterNameCombo);
		errorDecoration.setDescriptionText("Please select an existing cluster name, or enter a new one!");
		errorDecoration.hide();
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
					errorDecoration.show();
				} else {
					okButton.setEnabled(true);
					errorDecoration.hide();
				}
			}
		});
	}
	
	@Override
	protected void okPressed() {
		clusterName = clusterNameCombo.getText();
		super.okPressed();
	}

	public String getClusterName() {
		return clusterName;
	}
}
