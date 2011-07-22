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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.gluster.storage.management.gui.utils.GUIHelper;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of the actions added to a workbench window.
 * Each window will be populated with new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private IWorkbenchWindow window;
	/*
	 * Actions - important to allocate these only in makeActions, and then use them in the fill methods. This ensures
	 * that the actions aren't recreated when fillActionBars is called with FILL_PROXY.
	 */
	private IWorkbenchAction exitAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction helpContentsAction;

	private GUIHelper guiHelper = GUIHelper.getInstance();

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		this.window = window;
		/*
		 * Creates the actions and registers them. Registering is needed to ensure that key bindings work. The
		 * corresponding commands keybindings are defined in the plugin.xml file. Registering also provides automatic
		 * disposal of the actions when the window is closed.
		 */
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		aboutAction.setText("&About");
		aboutAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
				IImageKeys.HELP_16x16));
		register(aboutAction);
		
		helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		helpContentsAction.setText("&Contents");
		//helpContentsAction.setImageDescriptor(newImage)
		register(helpContentsAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		// File
		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);

		// Help
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		helpMenu.add(helpContentsAction);
		helpMenu.add(aboutAction);

		menuBar.add(fileMenu);
		// Add a group marker indicating where action set menus will appear.
		// All action sets from plugin.xml will get added here
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(helpMenu);
	}

	protected void fillCoolBar(ICoolBarManager coolBar) {
		// All our actions are added to toolbar through the extension point org.eclipse.ui.actionSets 
	}

}
