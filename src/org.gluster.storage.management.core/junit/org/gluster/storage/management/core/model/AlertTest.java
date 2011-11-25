package org.gluster.storage.management.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.gluster.storage.management.core.model.Alert;
import org.gluster.storage.management.core.model.Alert.ALERT_TYPES;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * The class <code>AlertTest</code> contains tests for the class <code>{@link Alert}</code>.
 *
 * @generatedBy CodePro at 10/17/11 3:32 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class AlertTest {

	/**
	 * Run the void copyFrom(Alert) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/17/11 3:32 PM
	 */
	@Test
	public void testCopyFrom_1()
		throws Exception {
		Alert fixture = new Alert(ALERT_TYPES.DISK_USAGE_ALERT, "server1:sda",
				Alert.ALERT_TYPE_STR[ALERT_TYPES.DISK_USAGE_ALERT.ordinal()] + " [85% used] in disk [server1:sda]");
		Alert alert = new Alert();
		alert.copyFrom(fixture);

		assertEquals(fixture.getId(), alert.getId());
		assertEquals(fixture.getReference(), alert.getReference());
		assertEquals(fixture.getType(), alert.getType());
		assertEquals(fixture.getMessage(), alert.getMessage());
		assertNotNull(alert);
	}


	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 10/17/11 3:32 PM
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
	 * @generatedBy CodePro at 10/17/11 3:32 PM
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
	 * @generatedBy CodePro at 10/17/11 3:32 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(AlertTest.class);
	}
}