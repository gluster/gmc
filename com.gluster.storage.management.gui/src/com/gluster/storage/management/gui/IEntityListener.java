package com.gluster.storage.management.gui;

import com.gluster.storage.management.core.model.Entity;

/**
 * Any class that is interested in changes to entities in application scope should implement this interface and register
 * with the application using {@link Application#addEntityListener(IEntityListener)}
 * 
 * @author root
 * 
 */
public interface IEntityListener {
	/**
	 * This method is called whenever any attribute of an entity in application scope changes 
	 * @param entity Entity that has changed
	 * @param paremeters List of attribute names that have changed. This can be null.
	 */
	public void entityChanged(Entity entity, String[] paremeters);
}
