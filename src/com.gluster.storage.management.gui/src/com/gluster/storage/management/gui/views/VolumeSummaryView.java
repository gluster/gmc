package com.gluster.storage.management.gui.views;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.model.Alert;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Partition;
import com.gluster.storage.management.core.model.Server.SERVER_STATUS;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.utils.NumberUtil;
import com.gluster.storage.management.core.utils.StringUtil;
import com.gluster.storage.management.core.utils.ValidationUtil;
import com.gluster.storage.management.gui.GlusterDataModelManager;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.toolbar.GlusterToolbarManager;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class VolumeSummaryView extends ViewPart {
	public static final String ID = VolumeSummaryView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private ScrolledForm form;
	private Volume volume;
	private CLabel lblStatusValue;
	private DefaultClusterListener volumeChangedListener;
	private Hyperlink changeLink;
	private Hyperlink CifsChangeLink;
	private Text accessControlText;
	private Text cifsUsersText;
	private ControlDecoration errDecoration;
	private ControlDecoration errCifsDecoration;
	private Composite parent;
	private static final String COURIER_FONT = "Courier";
	private Cluster cluster = GlusterDataModelManager.getInstance().getModel().getCluster();
	private Button nfsCheckBox;
	private FormText glusterNfsMountText;
	private String nfsMountInfo;
	private Label nfsLabel;
	private String nfs;
	
	private Label numberOfBricks;
	private Label totalDiskSpace;
	private Composite alertsSection;
	private Button cifsCheckbox;
	private Label cifsLabel;
	private Composite cifsUpdateLinkComposite;

	@Override
	public void createPartControl(Composite parent) {
		if (volume == null) {
			volume = (Volume) guiHelper.getSelectedEntity(getSite(), Volume.class);
		}

		this.parent = parent;
		setPartName("Summary");
		createSections();

		final GlusterToolbarManager toolbarManager = new GlusterToolbarManager(getSite().getWorkbenchWindow());
		// Refresh the navigation tree whenever there is a change to the data model
		volumeChangedListener = new DefaultClusterListener() {
			@Override
			public void volumeChanged(Volume volume, Event event) {
				updateVolumeStatusLabel();
				populateAccessControlText();
				changeNFSStatus(volume.isNfsEnabled());
				updateBrickChanges(volume);
				toolbarManager.updateToolbar(volume);
			}

			@Override
			public void alertsGenerated() {
				super.alertsGenerated();
				guiHelper.clearSection(alertsSection);
				populateAlertSection();
				alertsSection.layout();
			}
		};
		GlusterDataModelManager.getInstance().addClusterListener(volumeChangedListener);
	}

	@Override
	public void dispose() {
		super.dispose();
		GlusterDataModelManager.getInstance().removeClusterListener(volumeChangedListener);
	}

	private void createSections() {
		form = guiHelper.setupForm(parent, toolkit, "Volume Properties [" + volume.getName() + "]");

		createVolumePropertiesSection();
		createVolumeMountingInfoSection();
		createVolumeAlertsSection();

		parent.layout(); // IMP: lays out the form properly
	}

	private void createVolumeAlertsSection() {
		alertsSection = guiHelper.createSection(form, toolkit, "Alerts", null, 1, false);
		populateAlertSection();
	}
	
	private void populateAlertSection() {
		List<Alert> alerts = GlusterDataModelManager.getInstance().getModel().getCluster().getAlerts();

		for (int i = 0; i < alerts.size(); i++) {
			if (alerts.get(i).getType() == Alert.ALERT_TYPES.OFFLINE_VOLUME_BRICKS_ALERT
					&& alerts.get(i).getReference().split(":")[0].trim().equals(volume.getName())) {
				addAlertLabel(alertsSection, alerts.get(i));
			}
		}
	}

	private void addAlertLabel(Composite section, Alert alert) {
		CLabel lblAlert = new CLabel(section, SWT.NONE);
		lblAlert.setImage(guiHelper.getImage(IImageKeys.BRICK_OFFLINE_22x22));
		lblAlert.setText(alert.getMessage());
		lblAlert.redraw();
	}

	private FormText setFormTextStyle(FormText formText, String fontName, int size, int style) {
		Font font = new Font(Display.getCurrent(), new FontData(fontName, size, style));
		formText.setFont(font);
		return formText;
	}

	private void createVolumeMountingInfoSection() {
		String glusterFs = "Gluster:";
		nfs = "NFS:";
		String onlineServers = getOnlineServers(10); // Limited to 10 servers
		String firstOnlineServer = onlineServers.split(",")[0].trim();
		String glusterFsMountInfo = "mount -t glusterfs " + firstOnlineServer + ":/" + volume.getName()
				+ " <mount-point>";
		nfsMountInfo = "mount -t nfs " + firstOnlineServer + ":/" + volume.getName() + " <mount-point>";
		// TODO: if more than 10 servers...
		String info = "Server can be any server name in the storage cloud eg. <" + onlineServers + ">"; 

		Composite section = guiHelper.createSection(form, toolkit, "Mounting Information", null, 3, false);

		toolkit.createLabel(section, glusterFs, SWT.NORMAL);
		FormText glusterfsMountText = setFormTextStyle(toolkit.createFormText(section, true), COURIER_FONT, 10,
				SWT.NONE);
		glusterfsMountText.setText(glusterFsMountInfo, false, false);
		glusterfsMountText.setLayoutData(new GridData(GridData.BEGINNING, GridData.VERTICAL_ALIGN_CENTER, false, false,
				2, 0)); // Label spanned two column

		nfsLabel = toolkit.createLabel(section, nfs, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		nfsLabel.setLayoutData(data);

		glusterNfsMountText = setFormTextStyle(toolkit.createFormText(section, true), COURIER_FONT, 10, SWT.NONE);
		glusterNfsMountText.setText(nfsMountInfo, false, false);
		glusterNfsMountText.setLayoutData(new GridData(GridData.BEGINNING, GridData.VERTICAL_ALIGN_CENTER, false,
				false, 2, 0));

		changeNFSStatus( nfsCheckBox.getSelection());
		
		toolkit.createLabel(section, "");
		Label infoLabel = toolkit.createLabel(section, info, SWT.NONE);
		infoLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.VERTICAL_ALIGN_CENTER, false, false, 2, 0));

		// TODO: implement a logic to identify the corresponding glusterfs client download link
		String message = "You can download gluster FS client from";
		String glusterClientDownloadlinkText = "here.";
		final String glusterClientDownloadlink = "http://www.gluster.com";

		toolkit.createLabel(section, "");
		toolkit.createLabel(section, message);
		Hyperlink link = toolkit.createHyperlink(section, glusterClientDownloadlinkText, SWT.NORMAL);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				try {
					System.out.println(e.getLabel() + " [" + e.getHref() + "]");
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
							.openURL(new URL(glusterClientDownloadlink));
				} catch (PartInitException e1) {
					e1.printStackTrace();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private String getOnlineServers(int maxServers) {
		List<String> OnlineServers = new ArrayList<String>();
		for (GlusterServer server : cluster.getServers()) {
			if (server.getStatus() == SERVER_STATUS.ONLINE) {
				OnlineServers.add(server.getName());
				if (OnlineServers.size() >= maxServers) {
					break;
				}
			}
		}
		return StringUtil.collectionToString(OnlineServers, ", ") + ((OnlineServers.size() > maxServers) ? "..." : "");
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

		createNumOfBricksField(section);
		createDiskSpaceField(section);
		// createTransportTypeField(section);
		createNASProtocolField(section);
		createCifsField(section);
		createAccessControlField(section);
		createStatusField(section);
	}

	private GridData createDefaultLayoutData() {
		GridData layoutData = new GridData();
		layoutData.minimumWidth = 300;
		layoutData.widthHint = 300;
		return layoutData;
	}
	
	private void createCifsField(Composite section) {
		GridData data = new GridData();
		data.heightHint = 0;
		
		cifsLabel = toolkit.createLabel(section, "CIFS: ", SWT.NONE);
		cifsLabel.setLayoutData(data);
		cifsLabel.setVisible(false);
		
		cifsUsersText = toolkit.createText(section, volume.getAccessControlList(), SWT.BORDER);
		populateCifsUsersText();
		addKeyListenerForCifsUser();
		cifsUsersText.setLayoutData(createDefaultLayoutData());
		cifsUsersText.setEnabled(true);
		cifsUsersText.setLayoutData(data);
		cifsUsersText.setVisible(false);
		
		cifsUpdateLinkComposite = toolkit.createComposite(section, SWT.NONE);
		cifsUpdateLinkComposite.setVisible(false);
		cifsUpdateLinkComposite.setLayoutData(data);
		cifsUpdateLinkComposite.setLayout(new FillLayout());
		
		createChangeLinkForCifs(cifsUpdateLinkComposite);
		
		// error decoration used while validating the cifs users text
		errCifsDecoration = guiHelper.createErrorDecoration(cifsUsersText);
		errCifsDecoration.hide();
	}

	private void createAccessControlField(Composite section) {
		toolkit.createLabel(section, "Access Control: ", SWT.NONE);
		accessControlText = toolkit.createText(section, volume.getAccessControlList(), SWT.BORDER);

		populateAccessControlText();
		addKeyListenerForAccessControl();
		accessControlText.setLayoutData(createDefaultLayoutData());
		accessControlText.setEnabled(false);
		createChangeLinkForAccessControl(section);

		// error decoration used while validating the access control text
		errDecoration = guiHelper.createErrorDecoration(accessControlText);
		errDecoration.hide();
		createAccessControlInfoLabel(section); // info text
	}

	private void createAccessControlInfoLabel(Composite section) {
		toolkit.createLabel(section, "", SWT.NONE);
		Label accessControlInfoLabel = toolkit.createLabel(section, "(Comma separated list of IP addresses/hostnames)");
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		data.horizontalSpan = 2;
		accessControlInfoLabel.setLayoutData(data);
	}

	private void createChangeLinkForAccessControl(Composite section) {
		changeLink = toolkit.createHyperlink(section, "change", SWT.NONE);
		changeLink.addHyperlinkListener(new HyperlinkAdapter() {

			private void finishEdit() {
				saveAccessControlList();
			}

			private void startEdit() {
				accessControlText.setEnabled(true);
				accessControlText.setFocus();
				accessControlText.selectAll();
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

	private void saveAccessControlList() {
		final String newACL = accessControlText.getText();

		guiHelper.setStatusMessage("Setting access control list to [" + newACL + "]...");
		parent.update();

		if (newACL.equals(volume.getAccessControlList())) {
			accessControlText.setEnabled(false);
			changeLink.setText("change");
		} else if (ValidationUtil.isValidAccessControl(newACL)) {
			BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
				@Override
				public void run() {
					try {
						new VolumesClient().setVolumeOption(volume.getName(), Volume.OPTION_AUTH_ALLOW, newACL);
						accessControlText.setEnabled(false);
						changeLink.setText("change");

						GlusterDataModelManager.getInstance().setAccessControlList(volume, newACL);
					} catch (Exception e) {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Access control", e.getMessage());
					}
				}
			});
		} else {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Access control", "Invalid IP / Host name ");
		}
		guiHelper.clearStatusMessage();
		parent.update();
	}
	
	private void createChangeLinkForCifs(Composite section) {
		CifsChangeLink = toolkit.createHyperlink(section, "Update", SWT.NONE);
		CifsChangeLink.addHyperlinkListener(new HyperlinkAdapter() {

			private void finishEdit() {
				saveCifsConfiguration();
			}

			private void startEdit() {
				cifsUsersText.setEnabled(true);
				cifsUsersText.setFocus();
				cifsUsersText.selectAll();
				CifsChangeLink.setText("update");
			}

			@Override
			public void linkActivated(HyperlinkEvent e) {
				if (cifsUsersText.isEnabled()) {
					// we were already in edit mode.
					finishEdit();
				} else {
					// Get in to edit mode
					startEdit();
				}
			}
		});
	}
	
	private void saveCifsConfiguration() {
		final String cifsUsers = cifsUsersText.getText();

		guiHelper.setStatusMessage("Setting Cifs Configuration...");
		parent.update();
		
		List<String> userList = volume.getCifsUsers();
		String configuredUsers = "";
		if (userList != null) {			
			configuredUsers = StringUtil.collectionToString(userList, ",");
		}

		if (cifsUsersText.equals(configuredUsers)) {
			cifsUsersText.setEnabled(false);
			CifsChangeLink.setText("change");
			// There is no change in the users list
		} else  if(isvalidCifsUser()) {
			BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
				@Override
				public void run() {
					try {
						new VolumesClient().setCifsConfig(volume.getName(), cifsCheckbox.getSelection(), cifsUsers);
						cifsUsersText.setEnabled(false);
						CifsChangeLink.setText("change");

						GlusterDataModelManager.getInstance().setCifsConfig(volume, cifsCheckbox.getSelection(),
								Arrays.asList(cifsUsers.split(",")));
					} catch (Exception e) {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Cifs Configuration", e.getMessage());
						populateCifsUsersText();
					}
				}
			});
		} else {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Cifs Configuration", "Please enter cifs user name ");
			cifsUsersText.setFocus();
		}
		guiHelper.clearStatusMessage();
		parent.update();
	}
	
	private void saveNFSOption() {
		guiHelper.setStatusMessage("Setting NFS option...");
		parent.update();
		
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			@Override
			public void run() {
				try {
					boolean enableNfs = nfsCheckBox.getSelection();
					new VolumesClient().setVolumeOption(volume.getName(), Volume.OPTION_NFS_DISABLE,
							(enableNfs) ? GlusterConstants.OFF : GlusterConstants.ON);
					GlusterDataModelManager.getInstance().setNfsEnabled(volume, enableNfs);
				} catch (Exception e) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "NFS Option", e.getMessage());
				}
			}
		});
		guiHelper.clearStatusMessage();
		parent.update();
	}

	private void addKeyListenerForAccessControl() {
		accessControlText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent key) {
				switch (key.keyCode) {
				case SWT.ESC:
					// Reset to default
					populateAccessControlText();
					changeLink.setText("change");
					accessControlText.setEnabled(false);
					break;
				case 13:
					// User has pressed enter. Save the new value
					saveAccessControlList();
					break;
				}

				validateAccessControlList();
			}
		});
	}

	private void populateAccessControlText() {
		String accessControlList = volume.getAccessControlList();
		if (accessControlList == null) {
			// if not set, show default value
			accessControlList = GlusterDataModelManager.getInstance().getVolumeOptionDefaultValue(
					Volume.OPTION_AUTH_ALLOW);
		}
		accessControlText.setText(accessControlList);
	}
	
	private void addKeyListenerForCifsUser() {
		cifsUsersText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent key) {
				switch (key.keyCode) {
				case SWT.ESC:
					// Reset to default
					populateCifsUsersText();
					break;
				case 13:
					// User has pressed enter. Save the new value
					saveCifsConfiguration();
					break;
				}
				validateCifsUsers();
			}
		});
	}
	
	private void populateCifsUsersText() {
		List<String> userList = volume.getCifsUsers();
		if (userList == null) {
			cifsUsersText.setText("");			
		} else {
			String users = StringUtil.collectionToString(userList, ",");
			cifsUsersText.setText(users);
		}
	}

	private void createNASProtocolField(final Composite section) {
		toolkit.createLabel(section, "Access Protocols: ", SWT.NONE);

		Composite nasProtocolsComposite = toolkit.createComposite(section);
		nasProtocolsComposite.setLayout(new FillLayout());

		createCheckbox(nasProtocolsComposite, "Gluster", true, false);
		
		nfsCheckBox = createCheckbox(nasProtocolsComposite, "NFS", volume.isNfsEnabled(), true);
		
		nfsCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveNFSOption();
			}
		});
		
		// CIFS checkbox
		cifsCheckbox = createCheckbox(nasProtocolsComposite, "CIFS", volume.isCifsEnable(), true);
		cifsCheckboxListner(cifsCheckbox);
		
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}
	
	private void cifsCheckboxListner(final Button cifsCheckbox) {
		cifsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (cifsCheckbox.getSelection()) {
					GridData data = new GridData();
					data.heightHint = 20;
					data.widthHint = 100;

					cifsLabel.setVisible(true);
					cifsLabel.setLayoutData(data);
					
					GridData data1 = new GridData();
					data1.heightHint = 20;
					data1.widthHint = 300;
					cifsUsersText.setVisible(true);
					cifsUsersText.setLayoutData(data1);
					
					GridData data2 = new GridData();
					data2.heightHint = 25;
					data2.widthHint = 75;
					cifsUpdateLinkComposite.setVisible(true);
					cifsUpdateLinkComposite.setLayoutData(data2);
					form.reflow(true);
				} else {
					GridData data = new GridData();
					data.heightHint = 0;

					cifsUsersText.setVisible(false);
					cifsUsersText.setLayoutData(data);
					
					cifsLabel.setVisible(false);
					cifsLabel.setLayoutData(data);
					
					cifsUpdateLinkComposite.setVisible(false);
					cifsUpdateLinkComposite.setLayoutData(data);
					
					form.reflow(true);
				}
			}
		});
	}

	private Button createCheckbox(Composite parent, String label, boolean checked, boolean enabled) {
		final Button checkBox = toolkit.createButton(parent, label, SWT.CHECK);
		checkBox.setSelection(checked);
		checkBox.setEnabled(enabled);
		return checkBox;
	}
	
	private void changeNFSStatus(Boolean isNFSExported) {
		glusterNfsMountText.setVisible(isNFSExported);
		nfsLabel.setVisible(isNFSExported);
		nfsCheckBox.setSelection(isNFSExported);
	}
	
	
	private void updateBrickChanges(Volume volume) {
		numberOfBricks.setText("" + volume.getNumOfBricks());
		totalDiskSpace.setText("" + NumberUtil.formatNumber((getTotalDiskSpace() / 1024)));
	}

	private double getDiskSize(String serverName, String deviceName) {
		double diskSize = 0;
		GlusterServer server = cluster.getServer(serverName);
		if (server.getStatus() == SERVER_STATUS.ONLINE) {
			for (Disk disk : server.getDisks()) {
				if (disk.getName().equals(deviceName)) {
					diskSize = disk.getSpace();
					break;
				}

				if (disk.hasPartitions()) {
					for (Partition partition : disk.getPartitions()) {
						if (partition.getName().equals(deviceName)) {
							diskSize = partition.getSpace();
							break;
						}
					}
				}
			}
		}
		return diskSize;
	}

	private double getTotalDiskSpace() {
		double diskSize = 0;
		for (Brick brick : volume.getBricks()) {
			diskSize += getDiskSize(brick.getServerName(),
					GlusterDataModelManager.getInstance().getDeviceForBrickDir(brick).getName());
		}
		return diskSize;
	}

	private void createDiskSpaceField(Composite section) {
		Label diskSpaceLabel = toolkit.createLabel(section, "Total Disk Space (GB): ", SWT.NONE);
		diskSpaceLabel.setToolTipText("<b>bold</b>normal");
		totalDiskSpace = toolkit.createLabel(section, "" + NumberUtil.formatNumber((getTotalDiskSpace() / 1024)), SWT.NONE);
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
		lblStatusValue.setImage((volume.getStatus() == Volume.VOLUME_STATUS.ONLINE) ? guiHelper
				.getImage(IImageKeys.STATUS_ONLINE_16x16) : guiHelper.getImage(IImageKeys.STATUS_OFFLINE_16x16));
		GridData data = new GridData();
		data.horizontalAlignment = SWT.FILL;
		lblStatusValue.setLayoutData(data);
		lblStatusValue.redraw();
	}

	private void createTransportTypeField(Composite section) {
		toolkit.createLabel(section, "Transport Type: ", SWT.NONE);
		toolkit.createLabel(section, "" + volume.getTransportTypeStr(), SWT.NONE);
		toolkit.createLabel(section, "", SWT.NONE); // dummy
	}

	private void createNumOfBricksField(Composite section) {
		toolkit.createLabel(section, "Number of Bricks: ", SWT.NONE);
		numberOfBricks = toolkit.createLabel(section, "" + volume.getNumOfBricks(), SWT.NONE);
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

	private void validateAccessControlList() {
		errDecoration.hide();

		if (accessControlText.getText().length() == 0) {
			errDecoration.setDescriptionText("Access control list cannot be empty!");
			errDecoration.show();
			return;
		}

		if (!ValidationUtil.isValidAccessControl(accessControlText.getText())) {
			errDecoration
					.setDescriptionText("Access control list must be a comma separated list of IP addresses/Host names. Please enter a valid value!");
			errDecoration.show();
		}
	}
	
	private void validateCifsUsers() {
		if (cifsCheckbox.getSelection()) {
			String cifsUserList = cifsUsersText.getText().trim();
			if (cifsUserList.length() == 0) {
				errCifsDecoration.setDescriptionText("Please enter cifs user name");
				errCifsDecoration.show();
			} else {
				errCifsDecoration.hide();
			}
		}
	}
	
	private boolean isvalidCifsUser() {
		if (cifsCheckbox.getSelection()) {
			String cifsUserList = cifsUsersText.getText().trim();
			if (cifsUserList.length() == 0) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}
}
