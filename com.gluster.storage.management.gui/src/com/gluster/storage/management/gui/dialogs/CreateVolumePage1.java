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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;

public class CreateVolumePage1 extends WizardPage {
	private static final String PAGE_NAME = "create.volume.page.1";
	private Text txtName;
	private Text txtAccessControl;

	/**
	 * Create the wizard.
	 */
	public CreateVolumePage1() {
		super(PAGE_NAME);
		setTitle("Create Volume");
		setDescription("Create a new Volume by choosing disks from the cluster servers and configuring the volume properties.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.verticalSpacing = 10;
		gl_container.marginHeight = 10;
		gl_container.marginLeft = 20;
		gl_container.horizontalSpacing = 10;
		container.setLayout(gl_container);
		
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblName = new Label(container, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name: ");
		
		txtName = new Text(container, SWT.BORDER);
		GridData txtNameData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		txtNameData.widthHint = 300;
		txtName.setLayoutData(txtNameData);
		
		Label lblType = new Label(container, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type: ");
		
		ComboViewer typeComboViewer = new ComboViewer(container, SWT.READ_ONLY);
		Combo typeCombo = typeComboViewer.getCombo();
		GridData typeComboData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		typeCombo.setLayoutData(typeComboData);
		typeComboViewer.setContentProvider(new ArrayContentProvider());
		typeComboViewer.setInput(Volume.VOLUME_TYPE.values());
		typeCombo.select(VOLUME_TYPE.PLAIN_DISTRIBUTE.ordinal()); // default type = Plain Distribute
		typeComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				VOLUME_TYPE volumeType = (VOLUME_TYPE)element;
				return Volume.getVolumeTypeStr(volumeType);
			}
		});
		
		Label lblTransportType = new Label(container, SWT.NONE);
		lblTransportType.setText("Transport Type: ");
		
		Label lblEthernet = new Label(container, SWT.NONE);
		lblEthernet.setText("Ethernet");
		
		Label lblDisks = new Label(container, SWT.RIGHT);
		lblDisks.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDisks.setText("Disks: ");
		
		Link linkCustomize = new Link(container, SWT.UNDERLINE_LINK);
		linkCustomize.setText("All Disks (<A>customize</A>)");
		linkCustomize.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				SelectDisksDialog dialog = new SelectDisksDialog(getShell());
				dialog.create();
		        dialog.open();
			}
		});

		Label lblNasProtocol = new Label(container, SWT.RIGHT);
		lblNasProtocol.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNasProtocol.setText("NAS Protocol: ");
		
		Button btnGluster = new Button(container, SWT.CHECK);
		btnGluster.setEnabled(false);
		btnGluster.setSelection(true);
		btnGluster.setText("Gluster");
		new Label(container, SWT.NONE);
		
		Button btnNfs = new Button(container, SWT.CHECK);
		btnNfs.setSelection(true);
		btnNfs.setText("NFS");
		
		Label lblAccessControl = new Label(container, SWT.NONE);
		lblAccessControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAccessControl.setText("Access Control: ");
		
		txtAccessControl = new Text(container, SWT.BORDER);
		txtAccessControl.setText("*");
		GridData accessControlData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		accessControlData.widthHint = 300;
		txtAccessControl.setLayoutData(accessControlData);

		new Label(container, SWT.NONE);
		Label lblAccessControlInfo = new Label(container, SWT.TOP);
		lblAccessControlInfo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblAccessControlInfo.setText("(Comma separated list of IP addresses)");
	}
}
