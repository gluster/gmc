package com.gluster.storage.management.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.details.DisksPage;

public class VolumeDisksView extends ViewPart {
	public static final String ID = VolumeDisksView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private DisksPage page;
	private Volume volume;

	@Override
	public void createPartControl(Composite parent) {
		if (volume == null) {
			volume = (Volume) guiHelper.getSelectedEntity(getSite(), Volume.class);
		}

		createPage(parent);
	}

	/**
	 * @param parent
	 */
	private void createPage(Composite parent) {
		page = new DisksPage(parent, SWT.NONE, getSite(), volume.getDisks());
		parent.layout(); // IMP: lays out the form properly
	}

	@Override
	public void setFocus() {
		page.setFocus();
	}
}

