package com.gluster.storage.management.console.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.views.pages.VolumeLogsPage;
import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.VolumeLogMessage;
import com.gluster.storage.management.core.utils.DateUtil;

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