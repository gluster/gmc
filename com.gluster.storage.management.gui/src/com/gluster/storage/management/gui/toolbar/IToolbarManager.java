package com.gluster.storage.management.gui.toolbar;

import com.gluster.storage.management.core.model.Entity;

/**
 * Whenever the current selection/action demands changes to the toolbar, the toolbar manager is used to update the
 * toolbar.
 */
public interface IToolbarManager {
	/**
	 * Updates the toolbar for given entity. This typically means that user is working with the given entity, and hence
	 * the toolbar actions related to that entity should be made visible, and other un-related actions should be hidden.
	 * 
	 * @param entity
	 */
	public void updateToolbar(Entity entity);
}
