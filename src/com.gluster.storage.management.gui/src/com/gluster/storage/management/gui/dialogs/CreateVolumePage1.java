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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
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
import com.gluster.storage.management.core.response.VolumeListResponse;

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
	private ValidationListener valListener = new ValidationListener();

	/**
	 * Create the wizard.
	 */
	public CreateVolumePage1() {
		super(PAGE_NAME);
		setTitle("Create Volume");
		setDescription("Create a new Volume by choosing disks from the cluster servers and configuring the volume properties.");
		
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

	private class ValidationListener implements ModifyListener {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		@Override
		public void modifyText(ModifyEvent e) {
			String volumeName = txtName.getText().trim();
			String accessControl = txtAccessControl.getText().trim();
			String volumeNameToken = "^[a-zA-Z][a-zA-Z0-9\\-]*";

			
			setErrorMessage(null);
			setPageComplete(true);
			
			if(volumeName.length() == 0) {
				setPageComplete(false);
				setErrorMessage("Please enter Volume Name");
			}
			
		    if (!volumeName.matches(volumeNameToken)) {
		    	setPageComplete(false);
		    	setErrorMessage("Please enter valid Volume Name");
		    }
		    
			if(accessControl.length() == 0) {
				setPageComplete(false);
				setErrorMessage("Please enter Access Control");
			}
			
			if(volume.getDisks().size() < 1) {
				setPageComplete(false);
				setErrorMessage("No disk found");
			}

			// acl validation
			String[] aclList = accessControl.split(",");
			for (String ip : aclList) {
				if (!isValidIP(ip)) {
					setPageComplete(false);
					setErrorMessage("Please enter valid access control list");
				} 
			}
			
		}
		
		private Boolean isValidIP(String ip) {
			// String pattern = "^.[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}";
			String pattern = "^.[0-9]{1,3}/.";
			if (ip == "*") {
				return true;
			}
			String[] ipQuads = ip.split(".");
			for (String quad : ipQuads) {
				if (!quad.matches(pattern)) {
					return false;
				}
			}
			return true;
			
		}
	}
	
	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		setPageComplete(false);
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
		txtName.addModifyListener(valListener);		
		
		Label lblType = new Label(container, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("Type: ");
		
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
		
		Label lblTransportType = new Label(container, SWT.NONE);
		lblTransportType.setText("Transport Type: ");
		
		Label lblEthernet = new Label(container, SWT.NONE);
		lblEthernet.setText("Ethernet");
		
		Label lblDisks = new Label(container, SWT.RIGHT);
		lblDisks.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDisks.setText("Disks: ");
		
		linkCustomize = new Link(container, SWT.UNDERLINE_LINK);
		linkCustomize.setText("All Disk(s) (<a>customize</a>)");
		linkCustomize.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						SelectDisksDialog dialog = new SelectDisksDialog(getShell(), allDisks, volume.getDisks());

						dialog.create();
				        if(dialog.open() == Window.OK) {
				        	// user has customized disks. get them from the dialog box.
				        	volume.setDisks(dialog.getSelectedBricks());
				        	linkCustomize.setText("" + volume.getDisks().size() + " Disk(s) (<a>customize</a>)");
				        }
					}
				});
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
		
		btnNfs = new Button(container, SWT.CHECK);
		btnNfs.setEnabled(false);
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
		txtAccessControl.addModifyListener(valListener);

		new Label(container, SWT.NONE);
		Label lblAccessControlInfo = new Label(container, SWT.TOP);
		lblAccessControlInfo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblAccessControlInfo.setText("(Comma separated list of IP addresses/Hostname)");
		
		Label lblStartVolume = new Label(container, SWT.NONE);
		lblStartVolume.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStartVolume.setText("Start Volume: ");
		
		btnStartVolume = new Button(container, SWT.CHECK);
		btnStartVolume.setSelection(true);
	}

	public Volume getVolume() {
		volume.setName(txtName.getText());
		
		IStructuredSelection selection = (IStructuredSelection)typeComboViewer.getSelection();
		volume.setVolumeType((VOLUME_TYPE)selection.getFirstElement());
		
		volume.setTransportType(TRANSPORT_TYPE.ETHERNET);
		Set<NAS_PROTOCOL> nasProtocols = new HashSet<Volume.NAS_PROTOCOL>();
		nasProtocols.add(NAS_PROTOCOL.GLUSTERFS);
		nasProtocols.add(NAS_PROTOCOL.NFS);
		
//		if(btnNfs.getSelection()) {
//			nasProtocols.add(NAS_PROTOCOL.NFS);
//		}
		
		volume.setAccessControlList(txtAccessControl.getText());
		
		return volume;
	}
	
	public Boolean getStartVolumeRequest() {
		return btnStartVolume.getSelection();
	}
	
	public Boolean isVolumeExist(String volumeName) {
		List<Volume> volumes = GlusterDataModelManager.getInstance().getModel().getCluster().getVolumes();
		for (Volume volume : volumes) {
			if (volume.getName().equals(volumeName)) {
				setErrorMessage("Volume name already exists.");
				return false;
			}
		}
		return true;
	}
	
	public Boolean isValidCreateVolumeForm() {
		IStructuredSelection selection = (IStructuredSelection)typeComboViewer.getSelection();
		if (selection.getFirstElement().equals(VOLUME_TYPE.DISTRIBUTED_MIRROR) && ((int)volume.getDisks().size()) % 2 != 0 ) {
			setErrorMessage("Mirror type volume requires disk in multiples of two");
			return false;
		} else if(selection.getFirstElement().equals(VOLUME_TYPE.DISTRIBUTED_STRIPE) && ((int)volume.getDisks().size()) % 4 != 0) {
			setErrorMessage("Stripe type volume requires disk in multiples of four");
			return false;
		}
		
		if(!isVolumeExist(txtName.getText())) {
			return false;
		}
		
		return true;
	}
}
