/**
 * 
 */
package com.gluster.storage.management.console.views.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.gluster.storage.management.console.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.VolumeOption;
import com.gluster.storage.management.core.model.VolumeOptionInfo;
import com.gluster.storage.management.core.model.VolumeOptions;

/**
 * Editing support for the "value" column in volume options table viewer.
 */
public class OptionKeyEditingSupport extends EditingSupport {
	private CellEditor cellEditor;
	private Volume volume;
	private List<VolumeOptionInfo> defaults = GlusterDataModelManager.getInstance().getVolumeOptionsDefaults();
	private String[] allowedKeys;
	private ColumnViewer viewer;

	public OptionKeyEditingSupport(ColumnViewer viewer, Volume volume) {
		super(viewer);
		this.volume = volume;
		this.viewer = viewer;
	}

	/**
	 * @return array of option keys that are not already set on the volume
	 */
	private String[] getAllowedKeys() {
		ArrayList<String> keys = new ArrayList<String>();
		VolumeOptions volumeOptions = volume.getOptions();
		for(VolumeOptionInfo optionInfo : defaults) {
			String optionName = optionInfo.getName();
			if(!volumeOptions.containsKey(optionName) || volumeOptions.get(optionName).isEmpty()) {
				// key not set => available for setting
				// value not set => this is the row being edited
				keys.add(optionName);
			}
		}
		return keys.toArray(new String[0]);
	}
	
	@Override
	protected void setValue(final Object element, final Object value) {
		VolumeOption oldEntry = (VolumeOption)element;
		Integer newValue = (Integer)value;
		String newKey = allowedKeys[newValue];
		
		if (((VolumeOption)element).getKey().equals(newKey)) {
			// selected value is same as old one.
			return;
		}

		// value has changed. set new value and refresh the viewer.
		volume.getOptions().remove(oldEntry.getKey());
		volume.setOption(newKey, "");
		getViewer().refresh();
	}

	@Override
	protected Object getValue(Object element) {
		VolumeOption entryBeingAdded = getEntryBeingAdded();
		if(entryBeingAdded == null) {
			return cellEditor.getValue();
		}
		
		if(entryBeingAdded.getKey().isEmpty()) {
			// editing just about to start. set first element as default.
			return 0;
		}
		
		return getIndexOfEntry(entryBeingAdded);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		allowedKeys = getAllowedKeys();
		cellEditor = new ComboBoxCellEditor((Composite) viewer.getControl(), allowedKeys, SWT.READ_ONLY);
		return cellEditor;
	}

	private int getIndexOfEntry(VolumeOption entryBeingAdded) {
		for(int index = 0; index < allowedKeys.length; index++) {
			if(allowedKeys[index].equals(entryBeingAdded.getKey())) {
				return index;
			}
		}
		return -1;
	}

	protected VolumeOption getEntryBeingAdded() {
		List<VolumeOption> options = volume.getOptions().getOptions();
		int size = options.size();
		String lastValue = options.get(size - 1).getValue();
		if(lastValue == null || lastValue.isEmpty()) {
			// it's the LAST entry, and it's value is empty.
			// means this is a new row being added in the table viewer.
			return options.get(size - 1);
		}
		return null;
	}

	@Override
	protected boolean canEdit(Object element) {
		VolumeOption entry = (VolumeOption)element;
		return (entry.getKey().isEmpty() || entry.getValue().isEmpty()); 
	}
}
