/**
 * TestFileUtil.java
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
package com.gluster.storage.management.core.utils;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFileUtil {
	private String testFileName;
	private String fileContent;
	private FileUtil fileUtil;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testFileName = "testFileUtil.txt";
		fileContent = "Testing FileUtil class.";
		fileUtil = new FileUtil();

		BufferedOutputStream outStream = new BufferedOutputStream(new FileOutputStream(testFileName));
		OutputStreamWriter writer = new OutputStreamWriter(outStream);
		writer.write(fileContent);
		writer.close();
		outStream.close();
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		File testFile = new File(testFileName);
		testFile.delete();
	}

	/**
	 * Test method for {@link com.gluster.storage.management.core.utils.FileUtil#readFileAsString(java.io.File)}.
	 */
	@Test
	public final void testReadFileAsString() {
		String readContent = fileUtil.readFileAsString(new File(testFileName));
		assertTrue("File contents expected [" + fileContent + "], actual [" + readContent + "]",
				readContent.equals(fileContent));
	}
}
