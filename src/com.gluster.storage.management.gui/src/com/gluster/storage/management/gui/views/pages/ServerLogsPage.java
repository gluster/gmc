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
package com.gluster.storage.management.gui.views.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class ServerLogsPage extends Composite {

	private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	private final GUIHelper guiHelper = GUIHelper.getInstance();
	private Text text;
	private Table table;

	public enum LOG_TABLE_COLUMN_INDICES {
		DATE, TIME, DISK, SEVERITY, MESSAGE
	};

	private static final String[] LOG_TABLE_COLUMN_NAMES = new String[] { "Date", "Time", "Disk", "Severity", "Message" };

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ServerLogsPage(Composite parent, int style, GlusterServer server) {
		super(parent, style);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
			}
		});
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		setLayout(new GridLayout(1, false));
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		setLayoutData(layoutData);

		Composite composite = toolkit.createComposite(this, SWT.NONE);
		toolkit.paintBordersFor(composite);

		Label lblScanLast = toolkit.createLabel(composite, "Scan last", SWT.NONE);
		lblScanLast.setBounds(0, 15, 80, 20);

		text = toolkit.createText(composite, "100", SWT.NONE);
		text.setBounds(85, 15, 60, 20);
		text.setTextLimit(4);
		text.addVerifyListener(new VerifyListener() {

			@Override
			public void verifyText(VerifyEvent event) {
				// Assume we allow it
				event.doit = true;

				String text = event.text;
				char[] chars = text.toCharArray();

				// Don't allow if text contains non-digit characters
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isDigit(chars[i])) {
						event.doit = false;
						break;
					}
				}

			}
		});

		Label lblMessagesAndFilter = toolkit.createLabel(composite, " messages from ", SWT.CENTER);
		lblMessagesAndFilter.setBounds(160, 15, 110, 20);

		Combo combo = new Combo(composite, SWT.CENTER);
		combo.setBounds(295, 15, 100, 20);
		combo.setItems(new String[] { "syslog", "dmesg" });
		toolkit.adapt(combo);
		toolkit.paintBordersFor(combo);
		combo.select(0);

		Button btngo = toolkit.createButton(composite, "&Go", SWT.NONE);
		btngo.setBounds(410, 13, 50, 30);

		Label separator = toolkit.createLabel(composite, "", SWT.SEPARATOR | SWT.HORIZONTAL | SWT.FILL);
		separator.setBounds(0, 50, 500, 2);

		Label lblFilterString = toolkit.createLabel(composite, "Filter String", SWT.LEFT);
		lblFilterString.setBounds(0, 65, 100, 20);

		text = guiHelper.createFilterText(toolkit, composite);
		text.setBounds(105, 65, 250, 20);

		Composite logContentsComposite = createLogContentsComposite(toolkit);
		// Text logContentsText = toolkit.createText(logContentsComposite, "", SWT.MULTI | SWT.FLAT | SWT.BORDER);
		// logContentsText.setEditable(false);
		// populateDummyLogContent(logContentsText);

		ListViewer logViewer = new ListViewer(logContentsComposite, SWT.BORDER | SWT.V_SCROLL | SWT.NO);
		logViewer.setContentProvider(new ArrayContentProvider());
		guiHelper.createFilter(logViewer, text, false);
		logViewer.setInput(getDummyLogContents());

		// TODO: Link the filter string with the contents text
	}

	private Composite createLogContentsComposite(FormToolkit toolkit) {
		Composite tableViewerComposite = toolkit.createComposite(this, SWT.NONE);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.verticalIndent = 10;
		tableViewerComposite.setLayoutData(layoutData);
		return tableViewerComposite;
	}

	private String[] getDummyLogContents() {

		String[] logMessages = {
				"Jan 19 13:43:08 shireesh-laptop dhclient: last message repeated 6 times",
				"Jan 19 13:44:08 shireesh-laptop dhclient: last message repeated 5 times",
				"Jan 19 13:44:47 shireesh-laptop dhclient: last message repeated 2 times",
				"Jan 19 13:44:47 shireesh-laptop dhclient: DHCPREQUEST of 192.168.1.174 on eth1 to 255.255.255.255 port 67",
				"Jan 19 13:45:49 shireesh-laptop dhclient: last message repeated 6 times",
				"Jan 19 13:46:59 shireesh-laptop dhclient: last message repeated 6 times",
				"Jan 19 13:48:01 shireesh-laptop dhclient: last message repeated 4 times",
				"Jan 19 13:49:02 shireesh-laptop dhclient: last message repeated 5 times",
				"Jan 19 13:50:08 shireesh-laptop dhclient: last message repeated 4 times",
				"Jan 19 13:51:08 shireesh-laptop dhclient: last message repeated 6 times",
				"Jan 19 13:52:08 shireesh-laptop dhclient: last message repeated 4 times",
				"Jan 19 13:53:08 shireesh-laptop dhclient: last message repeated 6 times",
				"Jan 19 13:54:08 shireesh-laptop dhclient: last message repeated 5 times",
				"Jan 19 13:55:08 shireesh-laptop dhclient: last message repeated 4 times",
				"Jan 19 13:56:08 shireesh-laptop dhclient: last message repeated 4 times",
				"Jan 19 13:57:08 shireesh-laptop dhclient: last message repeated 3 times",
				"Jan 19 13:58:08 shireesh-laptop dhclient: last message repeated 6 times",
				"Jan 19 13:59:08 shireesh-laptop dhclient: last message repeated 4 times",
				"Jan 19 13:59:40 shireesh-laptop dhclient: last message repeated 3 times",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>  DHCP: device eth1 state changed bound -> expire",
				"Jan 19 13:59:40 shireesh-laptop dhclient: DHCPDISCOVER on eth1 to 255.255.255.255 port 67 interval 8",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>  DHCP: device eth1 state changed expire -> preinit",
				"Jan 19 13:59:40 shireesh-laptop dhclient: DHCPOFFER of 192.168.1.174 from 192.168.1.1",
				"Jan 19 13:59:40 shireesh-laptop dhclient: DHCPREQUEST of 192.168.1.174 on eth1 to 255.255.255.255 port 67",
				"Jan 19 13:59:40 shireesh-laptop dhclient: DHCPACK of 192.168.1.174 from 192.168.1.1",
				"Jan 19 13:59:40 shireesh-laptop dhclient: bound to 192.168.1.174 -- renewal in 3205 seconds.",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>  DHCP: device eth1 state changed preinit -> bound",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>    address 192.168.1.174",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>    prefix 24 (255.255.255.0)",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>    gateway 192.168.1.1",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>    nameserver '192.168.1.1'",
				"Jan 19 13:59:40 shireesh-laptop NetworkManager: <info>    domain name 'in.gluster.com'",
				"Jan 19 14:03:53 shireesh-laptop avahi-daemon[1098]: Invalid legacy unicast query packet.",
				"Jan 19 14:03:53 shireesh-laptop avahi-daemon[1098]: Received response from host 192.168.1.155 with invalid source port 37219 on interface 'eth0.0'",
				"Jan 19 14:03:54 shireesh-laptop avahi-daemon[1098]: Invalid legacy unicast query packet.",
				"Jan 19 14:03:54 shireesh-laptop avahi-daemon[1098]: Invalid legacy unicast query packet.",
				"Jan 19 14:03:54 shireesh-laptop avahi-daemon[1098]: Received response from host 192.168.1.155 with invalid source port 37219 on interface 'eth0.0'",
				"Jan 19 14:05:09 shireesh-laptop avahi-daemon[1098]: last message repeated 8 times",
				"Jan 19 14:12:48 shireesh-laptop NetworkManager: <debug> [1295426568.002642] periodic_update(): Roamed from BSSID E0:CB:4E:C0:0B:7F (glfs) to (none) ((none))",
				"Jan 19 14:12:54 shireesh-laptop NetworkManager: <debug> [1295426574.002448] periodic_update(): Roamed from BSSID (none) ((none)) to E0:CB:4E:C0:0B:7F (glfs)",
				"Jan 19 14:17:01 shireesh-laptop CRON[5321]: (root) CMD (   cd / && run-parts --report /etc/cron.hourly)" };
		
		return logMessages;
	}
}
