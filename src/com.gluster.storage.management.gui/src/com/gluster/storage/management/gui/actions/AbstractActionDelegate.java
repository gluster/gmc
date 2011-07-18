/*******************************************************************************
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.gluster.storage.management.gui.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.UIPlugin;

import com.gluster.storage.management.core.model.Entity;

/**
 * All action delegates in the application should extend from this class. It provides common functionality of grabbing
 * the Window object on initialization and extracting the selected entity in case of selection change on the navigation
 * tree.
 */
@SuppressWarnings("restriction")
public abstract class AbstractActionDelegate implements IWorkbenchWindowActionDelegate {
	protected IWorkbenchWindow window;
	protected Logger logger = Logger.getLogger(this.getClass());
	
	// the latest selected entity
	protected Entity selectedEntity;

	@Override
	public void run(final IAction action) {
		// Real action code must be executed using Display#asyncExec.
		// Otherwise the system can hang when opening new dialog boxes on linux platform
		try {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					performAction(action);
				}
			});
		} catch (final Exception e) {
			final String actionDesc = action.getDescription();
			logger.error("Exception while running action [" + actionDesc + "]",  e);

			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					showErrorDialog(actionDesc, e.getMessage());
				}
			});
		}
	}

	abstract protected void performAction(final IAction action);

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			Entity selectedEntity = (Entity) ((StructuredSelection) selection).getFirstElement();

			if (this.selectedEntity == selectedEntity) {
				// entity selection has not changed. do nothing.
				return;
			}

			if (selectedEntity != null) {
				this.selectedEntity = selectedEntity;
			}
		}
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	protected Shell getShell() {
		return getWindow().getShell();
	}
	
	protected IWorkbenchWindow getWindow() {
		return window == null ? UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() : window;
	}

	protected void showInfoDialog(final String title, final String message) {
		MessageDialog.openInformation(getShell(), title, message);
	}

	protected void showWarningDialog(final String title, final String message) {
		MessageDialog.openWarning(getShell(), title, message);
	}

	protected void showErrorDialog(final String title, final String message) {
		MessageDialog.openError(getShell(), title, message);
	}

	protected boolean showConfirmDialog(final String title, final String message) {
		return MessageDialog.openQuestion(getShell(), title, message);
	}
}
