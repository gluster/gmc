/*******************************************************************************
 * 
 * InitializeDiskTypeSelection.java
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
 *******************************************************************************/
package com.gluster.storage.management.gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class InitializeDiskTypeSelection extends Dialog  {
	
	private Combo formatTypeCombo = null;
	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Composite initializeDiskTypeComposite;
	private Composite composite;
	private String fsType; 

	public InitializeDiskTypeSelection(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
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
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// Makes sure that child composites inherit the same background
		parent.setBackgroundMode(SWT.INHERIT_FORCE); 
		
		composite =  (Composite) super.createDialogArea(parent);
		configureDialogLayout(composite);
		createComposite(composite);
		return composite;
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
	
	private void createComposite(Composite composite) {
		initializeDiskTypeComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		initializeDiskTypeComposite.setLayout(layout);

		createLabel(initializeDiskTypeComposite, "Format disk using ");
		createFormatTypeCombo(initializeDiskTypeComposite);
		createLabel(initializeDiskTypeComposite, " file system");
	}
	
	private void createLabel(Composite composite, String labelText) {
		Label formatTypeLabel = new Label(composite, SWT.NONE);
		formatTypeLabel.setText(labelText);
		formatTypeLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}
	
	private void createFormatTypeCombo(Composite composite) {
		List<String> fsType = new ArrayList<String>();
		fsType.add(GlusterConstants.FSTYPE_DEFAULT);
		fsType.add(GlusterConstants.FSTYPE_EXT_3);
		fsType.add(GlusterConstants.FSTYPE_EXT_4);
		fsType.add(GlusterConstants.FSTYPE_XFS);
		formatTypeCombo = new Combo(composite, SWT.READ_ONLY);
		formatTypeCombo.setItems(fsType.toArray(new String[0]));
		formatTypeCombo.select(0);
	}
	
	@Override
	protected void okPressed() {
		fsType = formatTypeCombo.getText();
		super.okPressed();
	}
	
	@Override
	public void cancelPressed() {
		super.cancelPressed();
	}
	
	/**
	 * Overriding to make sure that the dialog is centered in screen
	 */
	@Override
	protected void initializeBounds() {
		super.initializeBounds();

		guiHelper.centerShellInScreen(getShell());
	}
	
	public String getFSType() {
		return fsType.trim();
	}

}
