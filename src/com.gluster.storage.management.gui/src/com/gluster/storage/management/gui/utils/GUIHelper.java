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
package com.gluster.storage.management.gui.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.progress.IProgressConstants;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Disk;
import com.gluster.storage.management.core.utils.JavaUtil;
import com.gluster.storage.management.gui.Application;
import com.gluster.storage.management.gui.IImageKeys;
import com.gluster.storage.management.gui.views.NavigationView;
import com.gluster.storage.management.gui.views.TasksView;

public class GUIHelper {
	private static final GUIHelper instance = new GUIHelper();
	private static final ImageUtil imageUtil = new ImageUtil();

	private GUIHelper() {

	}

	public static GUIHelper getInstance() {
		return instance;
	}

	public ScrolledForm setupForm(Composite parent, FormToolkit toolkit, final String formName) {
		return setupForm(toolkit, formName, toolkit.createScrolledForm(parent));
	}

	public ScrolledForm setupForm(FormToolkit toolkit, final String formName, ScrolledForm form) {
		form.setText(formName);
		toolkit.decorateFormHeading(form.getForm());

		ColumnLayout layout = new ColumnLayout();

		// layout.topMargin = 0;
		// layout.bottomMargin = 5;
		// layout.leftMargin = 10;
		// layout.rightMargin = 10;
		// layout.horizontalSpacing = 10;
		// layout.verticalSpacing = 10;
		// layout.maxNumColumns = 4;
		// layout.minNumColumns = 1;

		form.getBody().setLayout(layout);
		return form;
	}

	public Composite createSection(final ScrolledForm form, FormToolkit toolkit, String title, String desc,
			int numColumns, boolean collapsible) {
		int style = Section.TITLE_BAR | Section.EXPANDED;
		if (desc != null && !desc.isEmpty()) {
			style |= Section.DESCRIPTION;
		}
		if (collapsible) {
			style |= Section.TWISTIE;
		}

		Section section = toolkit.createSection(form.getBody(), style);
		section.setText(title);
		section.setDescription(desc);

		// toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		layout.verticalSpacing = 15;
		layout.marginBottom = 20;
		layout.marginTop = 5;
		
		client.setLayout(layout);
		section.setClient(client);

		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}

	public Composite createTab(TabFolder tabFolder, String title, String imageKey) {
		TabItem item = new TabItem(tabFolder, SWT.NONE);
		item.setText(title);
		item.setImage(getImage(imageKey));

		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());

		item.setControl(composite);

		return composite;
	}

	public ImageDescriptor getImageDescriptor(String imagePath) {
		return imageUtil.getImageDescriptor(imagePath);
	}

	public Image getImage(String imagePath) {
		return imageUtil.getImage(imagePath);
	}

	public Action createPullDownMenu(String menuName, String iconPath, final MenuManager menuManager) {
		Action action = new Action(menuName, IAction.AS_DROP_DOWN_MENU) {
			public void run() {
			}
		};
		action.setMenuCreator(new IMenuCreator() {

			@Override
			public Menu getMenu(Menu menu) {
				return null;
			}

			@Override
			public Menu getMenu(Control control) {
				return menuManager.createContextMenu(control);
			}

			@Override
			public void dispose() {
			}
		});
		action.setImageDescriptor(getImageDescriptor(iconPath));
		return action;
	}

	public TableColumnLayout createTableColumnLayout(Table table, String[] columns) {
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		ColumnLayoutData defaultColumnLayoutData = new ColumnWeightData(100);

		for (String columnName : columns) {
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setText(columnName);

			tableColumnLayout.setColumnData(column, defaultColumnLayoutData);
		}

		return tableColumnLayout;
	}

	/**
	 * Creates a filter for given structured viewer that will filter the contents of the viewer based on the current
	 * text of the text field
	 * 
	 * @param viewer
	 *            Structured viewer for which the filter is to be created
	 * @param filterText
	 *            The text field whose contents are to be used for filtering
	 * @param caseSensitive
	 *            Flag indicating whether the filtering should be case sensitive
	 * @return The newly created filter
	 */
	public EntityViewerFilter createFilter(final StructuredViewer viewer, final Text filterText, boolean caseSensitive) {
		final String initialFilterString = filterText.getText();

		final EntityViewerFilter filter = new EntityViewerFilter(initialFilterString, caseSensitive);
		// On every keystroke inside the text field, update the filter string
		filterText.addKeyListener(new KeyAdapter() {
			private String filterString = initialFilterString;

			@Override
			public void keyReleased(KeyEvent e) {
				String enteredString = filterText.getText();
				if (enteredString.equals(filterString)) {
					// Filter string has not changed. don't do anything
					return;
				}

				// Update filter string
				filterString = enteredString;
				filter.setFilterString(filterString);

				// Refresh viewer with newly filtered content
				viewer.refresh(true);
				if(viewer instanceof TreeViewer) {
					((TreeViewer)viewer).expandAll();
				}
			}
		});

		viewer.addFilter(filter);
		return filter;
	}

	public IViewPart getView(String viewId) {
		IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getViewReferences();
		for (IViewReference view : views) {
			if (view.getId().equals(viewId)) {
				return view.getView(false);
			}
		}
		return null;
	}
	
	public IWorkbenchPart getActiveView() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
	}

	public ControlDecoration createErrorDecoration(Control control) {
		ControlDecoration passwordErrorDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
		passwordErrorDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage());
		return passwordErrorDecoration;
	}

	public void centerShellInScreen(Shell shell) {
		Rectangle monitorBounds = shell.getMonitor().getBounds();
		Rectangle myBounds = shell.getBounds();

		int x = monitorBounds.x + (monitorBounds.width - myBounds.width) / 2;
		int y = monitorBounds.y + (monitorBounds.height - myBounds.height) / 2;
		shell.setLocation(x, y);
	}

	public Text createFilterText(FormToolkit toolkit, Composite parent) {
		final String tooltipMessage = "Start typing to filter table contents.";
		final Text filterText = toolkit.createText(parent, "", SWT.FLAT);

		GridData data = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		data.widthHint = 300;
		filterText.setLayoutData(data);

		ControlDecoration searchDecoration = new ControlDecoration(filterText, SWT.LEFT);
		searchDecoration.setImage(getImage(IImageKeys.SEARCH));
		searchDecoration.show();
		searchDecoration.setShowHover(true);
		searchDecoration.setDescriptionText(tooltipMessage);
		searchDecoration.setMarginWidth(5);

		filterText.setToolTipText(tooltipMessage);
		return filterText;
	}

	public Text createFilterText(Composite parent) {
		final String tooltipMessage = "Start typing to filter table contents.";
		final Text filterText = new Text(parent, SWT.FLAT);

		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		data.widthHint = 300;
		filterText.setLayoutData(data);

		ControlDecoration searchDecoration = new ControlDecoration(filterText, SWT.RIGHT);
		searchDecoration.setImage(getImage(IImageKeys.SEARCH));
		searchDecoration.show();
		searchDecoration.setShowHover(true);
		searchDecoration.setDescriptionText(tooltipMessage);

		filterText.setToolTipText(tooltipMessage);
		return filterText;
	}

	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 */
	public void setColumnProperties(Table table, int columnIndex, int alignment, int weight) {
		TableColumn column = table.getColumn(columnIndex);
		column.setAlignment(alignment);

		TableColumnLayout tableColumnLayout = (TableColumnLayout) table.getParent().getLayout();
		tableColumnLayout.setColumnData(column, new ColumnWeightData(weight));
	}

	/**
	 * Sets properties for alignment and weight of given column of given table
	 * 
	 * @param table
	 * @param columnIndex
	 * @param alignment
	 * @param weight
	 * 
	 * @return The table viewer column created
	 */
	public TableViewerColumn setColumnProperties(TableViewer tableViewer, int columnIndex, int style, int weight) {
		TableViewerColumn column = new TableViewerColumn(tableViewer, style, columnIndex);
		TableColumnLayout tableColumnLayout = (TableColumnLayout) tableViewer.getTable().getParent().getLayout();
		tableColumnLayout.setColumnData(column.getColumn(), new ColumnWeightData(weight));
		column.setLabelProvider(new ColumnLabelProvider());
		return column;
	}

	/**
	 * Fetches the currently selected objects from the workbench site and returns the one of given type. If none of the
	 * selected objects are of given type, returns null
	 * 
	 * @param site
	 *            The workbench site
	 * @param expectedType
	 *            Type of the selected object to look for
	 * @return The selected object of given type if found, else null
	 */
	public <T> T getSelectedEntity(IWorkbenchSite site, Class<T> expectedType) {
		return getSelectedEntity(site.getWorkbenchWindow(), expectedType);
	}
	
	@SuppressWarnings({ "unchecked" })
	public <T> T getSelectedEntity(IWorkbenchWindow window, Class<T> expectedType) {
		ISelection selection = window.getSelectionService().getSelection(NavigationView.ID);
		if (selection instanceof IStructuredSelection) {
			Iterator<Object> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object selectedObj = iter.next();
				if (selectedObj.getClass() == expectedType) {
					return (T)selectedObj;
				}
			}
		}
		return null;
	}
	
	/**
	 * Fetches the currently selected objects from the workbench site and returns those of given type. If none of the
	 * selected objects are of given type, returns null
	 * 
	 * @param site
	 *            The workbench site
	 * @param expectedType
	 *            Type of the selected objects to look for
	 * @return The selected objects of given type if found, else null
	 */
	public <T> Set<T> getSelectedEntities(IWorkbenchSite site, Class<T> expectedType) {
		return getSelectedEntities(site.getWorkbenchWindow(), expectedType);
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> getSelectedEntities(IWorkbenchWindow window, Class<T> expectedType) {
		Set<T> selectedEntities = new HashSet<T>();
		ISelection selection = window.getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator<Object> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object selectedObj = iter.next();
				if (selectedObj.getClass() == expectedType) {
					selectedEntities.add((T) selectedObj);
				}
			}
		}
		return selectedEntities;
	}
	

	public void configureCheckboxTableViewer(final CheckboxTableViewer tableViewer) {
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				tableViewer.setSelection(new StructuredSelection(tableViewer.getCheckedElements()));
			}
		});
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				List<Object> checkedElements = Arrays.asList(tableViewer.getCheckedElements());
				List<Object> selectedElements = ((IStructuredSelection)event.getSelection()).toList();
		
				if (JavaUtil.listsDiffer(checkedElements, selectedElements)) {
					tableViewer.setSelection(new StructuredSelection(tableViewer.getCheckedElements()));
				}
			}
		});
	}
	
	public void showProgressView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(IProgressConstants.PROGRESS_VIEW_ID);
		} catch (PartInitException e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Could not open the progress view!", e);
		}
	}
	
	public void showTaskView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView( TasksView.ID );
		} catch (PartInitException e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Could not open the task progress view!", e);
		}
	}
	
	public void setStatusMessage(String message) {
		Application.getApplication().getStatusLineManager().setMessage(message);
	}
	
	public void clearStatusMessage() {
		Application.getApplication().getStatusLineManager().setMessage(null);
	}
	
	public String getDiskToolTip(Disk disk) {
		return disk.getQualifiedName() + " - " + disk.getDescription();
	}
}
