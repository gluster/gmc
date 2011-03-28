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
package com.gluster.storage.management.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;

public class FileUtil {
	public String readFileAsString(File file) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] data = new byte[fileInputStream.available()];
			fileInputStream.read(data);
			fileInputStream.close();
			
			return new String(data);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Could not read file [" + file + "]", e);
		}
	}
	
	public InputStream loadResource(String resourcePath) {
		return this.getClass().getClassLoader().getResourceAsStream(resourcePath);
	}
}