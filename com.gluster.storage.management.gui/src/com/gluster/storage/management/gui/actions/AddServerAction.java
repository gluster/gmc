package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.gluster.storage.management.core.model.EntityGroup;
import com.gluster.storage.management.core.model.Volume;

public class AddServerAction extends AbstractActionDelegate {
	@Override
	public void run(IAction action) {
		System.out.println("Running [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void dispose() {
		System.out.println("Disposing [" + this.getClass().getSimpleName() + "]");
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		
		action.setEnabled(true);
		if(selectedEntity instanceof EntityGroup && ((EntityGroup)selectedEntity).getEntityType() == Volume.class) {
			action.setEnabled(false);
		}
	}
}
