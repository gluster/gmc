package com.gluster.storage.management.gui.views.details;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.gluster.storage.management.core.model.Entity;

/**
 * For every entity that can be selected from the navigation view (cluster tree), a set of tabs are created on the
 * details view. Each entity has a corresponding tab creator that creates these tabs. These tab creators must implement
 * this interface.
 * <p>
 * <b>Important:</b> Tab creators are cached for performance reasons. Hence they should not store any state information
 * in class level variables.
 */
public interface TabCreator {
	/**
	 * Creates tabs for the given entity
	 * 
	 * @param entity
	 *            Entity for which tabs are to be created
	 * @param tabFolder
	 *            The tab folder in which the tabs are to be created
	 * @param toolkit
	 *            The form toolkit that can be used for create components using Forms API
	 * @param site
	 *            The workbench site that can be used to register as a selection provider
	 */
	public void createTabs(Entity entity, TabFolder tabFolder, FormToolkit toolkit, IWorkbenchSite site);
}
