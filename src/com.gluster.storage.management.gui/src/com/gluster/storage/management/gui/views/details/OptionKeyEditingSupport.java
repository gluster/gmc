/**
 * 
 */
package com.gluster.storage.management.gui.views.details;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.VolumeOptionInfo;

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
		Map<String, String> volumeOptions = volume.getOptions();
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
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(final Object element, final Object value) {
		Entry<String, String> oldEntry = (Entry<String, String>)element;
		Integer newValue = (Integer)value;
		String newKey = allowedKeys[newValue];
		
		if (((Entry<String, String>)element).getKey().equals(newKey)) {
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
		Entry<String, String> entryBeingAdded = getEntryBeingAdded();
		if(entryBeingAdded == null) {
			return cellEditor.getValue();
		}
		
		return getIndexOfEntry(entryBeingAdded);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		allowedKeys = getAllowedKeys();
		cellEditor = new ComboBoxCellEditor((Composite) viewer.getControl(), allowedKeys);
		return cellEditor;
	}

	private int getIndexOfEntry(Entry<String, String> entryBeingAdded) {
		for(int index = 0; index < allowedKeys.length; index++) {
			if(allowedKeys[index].equals(entryBeingAdded.getKey())) {
				return index;
			}
		}
		return -1;
	}

	private Entry<String, String> getEntryBeingAdded() {
		Entry<String, String> entryBeingAdded = null;
		Iterator<Entry<String, String>> iter = volume.getOptions().entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, String> nextEntry = iter.next();
			if(!iter.hasNext() && nextEntry.getValue().isEmpty()) {
				// it's the LAST entry, and it's value is empty.
				// means this is a new row being added in the table viewer.
				entryBeingAdded = nextEntry;
			}
		}
		return entryBeingAdded;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean canEdit(Object element) {
		Entry<String, String> entry = (Entry<String, String>)element;
		return (entry.getKey().isEmpty() || entry.getValue().isEmpty()); 
	}
}
