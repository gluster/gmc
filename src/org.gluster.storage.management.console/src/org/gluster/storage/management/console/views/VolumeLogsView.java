/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.VolumeLogsPage;
import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.VolumeLogMessage;
import org.gluster.storage.management.core.utils.DateUtil;


public class VolumeLogsView extends ViewPart implements IDoubleClickListener {
	VolumeLogsPage logsPage;
	public static final String ID = VolumeLogsView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private Volume volume;

	@Override
	public void createPartControl(Composite parent) {
		if (volume == null) {
			volume = guiHelper.getSelectedEntity(getSite(), Volume.class);
		}

		createPage(parent);
	}

	private void createPage(Composite parent) {
		logsPage = new VolumeLogsPage(parent, SWT.NONE, volume);
		logsPage.addDoubleClickListener(this);
		
		parent.layout(); // IMP: lays out the form properly
	}

	@Override
	public void setFocus() {
		logsPage.setFocus();
	}
	
	@Override
	public void doubleClick(DoubleClickEvent event) {
		VolumeLogMessage volumeLogMessage = (VolumeLogMessage) ((StructuredSelection) event.getSelection())
				.getFirstElement();
		String message = DateUtil.formatDate(volumeLogMessage.getTimestamp()) + " "
				+ DateUtil.formatTime(volumeLogMessage.getTimestamp()) + " [" + volumeLogMessage.getSeverity() + "]"
				+ CoreConstants.NEWLINE + CoreConstants.NEWLINE + volumeLogMessage.getMessage();

		new MessageDialog(getSite().getShell(), "Log message from " + volumeLogMessage.getBrick(), null, message,
				MessageDialog.NONE, new String[] { "Close" }, 0).open();

	}
}