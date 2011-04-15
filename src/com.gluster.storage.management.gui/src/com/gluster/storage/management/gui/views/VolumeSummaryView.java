package com.gluster.storage.management.gui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Event.EVENT_TYPE;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.NAS_PROTOCOL;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.core.utils.ValidationUtil;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.toolbar.GlusterToolbarManager;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeSummaryView extends ViewPart {
	public static final String ID = VolumeSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private static final String VOLUME_OPTION_AUTH_ALLOW = "auth.allow";

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private Volume volume;
	private CLabel lblStatusValue;
	private DefaultClusterListener volumeChangedListener;

	@Override
	public void createPartControl(Composite parent) {
		if (volume == null) {
			volume = (Volume) guiHelper.getSelectedEntity(getSite(), Volume.class);
		}

		createSections(parent);

		// Refresh the navigation tree whenever there is a change to the data model
		volumeChangedListener = new DefaultClusterListener() {
			@Override
			public void volumeChanged(Volume volume, Event event) {
				if (event.getEventType() == EVENT_TYPE.VOLUME_STATUS_CHANGED) {
					updateVolumeStatusLabel();
					new GlusterToolbarManager(getSite().getWorkbenchWindow()).updateToolbar(volume);
				}
			}
		};
		GlusterDataModelManager.getInstance().addClusterListener(volumeChangedListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(volumeChangedListener);
	}

	private void createSections(Composite parent) {
		form = guiHelper.setupForm(parent, toolkit, "Volume Properties [" + volume.getName() + "]");

		createVolumePropertiesSection();
		createVolumeMountingInfoSection();
		createVolumeAlertsSection();

		parent.layout(); // IMP: lays out the form properly
	}

	private void createVolumeAlertsSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Alerts", null, 1, false);
		List<Alert> alerts = GlusterDataModelManager.getInstance().getModel().getCluster().getAlerts();

		for (int i = 0; i < alerts.size(); i++) {
			if (alerts.get(i).getReference().split(":")[0].trim().equals(volume.getName())
					&& alerts.get(i).getType() == Alert.ALERT_TYPES.OFFLINE_VOLUME_DISKS_ALERT) {
				addAlertLabel(section, alerts.get(i));
			}
		}
	}

	private void addAlertLabel(Composite section, Alert alert) {
		CLabel lblAlert = new CLabel(section, SWT.NONE);
		lblAlert.setImage(guiHelper.getImage(IImageKeys.DISK_OFFLINE));
		lblAlert.setText(alert.getMessage());
		lblAlert.redraw();
	}

	private void createVolumeMountingInfoSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Mounting Information", null, 3, false);
		toolkit.createLabel(section, "Information about mounting the\nvolume will be printed here");
	}

	/**
	 * 
	 */
	private void createVolumePropertiesSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Properties", null, 3, false);

		createVolumeTypeField(section);

		VOLUME_TYPE volumeType = volume.getVolumeType();
		if (volumeType == VOLUME_TYPE.DISTRIBUTED_MIRROR) {
			createReplicaCountField(section);
		}

		if (volumeType == VOLUME_TYPE.DISTRIBUTED_STRIPE) {
			createStripeCountField(section);
		}

		createNumOfDisksField(section);
		createDiskSpaceField(section);
		createTransportTypeField(section);
		createNASProtocolField(section);
		createAccessControlField(section);
		createStatusField(section);
	}

	private GridData createDefaultLayoutData() {
		GridData layoutData = new GridData();
		layoutData.minimumWidth = 150;
		layoutData.widthHint = 150;
		return layoutData;
	}

	private void createAccessControlField(Composite section) {
		toolkit.createLabel(section, "Access Control: ", SWT.NONE);
		Text accessControlText = toolkit.createText(section, volume.getAccessControlList());
		accessControlText.setLayoutData(createDefaultLayoutData());
		accessControlText.setEnabled(false);
		createChangeLinkForAccessControl(section, accessControlText);
	}

	private void createChangeLinkForAccessControl(Composite section, final Text accessControlText) {
		final Hyperlink changeLink = toolkit.createHyperlink(section, "change", SWT.NONE);
		changeLink.addHyperlinkListener(new HyperlinkAdapter() {

			@SuppressWarnings("static-access")
			private void finishEdit() {

				if (new ValidationUtil().isValidAccessControl(accessControlText.getText())) {
					Status status = (new VolumesClient(GlusterDataModelManager.getInstance().getSecurityToken()))
							.setVolumeOption(volume.getName(), VOLUME_OPTION_AUTH_ALLOW, accessControlText.getText());
					if (status.isSuccess()) {
						volume.setAccessControlList(accessControlText.getText());
						accessControlText.setEnabled(false);
						changeLink.setText("change");
						MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Access control",
								status.getMessage());
					} else {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Access control",
								status.getMessage());
					}

				} else {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Access control",
							"Invalid IP / Host name ");
				}

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

	private void createNASProtocolField(Composite section) {
		toolkit.createLabel(section, "NAS Protocols: ", SWT.NONE);

		Composite nasProtocolsComposite = toolkit.createComposite(section);
		nasProtocolsComposite.setLayout(new FillLayout());

		createCheckbox(nasProtocolsComposite, "Gluster", true);
		final Button nfsCheckBox = createCheckbox(nasProtocolsComposite, "NFS",
				volume.getNASProtocols().contains(NAS_PROTOCOL.NFS));

		toolkit.createLabel(section, "", SWT.NONE); // dummy
		// createChangeLinkForNASProtocol(section, nfsCheckBox);
	}

	private Button createCheckbox(Composite parent, String label, boolean selected) {
		final Button checkBox = toolkit.createButton(parent, label, SWT.CHECK);
		checkBox.setEnabled(false);
		checkBox.setSelection(selected);
		return checkBox;
	}

	private void createChangeLinkForNASProtocol(Composite section, final Button nfsCheckBox) {
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

	private void createDiskSpaceField(Composite section) {
		Label diskSpaceLabel = toolkit.createLabel(section, "Total Disk Space (GB): ", SWT.NONE);
		diskSpaceLabel.setToolTipText("<b>bold</b>normal");
		toolkit.createLabel(section, "" + NumberUtil.formatNumber(volume.getTotalDiskSpace()), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createStatusField(Composite section) {
		toolkit.createLabel(section, "Status: ", SWT.NONE);

		lblStatusValue = new CLabel(section, SWT.NONE);
		updateVolumeStatusLabel();

		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void updateVolumeStatusLabel() {
		lblStatusValue.setText(volume.getStatusStr());
		lblStatusValue.setImage(volume.getStatus() == Volume.VOLUME_STATUS.ONLINE ? guiHelper
				.getImage(IImageKeys.STATUS_ONLINE) : guiHelper.getImage(IImageKeys.STATUS_OFFLINE));
		lblStatusValue.redraw();
	}

	private void createTransportTypeField(Composite section) {
		toolkit.createLabel(section, "Transport Type: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getTransportTypeStr(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createNumOfDisksField(Composite section) {
		toolkit.createLabel(section, "Number of Disks: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getNumOfDisks(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createStripeCountField(Composite section) {
		toolkit.createLabel(section, "Stripe Count: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getStripeCount(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createReplicaCountField(Composite section) {
		toolkit.createLabel(section, "Replica Count: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getReplicaCount(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createVolumeTypeField(Composite section) {
		toolkit.createLabel(section, "Volume Type: ", SWT.NONE);
		toolkit.createLabel(section, volume.getVolumeTypeStr(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE);
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}
}
