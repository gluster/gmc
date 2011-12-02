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
/**
 * 
 */
package org.gluster.storage.management.console.views.pages;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gluster.storage.management.client.VolumesClient;
import org.gluster.storage.management.console.GlusterDataModelManager;
import org.gluster.storage.management.console.utils.GUIHelper;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.VolumeOption;
import org.gluster.storage.management.core.model.VolumeOptionInfo;


/**
 * Editing support for the "value" column in volume options table viewer.
 */
public class OptionValueEditingSupport extends EditingSupport {
	private CellEditor cellEditor;
	private Volume volume;
	private List<VolumeOptionInfo> defaults = GlusterDataModelManager.getInstance().getVolumeOptionsInfo();
	private GUIHelper guiHelper = GUIHelper.getInstance();

	public OptionValueEditingSupport(ColumnViewer viewer, Volume volume) {
		super(viewer);
		this.volume = volume;
		this.cellEditor = new TextCellEditor((Composite) viewer.getControl());
	}
	
	@Override
	protected void setValue(final Object element, final Object value) {
		final VolumeOption entry = (VolumeOption)element;
		final String optionKey = entry.getKey();
		final String optionValue = (String)value;
		final String oldValue = entry.getValue();

		// It is not allowed to change value to empty string
		if(optionValue.isEmpty()) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Set Volume Option",
					"Option value can't be empty! Please enter a valid value.");
			cellEditor.setFocus();
			return;
		}

		if (oldValue.equals(optionValue)) {
			// value is same as that present in the model. return without doing anything.
			return;
		}
		
		// value has changed. set volume option at back-end and update model accordingly
		guiHelper.setStatusMessage("Setting option [" + optionKey + " = " + optionValue + "]...");
		getViewer().getControl().update();
		
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {

			@Override
			public void run() {
				VolumesClient client = new VolumesClient();
				try {
					client.setVolumeOption(volume.getName(), optionKey, optionValue);
					GlusterDataModelManager.getInstance().setVolumeOption(volume, optionKey, optionValue);
				} catch(Exception e) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Set Volume Option", e.getMessage());
				}
				getViewer().update(entry, null);
			}
		});

		guiHelper.clearStatusMessage();
		getViewer().getControl().update();
	}

	/**
	 * @param key Key whose default value is to be fetched
	 * @return Default value of the volume option with given key
	 */
	private String getDefaultValue(String key) {
		for(VolumeOptionInfo optionInfo : defaults) {
			if(optionInfo.getName().equals(key)) {
				return optionInfo.getDefaultValue();
			}
		}
		return "";
	}

	@Override
	protected Object getValue(Object element) {
		VolumeOption entry = (VolumeOption) element;
		return entry.getValue().isEmpty() ? getDefaultValue(entry.getKey()) : entry.getValue();
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}
}
