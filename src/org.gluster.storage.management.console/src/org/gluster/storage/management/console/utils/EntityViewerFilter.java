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
package org.gluster.storage.management.console.utils;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.gluster.storage.management.core.model.Filterable;
import org.gluster.storage.management.core.utils.StringUtil;


public class EntityViewerFilter extends ViewerFilter {

	private String filterString;
	private boolean caseSensitive = false;

	public EntityViewerFilter(String filterString, boolean caseSensitive) {
		this.filterString = filterString;
		this.caseSensitive = caseSensitive;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public String getFilterString() {
		return filterString;
	}

	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (filterString == null || filterString.isEmpty()) {
			// No filter string. select everything
			return true;
		}

		if (element instanceof Filterable) {
			return ((Filterable) element).filter(filterString, caseSensitive);
		}
		
		if(element instanceof Entry) {
			Entry<String, String> entry = (Entry<String, String>)element;
			return StringUtil.filterString(entry.getKey() + entry.getValue(), filterString, caseSensitive);
		}
		
		if(element instanceof String) {
			return StringUtil.filterString((String)element, filterString, caseSensitive);
		}

		return false;
	}
}
