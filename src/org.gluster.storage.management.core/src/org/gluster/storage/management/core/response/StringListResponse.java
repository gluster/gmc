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
package org.gluster.storage.management.core.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.gluster.storage.management.core.model.Status;


@XmlRootElement(name = "response")
public class StringListResponse extends AbstractResponse {
	private List<String> data = new ArrayList<String>();
	
	public StringListResponse() {
	}
	
	public StringListResponse(List<String> data) {
		this.data = data;
		setStatus(Status.STATUS_SUCCESS);
	}
	
	@Override
	@XmlElementWrapper(name = "list")
	@XmlElement(name = "value", type = String.class)
	public List<String> getData() {
		return data;
	}
}