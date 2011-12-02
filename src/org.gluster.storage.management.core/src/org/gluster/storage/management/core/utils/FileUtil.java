/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Console is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;


public class FileUtil {
	public static String readFileAsString(File file) {
		try {
			return new String(readFileAsByteArray(file), CoreConstants.ENCODING_UTF8);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Could not read file [" + file + "]", e);
		}
	}

	public static byte[] readFileAsByteArray(File file) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] data = new byte[fileInputStream.available()];
			fileInputStream.read(data);
			fileInputStream.close();
			return data;
		} catch (Exception e) {
			throw new GlusterRuntimeException("Exception while reading file [" + file.getName() + "]: "
					+ e.getMessage(), e);
		}
	}
	
	public static void createTextFile(String fileName, String contents) {
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(contents);
			writer.close();
		} catch (Exception e) {
			throw new GlusterRuntimeException("Exception while trying to create text file [" + fileName + "]", e);
		}
	}
	
	public static String getTempDirName() {
		return System.getProperty("java.io.tmpdir");
	}
	
	/**
	 * Create a new temporary directory. Use something like
	 * {@link #recursiveDelete(File)} to clean this directory up since it isn't
	 * deleted automatically
	 * @return  the new directory
	 * @throws IOException if there is an error creating the temporary directory
	 */
	public static File createTempDir()
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
	public static void recursiveDelete(File fileOrDir)
	{
	    if(fileOrDir.isDirectory())
	    {
	        // recursively delete contents
	        for(File innerFile: fileOrDir.listFiles())
	        {
	        	recursiveDelete(innerFile);
	        }
	    }

	    if(!fileOrDir.delete()) {
			throw new GlusterRuntimeException("Couldn't delete file/directory [" + fileOrDir + "]");
		}
	}

	public static void renameFile(String fromPath, String toPath) {
		File fromFile = new File(fromPath);
		File toFile = new File(toPath);

		if(!fromFile.renameTo(toFile)) {
			throw new GlusterRuntimeException("Couldn't rename [" + fromFile + "] to [" + toFile + "]");
		}
	}
}
