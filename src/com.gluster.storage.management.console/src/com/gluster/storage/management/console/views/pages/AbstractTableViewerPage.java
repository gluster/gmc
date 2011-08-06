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

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.console.utils.GUIHelper;
import com.gluster.storage.management.core.model.ClusterListener;

public abstract class AbstractTableViewerPage<T> extends Composite implements ISelectionListener {

	private boolean useCheckboxes;
	private boolean multiSelection;
	
	protected final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	protected TableViewer tableViewer;
	protected GUIHelper guiHelper = GUIHelper.getInstance();
	protected Composite parent;
	protected IWorkbenchSite site;
	
	private Hyperlink linkAll, linkNone;
	private ClusterListener clusterListener;

	public AbstractTableViewerPage(IWorkbenchSite site, final Composite parent, int style, boolean useChechboxes, boolean multiSelection, Object model) {
		super(parent, style);
		this.parent = parent;
		this.site = site;
		
		this.useCheckboxes = useChechboxes;
		this.multiSelection = multiSelection;

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		setupPageLayout();
		
		createCheckboxSelectionLinks();

		Text filterText = guiHelper.createFilterText(toolkit, this);
		
		setupTableViewer(site, filterText);
		tableViewer.setInput(model);
		// register as selection provider so that other views can listen to any selection events on the tree
		site.setSelectionProvider(tableViewer);
		site.getPage().addSelectionListener(this);

		
		parent.layout(); // Important - this actually paints the table

		createListeners(parent);
	}

	public void createCheckboxSelectionLinks() {
		if (useCheckboxes) {
			// create the "select all/none" links
			toolkit.createLabel(this, "Select");
			linkAll = toolkit.createHyperlink(this, "all", SWT.NONE);
			linkAll.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
					((CheckboxTableViewer) tableViewer).setAllChecked(true);
					tableViewer.setSelection(new StructuredSelection(getAllEntities()));
				}
			});

			toolkit.createLabel(this, " / ");

			linkNone = toolkit.createHyperlink(this, "none", SWT.NONE);
			linkNone.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
					((CheckboxTableViewer) tableViewer).setAllChecked(false);
					tableViewer.setSelection(null);
				}
			});
		} else {
			// create dummy labels to maintain layout
			toolkit.createLabel(this, "");
			toolkit.createLabel(this, "");
			toolkit.createLabel(this, "");
			toolkit.createLabel(this, "");
		}
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
	protected abstract String[] getColumnNames();
	protected abstract void setColumnProperties(Table table);
	protected abstract IBaseLabelProvider getLabelProvider();
	protected abstract IContentProvider getContentProvider();
	protected abstract List<T> getAllEntities();

	public void addDoubleClickListener(IDoubleClickListener listener) {
		tableViewer.addDoubleClickListener(listener);
	}

	private void setupPageLayout() {
		final GridLayout layout = new GridLayout(5, false);
		layout.verticalSpacing = 10;
		layout.marginTop = 10;
		setLayout(layout);
	}

	protected void setupTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, getColumnNames());
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table);
	}

	private void createTableViewer(Composite parent) {
		int style = SWT.FLAT | SWT.FULL_SELECTION;
		style |= (multiSelection ? SWT.MULTI : SWT.SINGLE);
		
		if(useCheckboxes) {
			tableViewer = CheckboxTableViewer.newCheckList(parent, style);
		} else {
			tableViewer = new TableViewer(parent, style);
		}
		
		tableViewer.setLabelProvider(getLabelProvider());
		tableViewer.setContentProvider(getContentProvider());
		setupTable(parent, tableViewer.getTable());
	}

	private Composite createTableViewerComposite() {
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

	private void setupTableViewer(IWorkbenchSite site, final Text filterText) {
		Composite tableViewerComposite = createTableViewerComposite();
		createTableViewer(tableViewerComposite);
		site.setSelectionProvider(tableViewer);

		if(useCheckboxes) {
			// make sure that table selection is driven by checkbox selection
			guiHelper.configureCheckboxTableViewer((CheckboxTableViewer)tableViewer);
		}

		// Create a case insensitive filter for the table viewer using the filter text field
		guiHelper.createFilter(tableViewer, filterText, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}
}
