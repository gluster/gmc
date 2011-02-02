package com.gluster.storage.management.gui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IProgressConstants2;

import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Disk.DISK_STATUS;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class InitializeDiskJob extends Job {
	private Disk disk;
	private Application app = Application.getApplication();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();

	public InitializeDiskJob(Disk disk) {
		super("Initialize Disk [" + disk.getQualifiedName() + "]");
		this.disk = disk;
		setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		setProperty(IProgressConstants.ICON_PROPERTY, guiHelper.getImageDescriptor(IImageKeys.WORK_IN_PROGRESS));
	}

	private void updateStatus(final DISK_STATUS status) {
		disk.setStatus(status);
		disk.setSpaceInUse(0d);
		app.entityChanged(disk, new String[] { "status", "spaceInUse" });
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask("Formatting the disk...", 100);
		boolean cancelRequested = false;
		String msgPrefix = "";
		try {
			for (int i = 1; i <= 5; i++) {
				if (!cancelRequested && monitor.isCanceled()) {
					cancelRequested = true; // come only once here
					
					msgPrefix = "This task cannot be cancelled! ";
					monitor.setTaskName(msgPrefix);
//					setProperty(IProgressConstants.ICON_PROPERTY, guiHelper.getImageDescriptor(IImageKeys.STATUS_CANCELLED));
//
//					updateStatus(DISK_STATUS.UNINITIALIZED);
//					return new Status(Status.CANCEL, Application.PLUGIN_ID, "Cancelled");
				}
				Thread.sleep(2000);
				monitor.worked(20);
				monitor.setTaskName(msgPrefix + "[" + 20 * i + "%] completed");
			}
			monitor.done();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		updateStatus(DISK_STATUS.READY);
		setProperty(IProgressConstants.ICON_PROPERTY, guiHelper.getImageDescriptor(IImageKeys.STATUS_SUCCESS));

		return new Status(Status.OK, Application.PLUGIN_ID, "Task Completed!");
	}
}
