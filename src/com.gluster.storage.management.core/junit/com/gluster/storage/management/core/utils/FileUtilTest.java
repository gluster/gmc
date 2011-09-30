package com.gluster.storage.management.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>FileUtilTest</code> contains tests for the class <code>{@link FileUtil}</code>.
 *
 * @generatedBy CodePro at 9/29/11 2:39 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class FileUtilTest {
	
	/**
	 * To write the text into given file. 
	 *
	 * @generatedBy 
	 */
	private boolean writeToFile(String fileName, String text) {
		try {
			Writer output = null;
			File file = new File(fileName);
			output = new BufferedWriter(new FileWriter(file));
			output.write(text);
			output.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean createNestedDir(String path) {
		File file = new File(path);
		return file.mkdirs();
	}
	
	/**
	 * Run the File createTempDir() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testCreateTempDir_1()
		throws Exception {

		File result = FileUtil.createTempDir();
		
		assertNotNull(result);
		assertTrue(File.class.equals(result.getClass()));
	}

	/**
	 * Run the File createTempDir() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testCreateTempDir_2()
		throws Exception {

		File result1 = FileUtil.createTempDir();
		File result2 = FileUtil.createTempDir();

		assertNotSame(result1, result2);
	}

	
	/**
	 * Run the void createTextFile(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testCreateTextFile_1()
		throws Exception {
		String fileName = "";
		String contents = "";
		try {
			FileUtil.createTextFile(fileName, contents);
			fail("Failed to throw expception! for [] file name and [] file content in FileUtil.createTextFile()");
		} catch (Exception e) {
			//Nothing to do
		}
	}

	/**
	 * Run the String getTempDirName() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testGetTempDirName_1()
		throws Exception {

		String result = FileUtil.getTempDirName();

		// add additional test code here
		assertEquals("/tmp", result);
	}

	/**
	 * Run the byte[] readFileAsByteArray(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testReadFileAsByteArray_1()
		throws Exception {
		File file = new File("");
		try {
			byte[] result = FileUtil.readFileAsByteArray(file);
			assertNotNull(result);
			fail("Byte array read from empty file name should throw exception!");
		} catch (Exception e) {
			// Nothing to do
		}
	}

	/**
	 * Run the byte[] readFileAsByteArray(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testReadFileAsByteArray_2()
		throws Exception {
		// Setup 
		String filePath = "/tmp/test.txt";
		String text = "Welcome to Gluster Storage Management console.";
		if (!writeToFile(filePath, text) ) {
			fail("Text file creation error!");
		}
		
		File file = new File(filePath);

		byte[] result = FileUtil.readFileAsByteArray(file);
		assertNotNull(result);
		assertTrue(result instanceof byte[]);
	}

	
	/**
	 * Run the String readFileAsString(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testReadFileAsString_1()
		throws Exception {
		File file = new File("");
		try {
			String result = FileUtil.readFileAsString(file);
			assertNotNull(result);
			fail("Read from Empty file name should throw exception!");
		} catch (Exception e) {
			// Nothing to do
		}
	}
	
	
	/**
	 * Run the String readFileAsString(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testReadFileAsString_2()
		throws Exception {
		// Setup 
		String filePath = "/tmp/test.txt";
		String text = "Welcome to Gluster Storage Management console.";
		if (!writeToFile(filePath, text)) {
			fail("Text file creation error!");
		}
		
		File file = new File(filePath);
		String result = FileUtil.readFileAsString(file);

		assertNotNull(result);
		assertTrue(result instanceof String);
	}

	/**
	 * Run the void recursiveDelete(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testRecursiveDelete_1()
		throws Exception {
		
		if (!createNestedDir("/tmp/a/b/c/d")) {
			fail("Failed to create directory [/tmp/a/b/c/d]");
		}
		
		File fileOrDir = new File("/tmp/a");
		try {
			FileUtil.recursiveDelete(fileOrDir);
			assertTrue( !fileOrDir.exists() );
		} catch (Exception e) {
			// Nothing to do
		}
	}

	/**
	 * Run the void recursiveDelete(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testRecursiveDelete_2()
		throws Exception {

		if (!createNestedDir("/tmp/a/b/c/d")) {
			fail("Failed to create directory [/tmp/a/b/c/d]");
		}

		File fileOrDir = new File("/tmp/x");
		try {
			FileUtil.recursiveDelete(fileOrDir);
			assertTrue( !fileOrDir.exists() );
		} catch (Exception e) {
			// Nothing to do
		}
	}

	/**
	 * Run the void recursiveDelete(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testRecursiveDelete_3()
		throws Exception {
		//Setup
		File fileDir = new File("/tmp/a/bc");
		fileDir.mkdirs();
		fileDir.createNewFile();
		try {
			File fileOrDir = new File("/tmp/a");
			FileUtil.recursiveDelete(fileOrDir);
			assertTrue(!fileOrDir.exists());
		} catch (Exception e) {
			// Nothing to do
		}
	}

	/**
	 * Run the void recursiveDelete(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testRecursiveDelete_4()
		throws Exception {
		
		File fileDir = new File("/tmp/1/2.bc/c/123.xyz");
		fileDir.mkdirs();
		fileDir.createNewFile();
		
		File fileOrDir = new File("/tmp/1/*");
		try {
			FileUtil.recursiveDelete(fileOrDir);
			File file = new File("/tmp/1/2.bc");
			assertTrue(!file.exists());
		} catch (Exception e) {
			// fail("Unable to delete the file/folders recursively.");
		}
	}

	/**
	 * Run the void renameFile(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testRenameFile_1()
		throws Exception {
		String fromPath = "/tmp/test.txt";
		String toPath = "~/abc.txt";
		new File(fromPath).createNewFile();
		
		try {
			FileUtil.renameFile(fromPath, toPath);
			assertTrue(!new File(fromPath).exists());
			assertTrue(new File(toPath).exists());
		} catch (Exception e) {
			// Nothing to do 
		}
	}

	/**
	 * Run the void renameFile(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test
	public void testRenameFile_2()
		throws Exception {
		String fromPath = "/tmp/test.txt";
		String toPath = "/tmp/abc.txt";
		new File(fromPath).createNewFile();

		try {
			FileUtil.renameFile(fromPath, toPath);
			assertTrue(!new File(fromPath).exists());
			assertTrue(new File(toPath).exists());
		} catch (Exception e) {
			// Nothing to do 
		}
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		// add additional set up code here
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@After
	public void tearDown()
		throws Exception {
		// Add additional tear down code here
	}

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(FileUtilTest.class);
	}
}