package com.gluster.storage.management.gui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;

import com.gluster.storage.management.client.GlusterDataModelManager;
import com.gluster.storage.management.client.VolumesClient;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.gui.utils.GUIHelper;

public class ResetVolumeOptionsAction extends AbstractActionDelegate {
	private Volume volume;
	private GlusterDataModelManager modelManager = GlusterDataModelManager.getInstance();

	@Override
	public void dispose() {
	}

	@Override
	protected void performAction(final IAction action) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				final String actionDesc = action.getDescription();

				boolean confirmed = showConfirmDialog(actionDesc,
						"Are you sure you want to reset all options of the volume [" + volume.getName() + "] ?");
				if (!confirmed) {
					return;
				}

				try {
					new VolumesClient().resetVolumeOptions(volume.getName());
					showInfoDialog(actionDesc, "Volume options for [" + volume.getName() + "] reset successfully!");
					modelManager.resetVolumeOptions(volume);
				} catch (Exception e) {
					showErrorDialog(actionDesc, "Volume options for [" + volume.getName()
							+ "] could not be reset! Error: [" + e.getMessage() + "]");
				}
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gluster.storage.management.gui.actions.AbstractActionDelegate#selectionChanged(org.eclipse.jface.action.IAction
	 * , org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		volume = GUIHelper.getInstance().getSelectedEntity(getWindow(), Volume.class);
		
		if (volume instanceof Volume) {
			action.setEnabled(volume.getOptions().size() > 0);
		} else {
			action.setEnabled(false);
		}
	}
}
