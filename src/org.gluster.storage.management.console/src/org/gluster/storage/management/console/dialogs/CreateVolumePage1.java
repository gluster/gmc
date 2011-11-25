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
package org.gluster.storage.management.console.dialogs;

import java.util.Arrays;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Device;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import org.gluster.storage.management.core.model.Volume.NAS_PROTOCOL;
import org.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import org.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import org.gluster.storage.management.core.utils.ValidationUtil;


public class CreateVolumePage1 extends WizardPage {
	public static final String PAGE_NAME = "create.volume.page.1";
	private Text txtName;
	private ComboViewer typeComboViewer;
	private Text txtAccessControl;
	private Text txtCifsUsers;
	private Volume volume = new Volume();
	private Button btnNfs;
	private Button btnCIFS;
	private Button btnStartVolume;
	private Link linkCustomize;
	private List<Device> allDevices;
	private List<Device> selectedDevices;

	/**
	 * Create the wizard.
	 */
	public CreateVolumePage1() {
		super(PAGE_NAME);
		setTitle("Create Volume");
		setDescription("Create a new Volume by choosing bricks from the cluster servers and configuring the volume properties.");
		
		// by default, we create volume with all available disks
		allDevices = GlusterDataModelManager.getInstance().getReadyDevicesOfAllServers();
		selectedDevices = allDevices; // volume.setDisks(allDisks);
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
		
//		createTransportTypeLabel(container);
//		createTransportTypeValueLabel(container);
		
		createDisksLabel(container);
		createDisksCustomizeLink(container);
		
		createNasProtocolLabel(container);
		createNasProtocolCheckboxes(container);
		
		createCifsUserLabel(container);
		createCifsUserText(container);
		
		createEmptyLabel(container);
		createCifsUserInfoLabel(container);
		
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
	
	private void createCifsUserInfoLabel(Composite container) {
		Label lblCifsUserInfo = new Label(container, SWT.TOP);
		lblCifsUserInfo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblCifsUserInfo.setText("(Comma separated list user names)");
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
		lblAccessControl.setText("Allow Access From: ");
	}
	
	private void createCifsUserLabel(Composite container) {
		Label lblAccessControl = new Label(container, SWT.NONE);
		lblAccessControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAccessControl.setText("CIFS Users: ");
	}
	
	private void createCifsUserText(Composite container) {
		txtCifsUsers = new Text(container, SWT.BORDER);
//		txtCifsUsers.setText("testuser1,testuser2,testuser3");
		GridData cifsControlData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		cifsControlData.widthHint = 300;
		txtCifsUsers.setLayoutData(cifsControlData);	
		txtCifsUsers.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validateForm();				
			}
		});
	}

	private void createNasProtocolCheckboxes(Composite container) {
		Button btnGluster = new Button(container, SWT.CHECK);
		btnGluster.setEnabled(false);
		btnGluster.setSelection(true);
		btnGluster.setText("Gluster");
		createEmptyLabel(container);
		
		btnNfs = new Button(container, SWT.CHECK);
		btnNfs.setEnabled(true);
		btnNfs.setSelection(true);
		btnNfs.setText("NFS");
		createEmptyLabel(container);
		
		btnCIFS = new Button(container, SWT.CHECK);
		btnCIFS.setEnabled(true);
		btnCIFS.setSelection(false);
		btnCIFS.setText("CIFS");
		btnCIFS.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateForm();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				validateForm();				
			}
		});
	}

	private void createNasProtocolLabel(Composite container) {
		Label lblNasProtocol = new Label(container, SWT.RIGHT);
		lblNasProtocol.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNasProtocol.setText("Access Protocol: ");
	}

	private void createDisksCustomizeLink(Composite container) {
		linkCustomize = new Link(container, SWT.UNDERLINE_LINK);
		linkCustomize.setText("All Brick(s) (<a>customize</a>)" );
		linkCustomize.setEnabled(false);
		linkCustomize.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						SelectDisksDialog dialog = new SelectDisksDialog(getShell(), allDevices, selectedDevices, txtName.getText().trim());

						dialog.create();
				        if(dialog.open() == Window.OK) {
				        	// user has customized disks. get them from the dialog box.
				        	selectedDevices = dialog.getSelectedDevices();
				        	linkCustomize.setText("" + selectedDevices.size() + " Brick(s) (<a>customize</a>)");
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
		lblDisks.setText("Bricks: ");
	}

	private void createTypeCombo(Composite container) {
		typeComboViewer = new ComboViewer(container, SWT.READ_ONLY);
		Combo typeCombo = typeComboViewer.getCombo();
		GridData typeComboData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		typeCombo.setLayoutData(typeComboData);
		typeComboViewer.setContentProvider(new ArrayContentProvider());
		
		VOLUME_TYPE[] volumeTypes = new VOLUME_TYPE[3];
		volumeTypes[0] = VOLUME_TYPE.DISTRIBUTE;
		volumeTypes[1] = VOLUME_TYPE.REPLICATE;
		volumeTypes[2] = VOLUME_TYPE.STRIPE;
		
		typeComboViewer.setInput(volumeTypes);
		typeCombo.select(0); // default type = Plain Distribute
		typeComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return Volume.getVolumeTypeStr((VOLUME_TYPE)element);
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
		txtName.setTextLimit(32);
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
		volume.setReplicaCount(Volume.DEFAULT_REPLICA_COUNT);
		volume.setStripeCount(Volume.DEFAULT_STRIPE_COUNT);

		volume.setTransportType(TRANSPORT_TYPE.ETHERNET); // Support only for Ethernet
		Set<NAS_PROTOCOL> nasProtocols = new HashSet<Volume.NAS_PROTOCOL>();
		nasProtocols.add(NAS_PROTOCOL.GLUSTERFS);
		nasProtocols.add(NAS_PROTOCOL.NFS);
		
		volume.setAccessControlList(txtAccessControl.getText());

		if (btnNfs.getSelection()) {
			volume.enableNFS();
		} else {
			volume.disableNFS();
		}
		
		if (btnCIFS.getSelection()) {
			volume.enableCifs();
			volume.setCifsUsers(Arrays.asList(txtCifsUsers.getText().split(",")));
		} else {
			volume.disableCifs();
		}
		
		addVolumeBricks();
		return volume;
	}

	private void addVolumeBricks() {
		// first clear existing bricks, if any
		volume.getBricks().clear();
		
		for (Device device : selectedDevices) {
			Brick brick = new Brick(device.getServerName(), BRICK_STATUS.ONLINE, device.getMountPoint() + "/"
					+ volume.getName());
			volume.addBrick(brick);
		}
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
		validateCifsUsers();
		validateAccessControl();
		validateDisks();
	}

	private void validateDisks() {
		int diskCount = selectedDevices.size();
		
		if(diskCount  < 1) {
			setError("At least one brick must be selected!");
		}

		VOLUME_TYPE volumeType = (VOLUME_TYPE) ((IStructuredSelection) typeComboViewer
				.getSelection()).getFirstElement();
		if ((volumeType == VOLUME_TYPE.DISTRIBUTED_REPLICATE || volumeType == VOLUME_TYPE.REPLICATE ) && diskCount % 2 != 0) {
			setError("Mirror type volume requires bricks in multiples of two");
		} else if ((volumeType == VOLUME_TYPE.DISTRIBUTED_STRIPE || volumeType == VOLUME_TYPE.STRIPE) && diskCount % 4 != 0) {
			setError("Stripe type volume requires bricks in multiples of four");
		}
	}
	
	private void validateAccessControl() {
		String accessControl = txtAccessControl.getText().trim();
		if (accessControl.length() == 0) {
			setError("Please enter Access Control");
			return;
		}
		
		if (!ValidationUtil.isValidAccessControl(accessControl)) {
			setError("Invalid IP address/Host name [" + ValidationUtil.getInvalidIpOrHostname(accessControl)
					+ "]. Please enter a valid value!");
		}
	}
	
	
	private void validateCifsUsers() {
		if (btnCIFS.getSelection()) {
			String cifsUserList = txtCifsUsers.getText().trim();
			if (cifsUserList.length() == 0) {
				setError("Please enter cifs user name");
				return;
			}
		}
	}

	private void validateVolumeName() {
		String volumeName = txtName.getText().trim();
		String volumeNameToken = "^[a-zA-Z][a-zA-Z0-9\\-]*";
		
		if (volumeName.length() > 0) {
			linkCustomize.setEnabled(true);
		}
		
		if(volumeName.length() == 0) {
			setError("Please enter Volume Name");
			linkCustomize.setEnabled(false);
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
