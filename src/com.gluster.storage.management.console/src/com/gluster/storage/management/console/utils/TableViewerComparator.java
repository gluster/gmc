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
package com.gluster.storage.management.console.utils;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * Comparator for sorting contents of a table viewer
 */
public class TableViewerComparator extends ViewerComparator {
	private int column = -1;
	private static final int ASCENDING = 0;
	private static final int DESCENDING = 1;
	private static final int NONE = -1;
	private int direction = DESCENDING;

	public TableViewerComparator() {
		this(NONE);
	}

	public TableViewerComparator(int direction) {
		this.direction = direction;
	}

	public int getDirection() {
		return direction == DESCENDING ? SWT.DOWN : (direction == ASCENDING ? SWT.UP : SWT.NONE);
	}

	public void setColumn(int column) {
		if (column == this.column) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// first column selection or new column; do an ascending sort
			direction = ASCENDING;
			this.column = column;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if(direction == NONE) {
			// no sorting
			return 0;
		}
		
		int result = super.compare(viewer, e1, e2);
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			result = -result;
		}
		
		return result;
	}
}
