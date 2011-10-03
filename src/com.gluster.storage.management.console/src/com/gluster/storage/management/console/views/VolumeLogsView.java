package com.gluster.storage.management.console.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.views.pages.VolumeLogsPage;
import com.gluster.storage.management.core.model.Volume;

public class VolumeLogsView extends ViewPart {
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
		parent.layout(); // IMP: lays out the form properly
	}

	@Override
	public void setFocus() {
		logsPage.setFocus();
	}
}