package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.gluster.storage.management.core.model.Entity;

/**
 * All action delegates in the application should extend from this class. It provides common functionality of grabbing
 * the Window object on initialization and extracting the selected entity in case of selection change on the navigation
 * tree.
 */
public abstract class AbstractActionDelegate implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow window;
	protected Entity selectedEntity;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			Entity selectedEntity = (Entity) ((StructuredSelection) selection).getFirstElement();

			if (this.selectedEntity == selectedEntity) {
				// entity selection has not changed. do nothing.
				return;
			}

			if(selectedEntity != null) {
				this.selectedEntity = selectedEntity;
			}
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
