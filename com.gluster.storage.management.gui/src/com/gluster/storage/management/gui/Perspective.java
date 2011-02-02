package com.gluster.storage.management.gui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.gluster.storage.management.gui.views.details.DetailsView;
import com.gluster.storage.management.gui.views.navigator.NavigationView;

public class Perspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */
	public static final String ID = "com.gluster.storage.management.gui.perspective";

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		//layout.addStandaloneView(ClusterView.ID,  false, IPageLayout.LEFT, 0.30f, layout.getEditorArea());
		//layout.addStandaloneView(DetailsView.ID, false, IPageLayout.RIGHT, 0.70f, layout.getEditorArea());
	}
}
