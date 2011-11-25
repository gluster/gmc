package org.gluster.storage.management.console.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.console.views.pages.DisksPage;
import org.gluster.storage.management.core.model.Disk;
import org.gluster.storage.management.core.model.EntityGroup;
import org.gluster.storage.management.core.model.GlusterServer;


public class DisksView extends ViewPart {
	public static final String ID = DisksView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private EntityGroup<GlusterServer> servers;
	private DisksPage page;

	@SuppressWarnings("unchecked")
	@Override
	public void createPartControl(Composite parent) {
		if (servers == null) {
			servers = guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
		}
		
		page = new DisksPage(parent, SWT.NONE, getSite(), getAllDisks(servers));
		//page.layout(); // IMP: lays out the form properly
	}

	private List<Disk> getAllDisks(EntityGroup<GlusterServer> servers) {
		List<Disk> disks = new ArrayList<Disk>();
		for(GlusterServer server : servers.getEntities()) {
			disks.addAll(server.getDisks());
		}
		return disks;
	}

	@Override
	public void setFocus() {
		page.setFocus();
	}
}