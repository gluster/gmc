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
package org.gluster.storage.management.console.validators;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Control;

public class StringRequiredValidator implements IValidator {
	protected final String errorText;
	protected final ControlDecoration controlDecoration;
	protected final Control linkedControl;

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
		if(linkedControl != null) {
			linkedControl.setEnabled(true);
		}
		controlDecoration.hide();
		return Status.OK_STATUS;
	}
}
