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
package com.gluster.storage.management.gui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.gluster.storage.management.gui.utils.GUIHelper;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
	private final static int DEFAULT_WIDTH = 1024;
	private final static int DEFAULT_HEIGHT = 768;
	private final GUIHelper guiHelper = GUIHelper.getInstance();
	
	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		super.preWindowOpen();
		
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowMenuBar(true);
		configurer.setShowProgressIndicator(true); // shows progress indicator in status bar
	}

	@Override
	public void postWindowCreate() {
		super.postWindowCreate();
		guiHelper.centerShellInScreen(getWindowConfigurer().getWindow().getShell());
		getWindowConfigurer().getWindow().getShell().setMaximized(true);
		Application.getApplication().setStatusLineManager(
				getWindowConfigurer().getActionBarConfigurer().getStatusLineManager());
	}	
}
