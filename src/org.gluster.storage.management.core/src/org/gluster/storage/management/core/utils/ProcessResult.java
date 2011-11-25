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
package org.gluster.storage.management.core.utils;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the result of a command execution in a separate process. Consists of the "exit status" of the process and
 * output from the process. The output includes stdout as well as stderr streams
 */
@XmlRootElement
public class ProcessResult {

	public static final int SUCCESS = 0;
	public static final int FAILURE = 1;
	private int exitValue;
	private String output;

	// Required for JAXB de-serialization
	public ProcessResult() {

	}

	public ProcessResult(int exitValue, String output) {
		this.exitValue = exitValue;
		this.output = output;
	}

	public int getExitValue() {
		return exitValue;
	}

	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public boolean isSuccess() {
		return exitValue == SUCCESS;
	}
	
	@Override
	public String toString() {
		return "["+ getExitValue() + "][" + getOutput() + "]";
	}
}
