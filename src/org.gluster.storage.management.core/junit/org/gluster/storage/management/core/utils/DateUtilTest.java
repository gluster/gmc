package org.gluster.storage.management.core.utils;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.utils.DateUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * The class <code>DateUtilTest</code> contains tests for the class <code>{@link DateUtil}</code>.
 *
 * @generatedBy CodePro at 9/27/11 12:31 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class DateUtilTest {

	private Date date = DateUtil.getDate(2011, 9, 29, 11, 17, 38, 10);

	/**
	 * Run the String dateToString(Date) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test
	public void testDateToString_1()
		throws Exception {
		String result = DateUtil.dateToString(date);

		assertEquals("09/29/2011 11:17:38", result);
	}

	/**
	 * Run the String dateToString(Date,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test
	public void testDateToString_2()
		throws Exception {
		String dateFormat = "";

		String result = DateUtil.dateToString(date, dateFormat);
		assertEquals("", result);
	}
	
	/**
	 * Run the String dateToString(Date,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test
	public void testDateToString_3()
		throws Exception {
		String dateFormat = CoreConstants.PURE_DATE_FORMAT;

		String result = DateUtil.dateToString(date, dateFormat);
		assertEquals("09/29/2011", result);
	}

	/**
	 * Run the String formatDate(Date) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test
	public void testFormatDate_1()
		throws Exception {
		String result = DateUtil.formatDate(date);

		assertEquals("09/29/2011", result);
	}
	
	/**
	 * Run the String formatDate(Date) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test
	public void testFormatDate_2()
		throws Exception {
		Date date1 = DateUtil.getDate(1965, 1, 12, 0, 0, 0, 0);
		String result = DateUtil.formatDate(date1);
		assertEquals("01/12/1965", result);
	}

	/**
	 * Run the String formatTime(Date) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test
	public void testFormatTime_1()
		throws Exception {
		String result = DateUtil.formatTime(date);

		// add additional test code here
		assertEquals("11:17:38.010", result);
	}

	/**
	 * Run the Date stringToDate(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test(expected=GlusterRuntimeException.class)
	public void testStringToDate_1() 
		throws Exception {
		DateUtil.stringToDate("");
	}
	
	/**
	 * Run the Date stringToDate(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test(expected=GlusterRuntimeException.class)
	public void testStringToDate_2() 
		throws Exception {
		String testDate = "09/29/2011";
		DateUtil.stringToDate(testDate);
	}

	/**
	 * Run the Date stringToDate(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test
	public void testStringToDate_3()
		throws Exception {
		String dateFormat = "MM/dd/yyyy";
		String input = "09/29/2011"; // MM/dd/yyyy HH:mm:ss
		Date expectedDate = DateUtil.getDate(2011, 9, 29, 0, 0, 0, 0);
		
		Date result = DateUtil.stringToDate(input, dateFormat);
		assertEquals(expectedDate, result);
	}

	/**
	 * Run the Date stringToDate(String,String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	@Test(expected = GlusterRuntimeException.class)
	public void testStringToDate_4()
		throws Exception {
			String inputDate = "";
			String dateFormat = "";

			DateUtil.stringToDate(inputDate, dateFormat);
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 9/27/11 12:31 PM
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
	 * @generatedBy CodePro at 9/27/11 12:31 PM
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
	 * @generatedBy CodePro at 9/27/11 12:31 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(DateUtilTest.class);
	}
}