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
package com.gluster.storage.management.gui.views.details.tabcreators;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.NAS_PROTOCOL;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.BricksPage;
import com.gluster.storage.management.gui.views.details.DisksPage;
import com.gluster.storage.management.gui.views.details.TabCreator;
import com.gluster.storage.management.gui.views.details.VolumeLogsPage;
import com.gluster.storage.management.gui.views.details.VolumeOptionsPage;

public class VolumeTabCreator implements TabCreator {
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	private void createVolumePropertiesSection(final Volume volume, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Properties", null, 3, false);		

		createVolumeTypeField(volume, toolkit, section);

		VOLUME_TYPE volumeType = volume.getVolumeType();
		if (volumeType == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			createReplicaCountField(volume, toolkit, section);
		}

		if (volumeType == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			createStripeCountField(volume, toolkit, section);
		}

		createNumOfDisksField(volume, toolkit, section);
		createDiskSpaceField(volume, toolkit, section);
		createTransportTypeField(volume, toolkit, section);
		createNASProtocolField(volume, toolkit, section);
		createAccessControlField(volume, toolkit, section);
		createStatusField(volume, toolkit, section);	
	}

	private void createDiskSpaceField(final Volume volume, FormToolkit toolkit, Composite section) {
		Label diskSpaceLabel = toolkit.createLabel(section, "Total Disk Space (GB): ", SWT.NONE);
		diskSpaceLabel.setToolTipText("<b>bold</b>normal");
		toolkit.createLabel(section, "" + NumberUtil.formatNumber(volume.getTotalDiskSpace()), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createStatusField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "Status: ", SWT.NONE);
		
		CLabel lblStatusValue = new CLabel(section, SWT.NONE);
		lblStatusValue.setText(volume.getStatusStr());
		lblStatusValue.setImage(volume.getStatus() == Volume.VOLUME_STATUS.ONLINE ? guiHelper
				.getImage(IImageKeys.STATUS_ONLINE) : guiHelper.getImage(IImageKeys.STATUS_OFFLINE));
		
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createTransportTypeField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "Transport Type: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getTransportTypeStr(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createNumOfDisksField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "Number of Disks: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getNumOfDisks(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createStripeCountField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "Stripe Count: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getStripeCount(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createReplicaCountField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "Replica Count: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getReplicaCount(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createVolumeTypeField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "Volume Type: ", SWT.NONE);
		toolkit.createLabel(section, volume.getVolumeTypeStr(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE);
	}

	private void createVolumeAlertsSection(final Volume volume, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Alerts", null, 3, false);
		toolkit.createLabel(section, "Volume related alerts will be displayed here");
	}
	
	private void createVolumeMountingInfoSection(final Volume volume, FormToolkit toolkit, final ScrolledForm form) {
		Composite section = guiHelper.createSection(form, toolkit, "Mounting Information", null, 3, false);
		toolkit.createLabel(section, "Information about mounting the\nvolume will be printed here");
	}

	private GridData createDefaultLayoutData() {
		GridData layoutData = new GridData();
		layoutData.minimumWidth = 150;
		layoutData.widthHint = 150;
		return layoutData;
	}

	private void createAccessControlField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "Access Control: ", SWT.NONE);
		Text accessControlText = toolkit.createText(section, volume.getAccessControlList());
		accessControlText.setLayoutData(createDefaultLayoutData());
		accessControlText.setEnabled(false);
		createChangeLinkForAccessControl(volume, toolkit, section, accessControlText);
	}

	private void createChangeLinkForAccessControl(final Volume volume, FormToolkit toolkit, Composite section,
			final Text accessControlText) {
		final Hyperlink changeLink = toolkit.createHyperlink(section, "change", SWT.NONE);
		changeLink.addHyperlinkListener(new HyperlinkAdapter() {

			private void finishEdit() {
				// TODO: Update value to back-end
				// TODO: Validation of entered text
				volume.setAccessControlList(accessControlText.getText());
				accessControlText.setEnabled(false);
				changeLink.setText("change");
			}
			
			private void startEdit() {
				accessControlText.setEnabled(true);
				changeLink.setText("update");
			}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (accessControlText.isEnabled()) {
					// we were already in edit mode.
					finishEdit();
				} else {
					// Get in to edit mode
					startEdit();
				}
			}
		});
	}

	private void createNASProtocolField(final Volume volume, FormToolkit toolkit, Composite section) {
		toolkit.createLabel(section, "NAS Protocols: ", SWT.NONE);

		Composite nasProtocolsComposite = toolkit.createComposite(section);
		nasProtocolsComposite.setLayout(new FillLayout());
		
		createCheckbox(toolkit, nasProtocolsComposite, "Gluster", true);
		final Button nfsCheckBox = createCheckbox(toolkit, nasProtocolsComposite, "NFS", volume.getNASProtocols().contains(NAS_PROTOCOL.NFS));

		createChangeLinkForNASProtocol(volume, toolkit, section, nfsCheckBox);
	}

	private void createChangeLinkForNASProtocol(final Volume volume, FormToolkit toolkit, Composite section,
			final Button nfsCheckBox) {
		final Hyperlink changeLink = toolkit.createHyperlink(section, "change", SWT.NONE);
		changeLink.addHyperlinkListener(new HyperlinkAdapter() {

			private void finishEdit() {
				// TODO: Update value to back-end
				if (nfsCheckBox.getSelection()) {
					volume.enableNFS();
				} else {
					volume.disableNFS();
				}
				nfsCheckBox.setEnabled(false);
				changeLink.setText("change");
			}
			
			private void startEdit() {
				nfsCheckBox.setEnabled(true);
				changeLink.setText("update");
			}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (nfsCheckBox.isEnabled()) {
					// we were already in edit mode.
					finishEdit();
				} else {
					// Get in to edit mode
					startEdit();
				}
			}
		});
	}

	private Button createCheckbox(FormToolkit toolkit, Composite parent, String label, boolean selected) {
		final Button checkBox = toolkit.createButton(parent, label, SWT.CHECK);
		checkBox.setEnabled(false);
		checkBox.setSelection(selected);
		return checkBox;
	}
	
	private void createVolumePropertiesTab(Volume volume, TabFolder tabFolder, FormToolkit toolkit) {
		Composite volumeTab = guiHelper.createTab(tabFolder, volume.getName() + " - Properties", IImageKeys.VOLUME);
		final ScrolledForm form = guiHelper.setupForm(volumeTab, toolkit, "Volume Properties [" + volume.getName() + "]");
		createVolumePropertiesSection(volume, toolkit, form);
		createVolumeMountingInfoSection(volume, toolkit, form);
		createVolumeAlertsSection(volume, toolkit, form);

		volumeTab.layout(); // IMP: lays out the form properly
	}

	private void createVolumeLogsTab(Volume volume, TabFolder tabFolder, FormToolkit toolkit) {
		Composite volumeTab = guiHelper.createTab(tabFolder, "Logs", IImageKeys.VOLUME);
		VolumeLogsPage logsPage = new VolumeLogsPage(volumeTab, SWT.NONE, volume);

		volumeTab.layout(); // IMP: lays out the form properly
	}

	private void createVolumeDisksTab(Volume volume, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		Composite volumeDisksTab = guiHelper.createTab(tabFolder, "Disks", IImageKeys.VOLUME);
		BricksPage page = new BricksPage(volumeDisksTab, SWT.NONE, site, GlusterDataModelManager.getInstance().getOnlineBricks(volume));
		volumeDisksTab.layout(); // IMP: lays out the form properly
	}

	private void createVolumeOptionsTab(Volume volume, TabFolder tabFolder, FormToolkit toolkit) {
		Composite volumeTab = guiHelper.createTab(tabFolder, "Options", IImageKeys.VOLUME);
		//VolumeOptionsPage page = new VolumeOptionsPage(volumeTab, SWT.NONE, volume);

		volumeTab.layout(); // IMP: lays out the form properly
	}

	@Override
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site) {
		createVolumePropertiesTab((Volume) entity, tabFolder, toolkit);
		createVolumeDisksTab((Volume) entity, tabFolder, toolkit, site);
		createVolumeOptionsTab((Volume) entity, tabFolder, toolkit);
		createVolumeLogsTab((Volume) entity, tabFolder, toolkit);
	}
}
