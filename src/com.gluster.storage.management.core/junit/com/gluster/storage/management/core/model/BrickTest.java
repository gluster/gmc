package com.gluster.storage.management.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gluster.storage.management.core.model.Brick.BRICK_STATUS;

/**
 * The class <code>BrickTest</code> contains tests for the class <code>{@link Brick}</code>.
 *
 * @generatedBy CodePro at 10/17/11 4:39 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class BrickTest {
	/**
	 * Run the void copyFrom(Brick) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testCopyFrom_1()
		throws Exception {
		Brick fixture = new Brick("Server1", BRICK_STATUS.ONLINE, "/sda1/songs");
		Brick newBrick = new Brick();
		newBrick.copyFrom(fixture);

		assertEquals(fixture.getServerName(), newBrick.getServerName());
		assertEquals(fixture.getBrickDirectory(), newBrick.getBrickDirectory());
		assertEquals(fixture.getStatus(), newBrick.getStatus());
		assertEquals(fixture.getQualifiedName(), newBrick.getQualifiedName());
	}

	/**
	 * Run the boolean equals(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testEquals_1()
		throws Exception {
		Brick fixture = new Brick("Server1", BRICK_STATUS.ONLINE, "/sda1/songs");
		Brick newBrick = new Brick();
		newBrick.copyFrom(fixture);

		boolean result = fixture.equals(newBrick);
		assertTrue(result);
	}

	/**
	 * Run the boolean equals(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testEquals_2()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");
		Brick newBrick = new Brick();
		newBrick.copyFrom(fixture);

		boolean result = fixture.equals(newBrick);
		assertTrue(result);
	}

	/**
	 * Run the boolean equals(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testEquals_3()
		throws Exception {
		Brick fixture = new Brick("", BRICK_STATUS.ONLINE, "");
		Brick newBrick = new Brick();
		newBrick.copyFrom(fixture);

		boolean result = fixture.equals(newBrick);
		assertTrue(result);
	}

	
	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testFilter_1()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");

		String filterString = "Ser";
		boolean caseSensitive = true;

		boolean result = fixture.filter(filterString, caseSensitive);
		assertTrue(result);
	}

	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testFilter_2()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");

		String filterString = "ser";
		boolean caseSensitive = true;

		boolean result = fixture.filter(filterString, caseSensitive);
		assertEquals(result, false);
	}

	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testFilter_3()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");

		String filterString = "Ser";
		boolean caseSensitive = false;

		boolean result = fixture.filter(filterString, caseSensitive);
		assertTrue(result);
	}
	
	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testFilter_4()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.ONLINE, "/md1/test");

		String filterString = "ser";
		boolean caseSensitive = false;

		boolean result = fixture.filter(filterString, caseSensitive);
		assertTrue(result);
	}

	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testFilter_5()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");

		String filterString = "";
		boolean caseSensitive = false;

		boolean result = fixture.filter(filterString, caseSensitive);
		assertTrue(result);
	}
	
	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testFilter_6()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");

		String filterString = "";
		boolean caseSensitive = true;

		boolean result = fixture.filter(filterString, caseSensitive);
		assertTrue(result);
	}
	
	
	/**
	 * Run the String getQualifiedName() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testGetQualifiedName_1()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");

		String result = fixture.getQualifiedName();

		assertNotNull(result);
		assertEquals(result, "Server2:/md1/test");
	}

	/**
	 * Run the String getQualifiedName() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testGetQualifiedName_2()
		throws Exception {
		Brick fixture = new Brick("", BRICK_STATUS.OFFLINE, "");

		String result = fixture.getQualifiedName();

		assertNotNull(result);
		assertEquals(result, ":");
	}
		
	
	/**
	 * Run the String toString() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	@Test
	public void testToString_1()
		throws Exception {
		Brick fixture = new Brick("Server2", BRICK_STATUS.OFFLINE, "/md1/test");

		String result = fixture.toString();

		assertNotNull(result);
		assertEquals(result, "Server2:/md1/test");
	}

	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 10/17/11 4:39 PM
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
	 * @generatedBy CodePro at 10/17/11 4:39 PM
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
	 * @generatedBy CodePro at 10/17/11 4:39 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(BrickTest.class);
	}
}