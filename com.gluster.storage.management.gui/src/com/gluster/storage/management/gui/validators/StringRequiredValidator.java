package com.gluster.storage.management.gui.validators;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Control;

public class StringRequiredValidator implements IValidator {
	private final String errorText;
	private final ControlDecoration controlDecoration;
	private final Control linkedControl;

	public StringRequiredValidator(String errorText, ControlDecoration controlDecoration, Control linkedControl) {
		super();
		this.errorText = errorText;
		this.controlDecoration = controlDecoration;
		this.linkedControl = linkedControl;
	}

	public StringRequiredValidator(String errorText, ControlDecoration controlDecoration) {
		this(errorText, controlDecoration, null);
	}

	public IStatus validate(Object value) {
		if (value instanceof String) {
			if (((String) value).isEmpty()) {
				controlDecoration.setDescriptionText(errorText);
				controlDecoration.show();
				if (linkedControl != null) {
					linkedControl.setEnabled(false);
				}
				return ValidationStatus.error(errorText);
			}
		}
		linkedControl.setEnabled(true);
		controlDecoration.hide();
		return Status.OK_STATUS;
	}
}
