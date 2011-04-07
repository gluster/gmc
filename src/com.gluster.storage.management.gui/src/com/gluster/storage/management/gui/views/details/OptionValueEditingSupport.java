/**
 * 
 */
package com.gluster.storage.management.gui.views.details;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.VolumeOptionInfo;

/**
 * Editing support for the "value" column in volume options table viewer.
 */
public class OptionValueEditingSupport extends EditingSupport {
	private CellEditor cellEditor;
	private Volume volume;
	private List<VolumeOptionInfo> defaults = GlusterDataModelManager.getInstance().getVolumeOptionsDefaults();

	public OptionValueEditingSupport(ColumnViewer viewer, Volume volume) {
		super(viewer);
		this.volume = volume;
		this.cellEditor = new TextCellEditor((Composite) viewer.getControl());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(final Object element, final Object value) {
		final Entry<String, String> entry = (Entry<String, String>) element;

		// It is not allowed to change value to empty string
		if(((String)value).isEmpty()) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Set Volume Option",
					"Option value can't be empty! Please enter a valid value.");
			cellEditor.setFocus();
			return;
		}

		if (entry.getValue().equals(value)) {
			// value is same as that present in the model. return without doing anything.
			return;
		}
		
		// value has changed. set volume option at back-end and update model accordingly
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {

			@Override
			public void run() {
				VolumesClient client = new VolumesClient(GlusterDataModelManager.getInstance().getSecurityToken());
				Status status = client.setVolumeOption(volume.getName(), entry.getKey(), (String) value);
				if (status.isSuccess()) {
					volume.setOption(entry.getKey(), (String) value);
				} else {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Set Volume Option",
							status.getMessage());
				}
				getViewer().update(entry, null);
			}
		});
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

	@SuppressWarnings("unchecked")
	@Override
	protected Object getValue(Object element) {
		Entry<String, String> entry = (Entry<String, String>) element;
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
