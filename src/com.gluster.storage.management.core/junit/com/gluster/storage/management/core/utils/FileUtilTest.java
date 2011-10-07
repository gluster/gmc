package com.gluster.storage.management.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;


/**
 * The class <code>FileUtilTest</code> contains tests for the class <code>{@link FileUtil}</code>.
 *
 * @generatedBy CodePro at 9/29/11 2:39 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class FileUtilTest {
	

	private String filePath = FileUtil.getTempDirName() + "/test.txt";
	

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
	
	private boolean createEmptyFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return true;
		}
		
		try {
			return file.createNewFile();
		} catch (IOException e) {
			return false;
		}
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
	@Test(expected=GlusterRuntimeException.class)
	public void testCreateTextFile_1()
		throws Exception {
		String fileName = "";
		String contents = "";
		FileUtil.createTextFile(fileName, contents);
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

		// while running on linux
		assertEquals("/tmp", result);
	}

	/**
	 * Run the byte[] readFileAsByteArray(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */

	@Test(expected=GlusterRuntimeException.class)
	public void testReadFileAsByteArray_1()
		throws Exception {
		File file = new File("");
		
		byte[] result = FileUtil.readFileAsByteArray(file);
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

	@Test(expected=GlusterRuntimeException.class)
	public void testReadFileAsString_1()
		throws Exception {
		File file = new File("");
		
		String result = FileUtil.readFileAsString(file);
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
		
		//Delete empty directories recursively
		File fileOrDir = new File(FileUtil.getTempDirName() + "/rd");
		
		FileUtil.recursiveDelete(fileOrDir);
		assertTrue(!fileOrDir.exists());
	}

	/**
	 * Run the void recursiveDelete(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */

	@Test(expected=GlusterRuntimeException.class)
	public void testRecursiveDelete_2()
		throws Exception {
		//Delete directories recursively (with some files) 
		File fileOrDir = new File(FileUtil.getTempDirName() + "/rdx");

		FileUtil.recursiveDelete(fileOrDir);
		assertTrue(!fileOrDir.exists());
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
		File fileOrDir = new File(FileUtil.getTempDirName() + "/rd");
		FileUtil.recursiveDelete(fileOrDir);
		assertTrue(!fileOrDir.exists());
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
		File file = new File(FileUtil.getTempDirName() + "/rd/b/bc/mydoc.txt");
		assertTrue(!file.exists());
		
		file = new File(FileUtil.getTempDirName() + "/rd//b/bc");
		assertTrue(!file.exists());
	}

	/**
	 * Run the void recursiveDelete(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test(expected=GlusterRuntimeException.class)
	public void testRecursiveDelete_5()
		throws Exception {
		File fileOrDir = new File(FileUtil.getTempDirName() + "/rd/*"); //Wild cards 
		FileUtil.recursiveDelete(fileOrDir);
	}
	
	
	/**
	 * Run the void recursiveDelete(File) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */
	@Test(expected=GlusterRuntimeException.class)
	public void testRecursiveDelete_6()
		throws Exception {
		File fileOrDir = new File(FileUtil.getTempDirName() + "/abcxyz");
		FileUtil.recursiveDelete(fileOrDir);
	}
	
	/**
	 * Run the void renameFile(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/29/11 2:39 PM
	 */

	@Test(expected=GlusterRuntimeException.class)
	public void testRenameFile_1()
		throws Exception {
		String fromPath = FileUtil.getTempDirName() + "/test.txt";
		new File(fromPath).createNewFile();
		String toPath = "~/abc.txt"; // Relative path
		
		FileUtil.renameFile(fromPath, toPath);
		assertTrue(!new File(fromPath).exists());
		assertTrue(new File(toPath).exists());
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
		String fromPath = FileUtil.getTempDirName() + "/test.txt";
		new File(fromPath).createNewFile();
		
		String toPath = FileUtil.getTempDirName() + "/abc.txt"; // Absolute path example
		FileUtil.renameFile(fromPath, toPath);
		assertTrue(!new File(fromPath).exists());
		assertTrue(new File(toPath).exists());
	}
	
	@Test
	public void testRenameFile_3()
		throws Exception {
		String fromPath = FileUtil.getTempDirName() + "/test.txt";
		new File(fromPath).createNewFile();
		
		String toPath = FileUtil.getTempDirName() + "/renamefile.txt"; 
		FileUtil.renameFile(fromPath, toPath);
		assertTrue(!new File(fromPath).exists());
		assertTrue(new File(toPath).exists());
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

		// testReadFileAsByteArray_2()
		if (!writeToFile(filePath, "Welcome to Gluster Storage Management console.")) {
			fail("Setup: Text file creation error!");
		}

		// testRecursiveDelete_1()
		if (!createNestedDir(FileUtil.getTempDirName() + "/rd/b/c/d")) {
			createEmptyFile(FileUtil.getTempDirName() + "/rd/b/mydoc.txt");
			createEmptyFile(FileUtil.getTempDirName() + "/rd/b/songs.mp3");
			createEmptyFile(FileUtil.getTempDirName() + "/rd/b/mysetup.cfg");
			
			createEmptyFile(FileUtil.getTempDirName() + "/rd/b/bc/mydoc.txt");
			createEmptyFile(FileUtil.getTempDirName() + "/rd/songs.mp3");
			createEmptyFile(FileUtil.getTempDirName() + "/rd/b/bc/mysetup.cfg");
			
			createEmptyFile(FileUtil.getTempDirName() + "/rd//b/mydoc.txt");
			createEmptyFile(FileUtil.getTempDirName() + "/rd/b/bc/songs.mp3");
			
		}
		
		if (! createEmptyFile(FileUtil.getTempDirName() + "/renamefile.txt") ) {
			fail("Failed to create file [/renamefile.txt]");
		}
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
		File file = new File(filePath);
		file.delete();
		file = new File(FileUtil.getTempDirName() + "/rd/b/c/d");
		if (file.exists()) {
			file.delete();
		}
		
		file = new File(FileUtil.getTempDirName() + "/rd/b/c");
		if (file.exists()) {
			file.delete();
		}
		
		file = new File(FileUtil.getTempDirName() + "/rd/b/mydoc.txt");
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileUtil.getTempDirName() + "/rd/b/songs.mp3");
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileUtil.getTempDirName() + "/rd/b/mysetup.cfg");
		if (file.exists()) {
			file.delete();
		}
		
		file = new File(FileUtil.getTempDirName() + "/rd/b/bc/mydoc.txt");
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileUtil.getTempDirName() + "/rd/b/bc/mysetup.cfg");
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileUtil.getTempDirName() + "/rd/b/bc/songs.mp3");
		if (file.exists()) {
			file.delete();
		}
		
		file = new File(FileUtil.getTempDirName() + "/rd/b/bc");
		if (file.exists()) {
			file.delete();
		}
		
		file = new File(FileUtil.getTempDirName() + "/rd/b");
		if (file.exists()) {
			file.delete();
		}
			
		file = new File(FileUtil.getTempDirName() + "/rd");
		if (file.exists()) {
			file.delete();
		}
			
		file = new File(FileUtil.getTempDirName() + "/abc.txt");
		if (file.exists()) {
			file.delete();
		}
		file = new File("~/abc.txt");
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileUtil.getTempDirName() + "/test.txt");
		if (file.exists()) {
			file.delete();
		}
		file = new File(FileUtil.getTempDirName() + "/renamefile.txt");
		if (file.exists()) {
			file.delete();
		}
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