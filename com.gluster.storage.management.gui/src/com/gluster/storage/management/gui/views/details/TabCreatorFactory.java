package com.gluster.storage.management.gui.views.details;

import com.gluster.storage.management.core.model.Entity;

/**
 * Interface for tab creator factory.
 */
public interface TabCreatorFactory {
	/**
	 * @param entity The entity for which tab creator factory is to be returned
	 * @return A tab creator factory for given entity
	 */
	public TabCreator getTabCreator(Entity entity);
}
