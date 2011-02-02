package com.gluster.storage.management.core.model;

/**
 * By default, the {@link EntityViewerFilter} filters the objects by parsing the
 * output of {@link Object#toString()} with the filter string. Classes that need
 * specific filtering logic can implement this interface. The default logic will
 * then be overridden by the method {@link Filterable#filter(String)}.
 */
public interface Filterable {
	/**
	 * @param filterString
	 *            String to be used for filtering
	 * @param caseSensitive
	 *            Flag indicating whether the filtering should be case sensitive
	 * @return true if the object can be selected using the filter string, else
	 *         false
	 */
	public boolean filter(String filterString, boolean caseSensitive);
}
