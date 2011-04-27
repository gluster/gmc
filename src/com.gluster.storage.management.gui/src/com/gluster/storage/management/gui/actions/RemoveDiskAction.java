package com.gluster.storage.management.gui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.utils.GUIHelper;
import com.gluster.storage.management.gui.views.VolumeDisksView;

public class RemoveDiskAction extends AbstractActionDelegate {
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
	private GUIHelper guiHelper = GUIHelper.getInstance();
	private List<Disk> disks;

	@Override
	protected void performAction(IAction action) {
		VolumesClient client = new VolumesClient(modelManager.getSecurityToken());
		// final Status status = client.removeDisk();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);

		action.setEnabled(false);
		Volume selectedVolume = (Volume)guiHelper.getSelectedEntity(window, Volume.class);
		if (selectedVolume != null) {
			// a volume is selected on navigation tree. Let's check if the currently open view is volume disks view
			IWorkbenchPart view = guiHelper.getActiveView();
			if(view instanceof VolumeDisksView) {
				// volume disks view is open. check if any disk is selected
				disks = getSelectedDisks(selection);
				action.setEnabled(disks.size() > 0);
			}
		}
	}

	private List<Disk> getSelectedDisks(ISelection selection) {
		List<Disk> selectedDisks = new ArrayList<Disk>();
		if (selection instanceof IStructuredSelection) {
			Iterator<Object> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object selectedObj = iter.next();
				if (selectedObj instanceof Disk) {
					selectedDisks.add((Disk)selectedObj);
				}
			}
		}
		return selectedDisks;
	}
}