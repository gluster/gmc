package com.gluster.storage.management.gui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.pages.DisksPage;

public class DisksView extends ViewPart {
	public static final String ID = DisksView.class.getName();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private EntityGroup<GlusterServer> servers;
	private DisksPage page;

	@Override
	public void createPartControl(Composite parent) {
		if (servers == null) {
			servers = (EntityGroup<GlusterServer>)guiHelper.getSelectedEntity(getSite(), EntityGroup.class);
		}
		
		page = new DisksPage(parent, SWT.NONE, getSite(), getAllDisks(servers));
		//page.layout(); // IMP: lays out the form properly
	}

	private List<Disk> getAllDisks(EntityGroup<GlusterServer> servers) {
		List<Disk> disks = new ArrayList<Disk>();
		for(GlusterServer server : (List<GlusterServer>)servers.getEntities()) {
			disks.addAll(server.getDisks());
		}
		return disks;
	}

	@Override
	public void setFocus() {
		page.setFocus();
	}
}