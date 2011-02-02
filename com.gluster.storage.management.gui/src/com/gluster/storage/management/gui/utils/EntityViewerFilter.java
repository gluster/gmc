package com.gluster.storage.management.gui.utils;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.gluster.storage.management.core.model.Filterable;
import com.gluster.storage.management.core.utils.StringUtils;

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
			return StringUtils.filterString(entry.getKey() + entry.getValue(), filterString, caseSensitive);
		}
		
		if(element instanceof String) {
			return StringUtils.filterString((String)element, filterString, caseSensitive);
		}

		return false;
	}
}
