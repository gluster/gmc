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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gluster.storage.management.console.utils.GUIHelper;


public class GlusterSupportDialog extends FormDialog {

	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private FormToolkit toolkit;
	private ScrolledForm form;
	private Composite parent;
	
	public GlusterSupportDialog(Shell shell) {
		super(shell);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		this.parent = newShell;
		newShell.setText("Gluster Management Console - Support Information");
	}
	
	protected void createFormContent(IManagedForm mform) {
		form = mform.getForm();
		toolkit = mform.getToolkit();
        form.getBody().setLayout(new GridLayout());
		createSections();
	}
	
	private void createSections() {
		contactGlusterSupportSection();
		commingSoonSection();
		form.layout();
		form.getParent().layout();
	}
	

	private void contactGlusterSupportSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Contact Gluster Support", null, 1, false);
		
		FormText formText = toolkit.createFormText(section, false);
		toolkit.createLabel(section, "Call 1-800-805-5215", SWT.NONE);
		toolkit.createLabel(section, "or", SWT.NONE);
		toolkit.createLabel(section, "Email:support@gluster.com", SWT.NONE);
//		String supportInfo = "<form>" +
//					"Call 1-800-805-5215<br />" +
//					"or<br />" +
//					"Email:support@gluster.com" +
//				"</form>";
//		formText.setText(supportInfo, true, true);
		GridData layoutData = new GridData();
		layoutData.widthHint = 730;
		layoutData.grabExcessHorizontalSpace = true;
		formText.setLayoutData(layoutData);
	}
	
	private void commingSoonSection() {
		Composite section = guiHelper.createSection(form, toolkit, "Coming Soon", null, 7, false);
		FormText formText = toolkit.createFormText(section, true);
		String commingSoonInfo = "<form>" +
					"The following features of GlusterFS will soon be supported in upcoming releases of Gluster Management Console " +
					"<li>Geo-replication</li>" +
					"<li>Directory Quota</li>" +
					"<li>Top and Profile</li>" +
					"<li>POSIX ACLs Support</li><br />" +
					"More information about these features can be found at<br /> " +
					"http://www.gluster.com/community/documentation/index.php/Gluster_3.2:_What_is_New_in_this_Release" +
				"</form>";
		formText.setText(commingSoonInfo, true, true);
		GridData layoutData = new GridData();
		layoutData.widthHint = 700;
		layoutData.grabExcessHorizontalSpace = true;
		formText.setLayoutData(layoutData);
		formText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
		        System.out.println("Link activated: " + e.getHref());
		        try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
					.openURL(new URL((String) e.getHref()));
				} catch (PartInitException e1) {
					e1.printStackTrace();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
		      }
		});
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}
	/**
	 * Overriding to make sure that the dialog is centered in screen
	 */
	@Override
	protected void initializeBounds() {
		super.initializeBounds();
		guiHelper.centerShellInScreen(getShell());
	}
	
}
