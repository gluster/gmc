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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.NAS_PROTOCOL;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.ValidationUtil;

public class CreateVolumePage1 extends WizardPage {
	public static final String PAGE_NAME = "create.volume.page.1";
	private Text txtName;
	private ComboViewer typeComboViewer;
	private Text txtAccessControl;
	private Volume volume = new Volume();
	private List<Disk> allDisks;
	private Button btnNfs;
	private Button btnStartVolume;
	private Link linkCustomize;

	/**
	 * Create the wizard.
	 */
	public CreateVolumePage1() {
		super(PAGE_NAME);
		setTitle("Create Volume");
		setDescription("Create a new Volume by choosing bricks from the cluster servers and configuring the volume properties.");
		
		// by default, we create volume with all available disks
		allDisks = GlusterDataModelManager.getInstance().getReadyDisksOfAllServers();
		volume.setDisks(getBricks(allDisks)); // volume.setDisks(allDisks);
	}
	
	private List<String> getBricks(List<Disk> allDisks) {
		List<String> disks = new ArrayList<String>();
		for(Disk disk: allDisks) {
			disks.add(disk.getServerName() + ":" + disk.getName());
		}
		return disks;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		setPageComplete(false);
		Composite container = createContainer(parent);
		
		createEmptyRow(container);
		
		createNameLabel(container);
		createNameText(container);		
		
		createTypeLabel(container);
		createTypeCombo(container);
		
		createTransportTypeLabel(container);
		createTransportTypeValueLabel(container);
		
		createDisksLabel(container);
		createDisksCustomizeLink(container);
		
		createNasProtocolLabel(container);
		createNasProtocolCheckboxes(container);
		
		createAccessControlLabel(container);
		createAccessControlText(container);

		createEmptyLabel(container);
		createAccessControlInfoLabel(container);
		
		createStartVolumeLabel(container);
		createStartVolumeCheckbox(container);
	}

	private void createStartVolumeCheckbox(Composite container) {
		btnStartVolume = new Button(container, SWT.CHECK);
		btnStartVolume.setSelection(true);
	}

	private void createStartVolumeLabel(Composite container) {
		Label lblStartVolume = new Label(container, SWT.NONE);
		lblStartVolume.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStartVolume.setText("Start Volume: ");
	}

	private void createAccessControlInfoLabel(Composite container) {
		Label lblAccessControlInfo = new Label(container, SWT.TOP);
		lblAccessControlInfo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblAccessControlInfo.setText("(Comma separated list of IP addresses/hostnames)");
	}

	private void createEmptyLabel(Composite container) {
		new Label(container, SWT.NONE);
	}

	private void createAccessControlText(Composite container) {
		txtAccessControl = new Text(container, SWT.BORDER);
		txtAccessControl.setText("*");
		GridData accessControlData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		accessControlData.widthHint = 300;
		txtAccessControl.setLayoutData(accessControlData);		
		txtAccessControl.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateForm();
			}
		});
	}

	private void createAccessControlLabel(Composite container) {
		Label lblAccessControl = new Label(container, SWT.NONE);
		lblAccessControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAccessControl.setText("Access Control: ");
	}

	private void createNasProtocolCheckboxes(Composite container) {
		Button btnGluster = new Button(container, SWT.CHECK);
		btnGluster.setEnabled(false);
		btnGluster.setSelection(true);
		btnGluster.setText("Gluster");
		createEmptyLabel(container);
		
		btnNfs = new Button(container, SWT.CHECK);
		btnNfs.setEnabled(false);
		btnNfs.setSelection(true);
		btnNfs.setText("NFS");
	}

	private void createNasProtocolLabel(Composite container) {
		Label lblNasProtocol = new Label(container, SWT.RIGHT);
		lblNasProtocol.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNasProtocol.setText("NAS Protocol: ");
	}

	private void createDisksCustomizeLink(Composite container) {
		linkCustomize = new Link(container, SWT.UNDERLINE_LINK);
		linkCustomize.setText("All Brick(s) (<a>customize</a>)");
		linkCustomize.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						SelectDisksDialog dialog = new SelectDisksDialog(getShell(), allDisks, volume.getDisks());

						dialog.create();
				        if(dialog.open() == Window.OK) {
				        	// user has customized disks. get them from the dialog box.
				        	volume.setBricks(dialog.getSelectedBricks(volume.getName()));
				        	linkCustomize.setText("" + volume.getDisks().size() + " Brick(s) (<a>customize</a>)");
				        	validateForm();
				        }
					}
				});
			}
		});
	}

	private void createDisksLabel(Composite container) {
		Label lblDisks = new Label(container, SWT.RIGHT);
		lblDisks.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDisks.setText("Disks: ");
	}

	private void createTransportTypeValueLabel(Composite container) {
		Label lblEthernet = new Label(container, SWT.NONE);
		lblEthernet.setText("Ethernet");
	}

	private void createTransportTypeLabel(Composite container) {
		Label lblTransportType = new Label(container, SWT.NONE);
		lblTransportType.setText("Transport Type: ");
	}

	private void createTypeCombo(Composite container) {
		typeComboViewer = new ComboViewer(container, SWT.READ_ONLY);
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
		typeComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validateForm();				
			}
		});
	}

	private void createTypeLabel(Composite container) {
		Label lblType = new Label(container, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type: ");
	}

	private void createNameText(Composite container) {
		txtName = new Text(container, SWT.BORDER);
		GridData txtNameData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		txtNameData.widthHint = 300;
		txtName.setLayoutData(txtNameData);		
		txtName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validateForm();
			}
		});
	}

	private void createNameLabel(Composite container) {
		Label lblName = new Label(container, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setText("Name: ");
	}

	private void createEmptyRow(Composite container) {
		createEmptyLabel(container);
		createEmptyLabel(container);
	}

	private Composite createContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);

		GridLayout gl_container = new GridLayout(2, false);
		gl_container.verticalSpacing = 10;
		gl_container.marginHeight = 10;
		gl_container.marginLeft = 20;
		gl_container.horizontalSpacing = 10;
		container.setLayout(gl_container);
		return container;
	}

	public Volume getVolume() {
		volume.setName(txtName.getText());
		
		IStructuredSelection selection = (IStructuredSelection)typeComboViewer.getSelection();
		volume.setVolumeType((VOLUME_TYPE)selection.getFirstElement());
		
		volume.setTransportType(TRANSPORT_TYPE.ETHERNET);
		Set<NAS_PROTOCOL> nasProtocols = new HashSet<Volume.NAS_PROTOCOL>();
		nasProtocols.add(NAS_PROTOCOL.GLUSTERFS);
		nasProtocols.add(NAS_PROTOCOL.NFS);
		
		volume.setAccessControlList(txtAccessControl.getText());
		
		return volume;
	}
	
	public Boolean startVolumeAfterCreation() {
		return btnStartVolume.getSelection();
	}
	
	public Boolean volumeExists(String volumeName) {
		List<Volume> volumes = GlusterDataModelManager.getInstance().getModel().getCluster().getVolumes();
		for (Volume volume : volumes) {
			if (volume.getName().equals(volumeName)) {
				setErrorMessage("Volume name already exists.");
				return false;
			}
		}
		return true;
	}
	
	private void validateForm() {
		clearErrors();
		validateVolumeName();
		validateAccessControl();
		validateDisks();
	}

	private void validateDisks() {
		int diskCount = volume.getDisks().size();
		
		if(diskCount  < 1) {
			setError("At least one disk must be selected!");
		}

		VOLUME_TYPE volumeType = (VOLUME_TYPE) ((IStructuredSelection) typeComboViewer
				.getSelection()).getFirstElement();
		if (volumeType == VOLUME_TYPE.DISTRIBUTED_MIRROR && diskCount % 2 != 0) {
			setError("Mirror type volume requires disks in multiples of two");
		} else if (volumeType == VOLUME_TYPE.DISTRIBUTED_STRIPE && diskCount % 4 != 0) {
			setError("Stripe type volume requires disks in multiples of four");
		}
	}
	
	private void validateAccessControl() {
		String accessControl = txtAccessControl.getText().trim();
		if (accessControl.length() == 0) {
			setError("Please enter Access Control");
			return;
		}
		
		if (!ValidationUtil.isValidAccessControl(accessControl)) {
			setError("Access control list must be a comma separated list of IP addresses/Host names. Please enter a valid value!");
		}
	}

	private void validateVolumeName() {
		String volumeName = txtName.getText().trim();
		String volumeNameToken = "^[a-zA-Z][a-zA-Z0-9\\-]*";
		if(volumeName.length() == 0) {
			setError("Please enter Volume Name");
		}
		
		if (!volumeName.matches(volumeNameToken)) {
			setError("Please enter valid Volume Name");
		}
		
		if(!volumeExists(volumeName)) {
			setError("Volume [" + volumeName + "] already exists!"); 
		}
	}

	private void clearErrors() {
		setErrorMessage(null);
		setPageComplete(true);
	}
	
	private void setError(String errorMsg) {
		setPageComplete(false);
		setErrorMessage(errorMsg);
	}
}
