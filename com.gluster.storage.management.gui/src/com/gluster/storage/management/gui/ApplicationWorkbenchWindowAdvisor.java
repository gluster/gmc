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
	}	
}
