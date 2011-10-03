package com.gluster.storage.management.console.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.console.views.pages.VolumeOptionsPage;
import com.gluster.storage.management.core.model.Volume;

public class VolumeOptionsView extends ViewPart {
	public static final String ID = VolumeOptionsView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private VolumeOptionsPage page;
	private Volume volume;

	@Override
	public void createPartControl(Composite parent) {
		if (volume == null) {
			volume = guiHelper.getSelectedEntity(getSite(), Volume.class);
		}

		createPage(parent);
	}

	private void createPage(Composite parent) {
		page = new VolumeOptionsPage(parent, SWT.NONE, volume);
		parent.layout(); // IMP: lays out the form properly
	}

	@Override
	public void setFocus() {
		page.setFocus();
	}
}

