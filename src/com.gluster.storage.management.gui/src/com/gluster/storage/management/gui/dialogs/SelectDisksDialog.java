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

import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.Device;

public class SelectDisksDialog extends Dialog {

	private BricksSelectionPage disksPage;
	private List<Device> allDevices;
	private List<Device> selectedDevices;
	private String volumeName;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public SelectDisksDialog(Shell parentShell, List<Device> allDevices, List<Device> selectedDevices, String volumeName) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.allDevices = allDevices;
		this.selectedDevices = selectedDevices;
		this.volumeName = volumeName;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout containerLayout = new GridLayout(2, false);
		container.setLayout(containerLayout);
		GridData containerLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(containerLayoutData);

		getShell().setText("Create Volume - Select Bricks");

		disksPage = new BricksSelectionPage(container, SWT.NONE, allDevices, selectedDevices, volumeName);
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1024, 600);
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		if (this.getSelectedDevices().size() == 0) {
			MessageDialog.openError(getShell(), "Select Brick(s)", "Please select atlease one brick");
		} else {
			super.okPressed();
		}
	}

	public List<Device> getSelectedDevices() {
		return disksPage.getChosenDevices();
	}

	public Set<Brick> getSelectedBricks(String volumeName) {
		return disksPage.getChosenBricks(volumeName);
	}
}
