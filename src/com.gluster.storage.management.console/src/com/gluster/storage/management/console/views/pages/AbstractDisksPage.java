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
package com.gluster.storage.management.console.views.pages;

import java.net.URI;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.client.TasksClient;
import com.gluster.storage.management.console.Application;
import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.IEntityListener;
import com.gluster.storage.management.console.dialogs.InitializeDiskTypeSelection;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.core.model.DefaultClusterListener;
import com.gluster.storage.management.core.model.Device;
import com.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.model.Entity;
import com.gluster.storage.management.core.model.Event;
import com.gluster.storage.management.core.model.Event.EVENT_TYPE;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.TaskInfo;

public abstract class AbstractDisksPage extends AbstractTableTreeViewerPage<Disk> implements IEntityListener {
	protected List<Disk> disks;
	
	/**
	 * @return Index of the "status" column in the table. Return -1 if status column is not displayed
	 */
	protected abstract int getStatusColumnIndex();

	public AbstractDisksPage(final Composite parent, int style, IWorkbenchSite site, List<Disk> disks) {
		super(site, parent, style, false, true, disks);
		this.disks = disks;
		
		// creates hyperlinks for "unitialized" disks
		setupStatusCellEditor(); 
		// Listen for disk status change events
		Application.getApplication().addEntityListener(this);
	}
	
	protected ClusterListener createClusterListener() {
		return new DefaultClusterListener() {
			@Override
			public void serverChanged(GlusterServer server, Event event) {
				super.serverChanged(server, event);
				EVENT_TYPE eventType = event.getEventType();
				switch (eventType) {
				case DEVICES_REMOVED:
				case DEVICES_ADDED:
				case DEVICES_CHANGED:
				case GLUSTER_SERVER_CHANGED:
					treeViewer.refresh(true);
				default:
					break;
				}
			}
		};
	}
	
	private void createInitializeLink(final TreeItem item, final int rowNum, final Device uninitializedDevice) {
		final Tree tree = treeViewer.getTree();
		final TreeEditor editor = new TreeEditor(tree);
		editor.grabHorizontal = true;
		editor.horizontalAlignment = SWT.RIGHT;

		tree.addPaintListener(new PaintListener() {
			private TreeItem myItem = item;
			private int myRowNum = rowNum;
			private ImageHyperlink myLink = null;
			private TreeEditor myEditor = null;

			private void createLinkFor(Device uninitializedDevice, TreeItem item1, int rowNum1) {
				myItem = item1;
				myRowNum = rowNum1;

				myEditor = new TreeEditor(tree);
				myEditor.grabHorizontal = true;
				myEditor.horizontalAlignment = SWT.RIGHT;

				myLink = toolkit.createImageHyperlink(tree, SWT.NONE);
				// link.setImage(guiHelper.getImage(IImageKeys.DISK_UNINITIALIZED));
				myLink.setText("Initialize");
				myLink.addHyperlinkListener(new StatusLinkListener(myLink, myEditor, treeViewer, uninitializedDevice));

				myEditor.setEditor(myLink, item1, getStatusColumnIndex());

				myItem.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						myLink.dispose();
						myEditor.dispose();
					}
				});
			}

			@Override
			public void paintControl(PaintEvent e) {
				int itemCount = tree.getItemCount();

				// Find the table item corresponding to our disk

				Device device = null;
				int rowNum1 = -1;
				TreeItem item1 = null;
				
				mainloop:
				for (int i = 0; i < itemCount; i++) {
					item1 = tree.getItem(i);
					
					device = (Device) item1.getData();
					if (device != null && device == uninitializedDevice) {
						// this is an uninitialized "disk"
						rowNum1 = i;
						break;
					}
					
					int partitionCount = item1.getItemCount();
					for(int j = 0; j < partitionCount; j++) {
						TreeItem partitionItem = item1.getItem(j);
						// check each partition
						Device partition = (Device)partitionItem.getData();
						if(partition != null && partition == uninitializedDevice) {
							// this is an uninitialized "partition"
							rowNum1 = i + j;
							item1 = partitionItem;
							device = partition;
							// found the uninitialized device. break out.
							break mainloop;
						}
					}
				}

				if (rowNum1 == -1) {
					// item disposed and disk not visible. nothing to do.
					return;
				}

				if (myEditor == null || myItem.isDisposed()) {
					// item visible, and
					// either editor never created, OR
					// old item disposed. create the link for it
					createLinkFor(device, item1, rowNum1);
				}

				if (rowNum1 != myRowNum) {
					// disk visible, but at a different row num. re-create the link
					myLink.dispose();
					myEditor.dispose();
					createLinkFor(device, item1, rowNum1);
				}

				myEditor.layout(); // IMPORTANT. Without this, the link location goes for a toss on maximize + restore
			}
		});
	}

	private void setupStatusCellEditor() {
		final TreeViewer viewer = treeViewer;
		final Tree tree = viewer.getTree();
		int rowNum = 0;
		for (int i = 0; i < tree.getItemCount(); i++, rowNum++) {
			final TreeItem item = tree.getItem(i);
			if (item.isDisposed() || item.getData() == null) {
				continue;
			}
			final Disk disk = (Disk) item.getData();
			if (disk.isUninitialized()) {
				createInitializeLink(item, rowNum, disk);
			}
			
			if (disk.hasPartitions()) {
				for(int j = 0; j < disk.getPartitions().size(); j++, rowNum++) {
					TreeItem partitionItem = item.getItem(j);
					// check each partition
					Device partition = (Device)partitionItem.getData();
					if (partition.isUninitialized()) {
						createInitializeLink(partitionItem, rowNum, partition);
					}
				}
			}
		}
	}

	private final class StatusLinkListener extends HyperlinkAdapter {
		private final Device device;
		private final TreeEditor myEditor;
		private final ImageHyperlink myLink;
		private final TreeViewer viewer;

		private StatusLinkListener(ImageHyperlink link, TreeEditor editor, TreeViewer viewer, Device device) {
			this.device = device;
			this.viewer = viewer;
			this.myEditor = editor;
			this.myLink = link;
		}

		private void updateStatus(final DEVICE_STATUS status, final boolean disposeEditor) {
			if (disposeEditor) {
				myLink.dispose();
				myEditor.dispose();
			}
			device.setStatus(status);
			viewer.update(device, new String[] { "status" });
			Application.getApplication().entityChanged(device, new String[] { "status" });
		}

		@Override
		public void linkActivated(HyperlinkEvent e) {
			GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
			
			// If the same task is already running return
			String reference = device.getServerName() + ":" + device.getName();
			TaskInfo existingTaskInfo = modelManager.getTaskByReference(reference);
			if (existingTaskInfo != null && existingTaskInfo.getStatus().getCode() != Status.STATUS_CODE_SUCCESS
					&& existingTaskInfo.getStatus().getCode() != Status.STATUS_CODE_FAILURE) {
				MessageDialog.openInformation(getShell(), "Error: Initialize disk", "Initializing disk [" + reference
						+ "] is already in progress! Try later.");
				return;
			}
			
			InitializeDiskTypeSelection formatDialog = new InitializeDiskTypeSelection(getShell());
			int userAction = formatDialog.open();
			if (userAction == Window.CANCEL) {
				formatDialog.cancelPressed();
				return;
			}

			GlusterServersClient serversClient = new GlusterServersClient();
			try {
				
				URI uri = serversClient.initializeDisk(device.getServerName(), device.getName(), formatDialog.getFSType());

				TasksClient taskClient = new TasksClient();
				TaskInfo taskInfo = taskClient.getTaskInfo(uri);
				
				if (taskInfo != null && taskInfo instanceof TaskInfo) {
					modelManager.addTask(taskInfo);
				}

				if (taskInfo.getStatus().getCode() == Status.STATUS_CODE_RUNNING) {
					updateStatus(DEVICE_STATUS.INITIALIZING, true);
				} else if (taskInfo.getStatus().getCode() == Status.STATUS_CODE_SUCCESS) {
					// If format completed (instantly), get the server details and update the server in the model   
					GlusterServer oldServer = modelManager.getModel().getCluster().getServer(device.getServerName());  
					GlusterServer newServer = serversClient.getGlusterServer(device.getServerName());
					modelManager.glusterServerChanged(oldServer, newServer);
					// updateStatus(DEVICE_STATUS.INITIALIZED, true);
					// GlusterDataModelManager.getInstance().updateDeviceStatus(device.getServerName(), device.getName(),
					//		DEVICE_STATUS.INITIALIZED);
				} else {
					MessageDialog.openError(getShell(), "Error: Initialize disk", taskInfo.getStatus().getMessage());
				}
				guiHelper.showTaskView();
			} catch (Exception e1) {
				MessageDialog.openError(getShell(), "Error: Initialize disk", e1.getMessage());
			}
		}
	}

	@Override
	public void entityChanged(final Entity entity, final String[] paremeters) {
		if (!(entity instanceof Device)) {
			return;
		}
		final Device device = (Device) entity;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				treeViewer.update(device, paremeters);

				if (device.isUninitialized()) {
					Tree tree = treeViewer.getTree();
					
					for (int rowNum = 0; rowNum < tree.getItemCount(); rowNum++) {
						TreeItem item = tree.getItem(rowNum);
						if (item.getData() == device) {
							createInitializeLink(item, rowNum, device);
						}
					}
				}
			}
		});
	}
}
