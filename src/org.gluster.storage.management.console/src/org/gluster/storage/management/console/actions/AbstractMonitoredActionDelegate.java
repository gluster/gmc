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
package org.gluster.storage.management.console.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.gluster.storage.management.console.ConsoleConstants;


/**
 * Any action that can potentially run for a long time, and supports monitoring and progress dialog should extend from
 * this class
 */
public abstract class AbstractMonitoredActionDelegate extends AbstractActionDelegate {
	/* (non-Javadoc)
	 * @see org.gluster.storage.management.console.actions.AbstractActionDelegate#performAction(org.eclipse.jface.action.IAction)
	 */
	@Override
	protected void performAction(final IAction action) {
		try {
			new ProgressMonitorDialog(getShell()).run(false, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					performAction(action, monitor);
				}
			});
		} catch (Exception e) {
			String errMsg = "Exception while performing action [" + action.getDescription() + "] : [" + e.getMessage() + "]";
			logger.error(errMsg, e);
			showErrorDialog(ConsoleConstants.CONSOLE_TITLE, errMsg);
		}
	}
	
	abstract void performAction(IAction action, IProgressMonitor monitor);
}
