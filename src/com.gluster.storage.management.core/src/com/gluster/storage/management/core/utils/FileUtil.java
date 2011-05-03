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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

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
	
	public void createTextFile(String fileName, String contents) {
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(contents);
		} catch (Exception e) {
			throw new GlusterRuntimeException("Exception while trying to create text file [" + fileName + "]", e);
		}
	}
	
	public String getTempDirName() {
		return System.getProperty("java.io.tmpdir");
	}
	
	/**
	 * Create a new temporary directory. Use something like
	 * {@link #recursiveDelete(File)} to clean this directory up since it isn't
	 * deleted automatically
	 * @return  the new directory
	 * @throws IOException if there is an error creating the temporary directory
	 */
	public File createTempDir()
	{
	    final File sysTempDir = new File(getTempDirName());
	    File newTempDir;
	    final int maxAttempts = 9;
	    int attemptCount = 0;
	    do
	    {
	        attemptCount++;
	        if(attemptCount > maxAttempts)
	        {
	            throw new GlusterRuntimeException(
	                    "The highly improbable has occurred! Failed to " +
	                    "create a unique temporary directory after " +
	                    maxAttempts + " attempts.");
	        }
	        String dirName = UUID.randomUUID().toString();
	        newTempDir = new File(sysTempDir, dirName);
	    } while(newTempDir.exists());

	    if(newTempDir.mkdirs())
	    {
	        return newTempDir;
	    }
	    else
	    {
	        throw new GlusterRuntimeException(
	                "Failed to create temp dir named " +
	                newTempDir.getAbsolutePath());
	    }
	}

	/**
	 * Recursively delete file or directory
	 * 
	 * @param fileOrDir
	 *            the file or dir to delete
	 * @return true if all files are successfully deleted
	 */
	public boolean recursiveDelete(File fileOrDir)
	{
	    if(fileOrDir.isDirectory())
	    {
	        // recursively delete contents
	        for(File innerFile: fileOrDir.listFiles())
	        {
	            if(!recursiveDelete(innerFile))
	            {
	                return false;
	            }
	        }
	    }

	    return fileOrDir.delete();
	}
}