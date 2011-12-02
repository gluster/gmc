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
package org.gluster.storage.management.console.views.pages;

import java.util.List;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.model.ClusterListener;
import org.gluster.storage.management.core.model.Disk;


public abstract class AbstractTableTreeViewerPage<T> extends Composite implements ISelectionListener {

	protected final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	protected TreeViewer treeViewer;
	protected GUIHelper guiHelper = GUIHelper.getInstance();
	protected Composite parent;
	protected IWorkbenchSite site;
	private ClusterListener clusterListener;
	
	private Text filterText;

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}
	
	private Composite createTreeViewerComposite() {
		Composite tableViewerComposite = new Composite(this, SWT.NO);
		tableViewerComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridData layoutData = new GridData();
		layoutData.horizontalSpan=5;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.grabExcessVerticalSpace = true;
		tableViewerComposite.setLayoutData(layoutData);
		
		return tableViewerComposite;
	}


	public AbstractTableTreeViewerPage(IWorkbenchSite site, final Composite parent, int style, boolean useChechboxes,
			boolean multiSelection, List<Disk> allDisks) {
		super(parent, style);
		
		setupPageLayout();
		//new FormToolkit(Display.getCurrent()).createButton(this, "test1", SWT.PUSH);
		
		this.parent = parent;
		this.site = site;

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayoutData(data);
		
		filterText = guiHelper.createFilterText(toolkit, this);
		
		Composite tableViewerComposite = createTreeViewerComposite();
		createTreeViewer(allDisks, tableViewerComposite);
		parent.layout(); // Important - this actually paints the table
		
		createListeners(parent); 
	}
	
	private void createListeners(final Composite parent) {
		/**
		 * Ideally not required. However the table viewer is not getting laid out properly on performing
		 * "maximize + restore" So this is a hack to make sure that the table is laid out again on re-size of the window
		 */
		addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				parent.layout();
			}
		});
		
		clusterListener = createClusterListener();
		GlusterDataModelManager.getInstance().addClusterListener(clusterListener);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
				GlusterDataModelManager.getInstance().removeClusterListener(clusterListener);
			}
		});
	}

	protected abstract ClusterListener createClusterListener();
	protected abstract IBaseLabelProvider getLabelProvider();
	protected abstract IContentProvider getContentProvider();
	
	private void createTreeViewer(List<Disk> allDisks, Composite tableViewerComposite) {
		treeViewer = new TreeViewer(tableViewerComposite);
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setLinesVisible(true);
		
		TreeColumnLayout ad = new TreeColumnLayout();
		tableViewerComposite.setLayout(ad);
		
		TreeColumn column = new TreeColumn(treeViewer.getTree(),SWT.NONE);
		column.setWidth(100);
		column.setText("Disk");
		ad.setColumnData(column, new ColumnWeightData(50, 100));
		
		column = new TreeColumn(treeViewer.getTree(),SWT.NONE);
		column.setWidth(100);
		column.setText("Partition");
		ad.setColumnData(column,new ColumnWeightData(50, 100));
		
		column = new TreeColumn(treeViewer.getTree(),SWT.NONE);
		column.setWidth(100);
		column.setText("Free Space (GB)");
		ad.setColumnData(column, new ColumnWeightData(50, 100));
		
		column = new TreeColumn(treeViewer.getTree(),SWT.NONE);
		column.setWidth(100);
		column.setText("Total Space (GB)");
		ad.setColumnData(column,new ColumnWeightData(50, 100));
		
		column = new TreeColumn(treeViewer.getTree(),SWT.NONE);
		column.setWidth(100);
		column.setText("Status");
		ad.setColumnData(column,new ColumnWeightData(50, 100));
		
		treeViewer.setLabelProvider(getLabelProvider());
		treeViewer.setContentProvider(getContentProvider());
		treeViewer.setInput(allDisks);
		
		// Create a case insensitive filter for the table viewer using the filter text field
		guiHelper.createFilter(treeViewer, filterText, false);
		
		treeViewer.expandAll();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}
}
