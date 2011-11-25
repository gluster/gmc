/**
 * KeysClient.java
 *
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
 */
package org.gluster.storage.management.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.ws.rs.core.MediaType;

import org.gluster.storage.management.core.constants.RESTConstants;

import com.sun.jersey.multipart.FormDataMultiPart;

public class KeysClient extends AbstractClient {

	public KeysClient() {
		super();
	}

	@Override
	public String getResourcePath() {
		return RESTConstants.RESOURCE_KEYS;
	}

	public void exportSshKeys(String filePath) {
		downloadResource(resource, filePath);
	}

	public void importSshKeys(String keysFile) {
		FormDataMultiPart form = new FormDataMultiPart();
		try {
			form.field("file", new FileInputStream(keysFile), MediaType.TEXT_PLAIN_TYPE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		uploadResource(resource, form);
	}
}
