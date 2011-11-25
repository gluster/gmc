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
package org.gluster.storage.management.console.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.gluster.storage.management.console.utils.GUIHelper;


public class InitDiskDialog extends Dialog  {
	
	private Combo formatTypeCombo = null;
	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Composite initializeDiskTypeComposite;
	private Composite composite;
	private String fsType;
	private String mountPoint;
	private Text mountPointText;
	private String deviceName;
	private List<String> fsTypes; 
    private static final String DEFAULT_MOUNT_POINT = "/export/";

	public InitDiskDialog(Shell parentShell, String deviceName, List<String> fsTypes) {
		super(parentShell);
		this.fsTypes = fsTypes;
		this.deviceName = deviceName;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		newShell.setText("Gluster Management Console - Select File System Type");
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

		createLabel(initializeDiskTypeComposite, "File system ");
		createFormatTypeCombo(initializeDiskTypeComposite);
		createLabel(initializeDiskTypeComposite, "Mount point ");
		createMountPointText(initializeDiskTypeComposite);
		createChangeLink(initializeDiskTypeComposite);
	}
	
	private void createLabel(Composite composite, String labelText) {
		Label formatTypeLabel = new Label(composite, SWT.NONE);
		formatTypeLabel.setText(labelText);
		formatTypeLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
	}
	
	private void createFormatTypeCombo(Composite composite) {
		formatTypeCombo = new Combo(composite, SWT.READ_ONLY);
		formatTypeCombo.setItems(fsTypes.toArray(new String[0]));
		formatTypeCombo.select(0);
		new Label(composite, SWT.NONE);
	}
	
	private void createMountPointText(Composite container) {
		mountPointText = new Text(container, SWT.BORDER);
		GridData txtNameData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		txtNameData.widthHint = 400;
		mountPointText.setTextLimit(100);
		mountPointText.setLayoutData(txtNameData);
		mountPointText.setText(DEFAULT_MOUNT_POINT + deviceName);
		mountPointText.setEnabled(false);
		mountPointText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateMountPoint();
			}
		});
	}
	
	private void createChangeLink(Composite container) {
		final Hyperlink changeLink = new Hyperlink(container, SWT.UNDERLINE_SINGLE);
		changeLink.setText("change");
		changeLink.setUnderlined(true);
		changeLink.setForeground(new Color(Display.getDefault(), 0, 0, 255));
		
		changeLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (!mountPointText.isEnabled()) {
					changeLink.setVisible(false);
					mountPointText.setEnabled(true);
				}
			}
		});
	}

	@Override
	protected void okPressed() {
		fsType = formatTypeCombo.getText().trim();
		mountPoint = mountPointText.getText().trim();
		if (validateForm()) {
			super.okPressed();
		} else {
			MessageDialog.openError(getShell(), "Initialize Disk - Error", "Please enter a valid mount point"); 
		}
	}
	
	@Override
	public void cancelPressed() {
		super.cancelPressed();
	}
	
	private boolean validateMountPoint() {
		String mountPoint = mountPointText.getText().trim();
		if (mountPoint.isEmpty()) {
			return false;
		}
		return mountPoint.matches("^/.+");
	}

	private boolean validateForm() {
		return (!formatTypeCombo.getText().trim().isEmpty() && validateMountPoint());
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
		return fsType;
	}

	public String getMountPoint() {
		return mountPoint;
	}
}
