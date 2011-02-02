package com.gluster.storage.management.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.gui.login.LoginDialog;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	public static final String PLUGIN_ID = "com.gluster.storage.management.gui";
	private static Application instance;
	private List<IEntityListener> entityListeners = Collections.synchronizedList(new ArrayList<IEntityListener>());

	public Application() {
		instance = this;
	}

	public static Application getApplication() {
		return instance;
	}

	private boolean login() {
		LoginDialog loginDialog = new LoginDialog(new Shell(Display.getDefault()));
		return (loginDialog.open() == Window.OK);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();

		final boolean[] loginSuccess = new boolean[1];
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				loginSuccess[0] = login();
			}
		});

		if (!loginSuccess[0]) {
			return IApplication.EXIT_OK;
		}
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	public void addEntityListener(IEntityListener listener) {
		entityListeners.add(listener);
	}

	public void entityChanged(Entity entity, String[] paremeters) {
		for (IEntityListener listener : entityListeners) {
			listener.entityChanged(entity, paremeters);
		}
	}
}
