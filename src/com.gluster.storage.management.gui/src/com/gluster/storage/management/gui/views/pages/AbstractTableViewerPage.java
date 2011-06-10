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
package com.gluster.storage.management.gui.views.pages;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.ClusterListener;
import com.gluster.storage.management.gui.utils.GUIHelper;

public abstract class AbstractTableViewerPage<T> extends Composite {

	protected final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
	protected CheckboxTableViewer tableViewer;
	protected GUIHelper guiHelper = GUIHelper.getInstance();
	protected Composite parent;
	
	private Hyperlink linkAll, linkNone;

	public AbstractTableViewerPage(IWorkbenchSite site, final Composite parent, int style, Object model) {
		super(parent, style);
		this.parent = parent;

		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		setupPageLayout();
		
		createCheckboxSelectionLinks();

		Text filterText = guiHelper.createFilterText(toolkit, this);
		
		setupServerTableViewer(site, filterText);

		tableViewer.setInput(model);
		parent.layout(); // Important - this actually paints the table

		createListeners(parent);
	}

	public void createCheckboxSelectionLinks() {
		// create the "select all/none" links
		toolkit.createLabel(this, "Select");
		linkAll = toolkit.createHyperlink(this, "all", SWT.NONE);
		linkAll.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
				tableViewer.setAllChecked(true);
				tableViewer.setSelection(new StructuredSelection(getAllEntities()));
			}
		});
		
		toolkit.createLabel(this, " / ");
		
		linkNone = toolkit.createHyperlink(this, "none", SWT.NONE);
		linkNone.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
				tableViewer.setAllChecked(false);
				tableViewer.setSelection(null);
			}
		});
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
		
		final ClusterListener clusterListener = createClusterListener();
		
		final GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();
		modelManager.addClusterListener(clusterListener);

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				toolkit.dispose();
				modelManager.removeClusterListener(clusterListener);
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

	private void setupServerTable(Composite parent, Table table) {
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		TableColumnLayout tableColumnLayout = guiHelper.createTableColumnLayout(table, getColumnNames());
		parent.setLayout(tableColumnLayout);

		setColumnProperties(table);
	}

	private CheckboxTableViewer createServerTableViewer(Composite parent) {
		CheckboxTableViewer tableViewer = CheckboxTableViewer.newCheckList(parent, SWT.FLAT | SWT.FULL_SELECTION | SWT.MULTI);
		
		tableViewer.setLabelProvider(getLabelProvider());
		tableViewer.setContentProvider(getContentProvider());

		setupServerTable(parent, tableViewer.getTable());

		return tableViewer;
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
		tableViewerComposite.setLayoutData(layoutData);
		
		return tableViewerComposite;
	}

	private void setupServerTableViewer(IWorkbenchSite site, final Text filterText) {
		Composite tableViewerComposite = createTableViewerComposite();
		tableViewer = createServerTableViewer(tableViewerComposite);
		site.setSelectionProvider(tableViewer);

		// make sure that table selection is driven by checkbox selection
		guiHelper.configureCheckboxTableViewer(tableViewer);

		// Create a case insensitive filter for the table viewer using the filter text field
		guiHelper.createFilter(tableViewer, filterText, false);
	}
}
