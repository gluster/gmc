package org.gluster.storage.management.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gluster.storage.management.core.utils.StringUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * The class <code>StringUtilTest</code> contains tests for the class <code>{@link StringUtil}</code>.
 *
 * @generatedBy CodePro at 21/9/11 4:53 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class StringUtilTest {
	public enum Season { WINTER, SPRING, SUMMER, FALL };
	/**
	 * Run the String collectionToString(Collection<? extends Object>,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testCollectionToString_1()
		throws Exception {
		List<String> string = new ArrayList<String>();
		string.add("test string");
		String delimiter = "";

		String result = StringUtil.collectionToString(string, delimiter);
		assertEquals("test string", result);
	}

	/**
	 * Run the String collectionToString(Collection<? extends Object>,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testCollectionToString_2()
		throws Exception {
		List<String> string = new ArrayList<String>();
		string.add("test string");
		string.add("welcome to world");
		String delimiter = "::";

		String result = StringUtil.collectionToString(string, delimiter);

		assertEquals("test string::welcome to world", result);
	}

	/**
	 * Run the String collectionToString(Collection<? extends Object>,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testCollectionToString_3()
		throws Exception {
		List<String> string = new ArrayList<String>();
		string.add("test ## string");
		string.add("java world");
		String delimiter = "##";

		String result = StringUtil.collectionToString(string, delimiter);
		assertEquals("test ## string##java world", result);
	}
	
	/**
	 * Run the String collectionToString(Collection<? extends Object>,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testCollectionToString_4()
		throws Exception {
		List<String> string = new ArrayList<String>();
		String delimiter = "";

		String result = StringUtil.collectionToString(string, delimiter);
		assertEquals("", result);
	}

	/**
	 * Run the List<String> enumToArray(T[]) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testEnumToArray_1()
		throws Exception {

		List<String> result = StringUtil.enumToArray(Season.values());

		assertNotNull(result);
		assertEquals(4, result.size());
	}

	/**
	 * Run the List<String> extractList(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testExtractList_1()
		throws Exception {
		String input = "This is test message";
		String delim = " ";

		List<String> result = StringUtil.extractList(input, delim);

		assertNotNull(result);
		assertEquals(4, result.size());
	}

	/**
	 * Run the List<String> extractList(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testExtractList_2()
		throws Exception {
		String input = "welcome#to#java#world";
		String delim = "#";

		List<String> result = StringUtil.extractList(input, delim);

		assertNotNull(result);
		assertEquals(4, result.size());
	}

	/**
	 * Run the List<String> extractList(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testExtractList_3()
		throws Exception {
		String input = "list$to%string";
		String delim = "%";

		List<String> result = StringUtil.extractList(input, delim);

		assertNotNull(result);
		assertEquals(2, result.size());
	}

	/**
	 * Run the Map<String, String> extractMap(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testExtractMap_1()
		throws Exception {
		String input = "k1=v1,k2=v2,k3=v3";
		String majorDelim = ",";
		String minorDelim = "=";

		Map<String, String> result = StringUtil.extractMap(input, majorDelim, minorDelim);

		// add additional test code here
		assertNotNull(result);
		assertEquals(3, result.size());
	}

	/**
	 * Run the Map<String, String> extractMap(String,String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testExtractMap_2()
		throws Exception {
		String input = "k1=>v1&k2=>v2&k3=>v3";
		String majorDelim = "&";
		String minorDelim = "=>";

		Map<String, String> result = StringUtil.extractMap(input, majorDelim, minorDelim);

		// add additional test code here
		assertNotNull(result);
		assertEquals(3, result.size());
	}

	/**
	 * Run the boolean filterString(String,String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testFilterString_1()
		throws Exception {
		String sourceString = "This is java program";
		String filterString = "Java";
		boolean caseSensitive = true;

		boolean result = StringUtil.filterString(sourceString, filterString, caseSensitive);

		assertEquals(false, result);
	}

	/**
	 * Run the boolean filterString(String,String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testFilterString_2()
		throws Exception {
		String sourceString = "This is java program";
		String filterString = "Java";
		boolean caseSensitive = false;

		boolean result = StringUtil.filterString(sourceString, filterString, caseSensitive);

		assertEquals(true, result);
	}

	/**
	 * Run the String removeSpaces(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	@Test
	public void testRemoveSpaces_1()
		throws Exception {
		String str = "this is   test string";

		String result = StringUtil.removeSpaces(str);

		// add additional test code here
		assertEquals("thisisteststring", result);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 21/9/11 4:53 PM
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
	 * @generatedBy CodePro at 21/9/11 4:53 PM
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
	 * @generatedBy CodePro at 21/9/11 4:53 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(StringUtilTest.class);
	}
}