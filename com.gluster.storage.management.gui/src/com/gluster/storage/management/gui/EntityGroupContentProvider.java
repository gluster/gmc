package com.gluster.storage.management.gui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.gluster.storage.management.core.model.EntityGroup;

public class EntityGroupContentProvider<T> implements
		IStructuredContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof EntityGroup) {
			return ((EntityGroup) inputElement).getChildren().toArray();
		}
		return null;
	}
}
