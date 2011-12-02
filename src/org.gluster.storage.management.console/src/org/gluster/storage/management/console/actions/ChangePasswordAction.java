/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.actions;

import org.eclipse.jface.action.IAction;
import org.gluster.storage.management.console.dialogs.ChangePasswordDialog;


public class ChangePasswordAction extends AbstractActionDelegate {

	@Override
	protected void performAction(IAction action) {
		try {
			// To open a dialog for change password
			ChangePasswordDialog dialog = new ChangePasswordDialog(getShell());
			dialog.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
	}
}
