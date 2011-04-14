/**
 * 
 */
package com.gluster.storage.management.gui.views.details;

import java.util.ArrayList;
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
			if(!volumeOptions.containsKey(optionName)) {
				keys.add(optionName);
			}
		}
		return keys.toArray(new String[0]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(final Object element, final Object value) {
		Integer newValue = (Integer)value;
		String newKey = allowedKeys[newValue];
		
		if (((Entry<String, String>)element).getKey().equals(newKey)) {
			// selected value is same as old one.
			return;
		}

		// value has changed. set volume option at back-end and update model accordingly
		volume.getOptions().remove("");
		volume.setOption(newKey, "");
		getViewer().refresh();
	}

	@Override
	protected Object getValue(Object element) {
		return cellEditor.getValue();
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		allowedKeys = getAllowedKeys();
		cellEditor = new ComboBoxCellEditor((Composite) viewer.getControl(), allowedKeys);
		return cellEditor;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean canEdit(Object element) {
		Entry<String, String> entry = (Entry<String, String>)element;
		return (entry.getKey().isEmpty() || entry.getValue().isEmpty()); 
	}
}
