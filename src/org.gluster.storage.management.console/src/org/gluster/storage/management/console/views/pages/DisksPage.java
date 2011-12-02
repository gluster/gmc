/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.console.views.pages;

import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchSite;
import org.gluster.storage.management.console.DeviceTableLabelProvider;
import org.gluster.storage.management.core.model.Disk;
import org.gluster.storage.management.core.model.Entity;


public class DisksPage extends AbstractDisksPage {

	public enum DISK_TABLE_COLUMN_INDICES {
		DISK, PARTITION, FREE_SPACE, TOTAL_SPACE, STATUS
	};

	public DisksPage(final Composite parent, int style, IWorkbenchSite site, List<Disk> disks) {
		super(parent, style, site, disks);
	}

	@Override
	protected DeviceTableLabelProvider getLabelProvider() {
		return new DeviceTableLabelProvider();
	}
	

	@Override
	protected IContentProvider getContentProvider() {
		return new DiskTreeContentProvider(disks);
	}
	
	@Override
	protected int getStatusColumnIndex() {
		return DISK_TABLE_COLUMN_INDICES.STATUS.ordinal();
	}

	@Override
	public void entityChanged(Entity entity, String[] paremeters) {
		// TODO Auto-generated method stub
		
	}

}