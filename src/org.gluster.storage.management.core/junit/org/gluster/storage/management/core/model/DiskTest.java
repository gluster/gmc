package org.gluster.storage.management.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.gluster.storage.management.core.model.Device;
import org.gluster.storage.management.core.model.Disk;
import org.gluster.storage.management.core.model.Partition;
import org.gluster.storage.management.core.model.Server;
import org.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import org.gluster.storage.management.core.model.Device.DEVICE_TYPE;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * The class <code>DiskTest</code> contains tests for the class <code>{@link Disk}</code>.
 *
 * @generatedBy CodePro at 10/19/11 6:26 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class DiskTest {
	private Disk disk;
	
	/**
	 * Run the Disk() constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testDisk_1()
		throws Exception {

		// add additional test code here
		assertNotNull(disk);
		assertEquals(new Double(5000.0), disk.getSpace());
		assertEquals("Hitachi HTS72323 ATA", disk.getDescription());
		assertEquals(true, disk.isReady());
		assertEquals(null, disk.getDiskInterface());
		assertEquals(null, disk.getRaidDisks());
		assertEquals(false, disk.hasPartitions());
		assertEquals(new Double(3000.0), disk.getSpaceInUse());
		assertEquals(DEVICE_TYPE.DATA, disk.getType());
		assertEquals(new Double(2000.0), disk.getFreeSpace());
		assertEquals(DEVICE_STATUS.INITIALIZED, disk.getStatus());
		assertEquals("Server1:sda1", disk.getQualifiedName());
		assertEquals(true, disk.isInitialized());
		assertEquals(false, disk.hasErrors());
		assertEquals("Server1", disk.getServerName());
		assertEquals("Available", disk.getStatusStr());
		assertEquals(false, disk.isUninitialized());
		assertEquals("/md0/sda1", disk.getMountPoint());
		assertEquals("ext4", disk.getFsType());
		assertEquals("3.2.3", disk.getFsVersion());
		assertEquals("sda1", disk.toString());
		assertEquals("sda1", disk.getName());
		assertTrue(disk.getParent() instanceof Server);
	}

	/**
	 * Run the Disk(Server,String,String,Double,Double,DEVICE_STATUS) constructor test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testDisk_2()
		throws Exception {
		Server server = new Server();
		String name = "";
		String mountPoint = "";
		Double space = new Double(1.0);
		Double spaceInUse = new Double(1.0);
		Device.DEVICE_STATUS status = Device.DEVICE_STATUS.INITIALIZED;

		Disk newDisk = new Disk(server, name, mountPoint, space, spaceInUse, status);

		// add additional test code here
		assertNotNull(newDisk);
		assertEquals(new Double(1.0), newDisk.getSpace());
		assertEquals(null, newDisk.getDescription());
		assertEquals(false, newDisk.isReady());
		assertEquals(null, newDisk.getDiskInterface());
		assertEquals(null, newDisk.getRaidDisks());
		assertEquals(false, newDisk.hasPartitions());
		assertEquals(new Double(1.0), newDisk.getSpaceInUse());
		assertEquals(null, newDisk.getType());
		assertEquals(new Double(0.0), newDisk.getFreeSpace());
		assertEquals("null:", newDisk.getQualifiedName());
		assertEquals(true, newDisk.isInitialized());
		assertEquals(false, newDisk.hasErrors());
		assertEquals(null, newDisk.getServerName());
		assertEquals("Initialized", newDisk.getStatusStr());
		assertEquals(false, newDisk.isUninitialized());
		assertEquals("", newDisk.getMountPoint());
		assertEquals(null, newDisk.getFsType());
		assertEquals(null, newDisk.getFsVersion());
		assertEquals("", newDisk.toString());
		assertEquals("", newDisk.getName());
	}

	/**
	 * Run the void copyFrom(Disk) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testCopyFrom_1()
		throws Exception {
		Disk newDisk = new Disk();
		newDisk.copyFrom(disk);
		
		assertEquals(newDisk.getSpace(), disk.getSpace());
		assertEquals(newDisk.getDescription(), disk.getDescription());
		assertEquals(newDisk.isReady(), disk.isReady());
		assertEquals(newDisk.getDiskInterface(), disk.getDiskInterface());
		assertEquals(newDisk.getRaidDisks(), disk.getRaidDisks());
		assertEquals(newDisk.hasPartitions(), disk.hasPartitions());
		assertEquals(newDisk.getSpaceInUse(), disk.getSpaceInUse());
		assertEquals(newDisk.getType(), disk.getType());
		assertEquals(newDisk.getFreeSpace(), disk.getFreeSpace());
		assertEquals(newDisk.getStatus(), disk.getStatus());
		assertEquals(newDisk.getQualifiedName(), disk.getQualifiedName());
		assertEquals(newDisk.isInitialized(), disk.isInitialized());
		assertEquals(newDisk.hasErrors(), disk.hasErrors());
		assertEquals(newDisk.getServerName(), disk.getServerName());
		assertEquals(newDisk.getStatusStr(), disk.getStatusStr());
		assertEquals(newDisk.isUninitialized(), disk.isUninitialized());
		assertEquals(newDisk.getMountPoint(), disk.getMountPoint());
		assertEquals(newDisk.getFsType(), disk.getFsType());
		assertEquals(newDisk.getFsVersion(), disk.getFsVersion());
		assertEquals(newDisk.toString(), disk.toString());
		assertEquals(newDisk.getName(), disk.getName());
		assertEquals(newDisk.getParent(), disk.getParent());
	}

	/**
	 * Run the boolean equals(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testEquals_1()
		throws Exception {
		
		Disk newDisk = new Disk();
		newDisk.copyFrom(disk);
		boolean result = newDisk.equals(disk);
		
		assertTrue(result);
	}

	/**
	 * Run the boolean equals(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testEquals_2()
		throws Exception {
		Server server = new Server();
		String name = "";
		String mountPoint = "";
		Double space = new Double(1.0);
		Double spaceInUse = new Double(1.0);
		Device.DEVICE_STATUS status = Device.DEVICE_STATUS.INITIALIZED;
		Disk newDisk  = new Disk(server, name, mountPoint, space, spaceInUse, status);

		boolean result = newDisk.equals(disk);

		assertTrue(!result);
	}

	/**
	 * Run the boolean equals(Object) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testEquals_3()
		throws Exception {
		
		Disk newDisk  = new Disk();
		newDisk.copyFrom(disk);
		boolean result = newDisk.equals(disk);

		assertTrue(result);
	}

	
	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testFilter_1()
		throws Exception {
		String filterString = "";
		boolean caseSensitive = true;
		boolean result = disk.filter(filterString, caseSensitive);
		
		assertTrue(result);
	}

	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testFilter_2()
		throws Exception {
		String filterString = "Serv";
		boolean caseSensitive = true;
		boolean result = disk.filter(filterString, caseSensitive);

		assertTrue(result);
	}

	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testFilter_3()
		throws Exception {
		String filterString = "serv";
		boolean caseSensitive = true;
		boolean result = disk.filter(filterString, caseSensitive);
		assertTrue(!result);
	}

	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testFilter_4()
		throws Exception {
		String filterString = "hitachi";
		boolean caseSensitive = true;

		boolean result = disk.filter(filterString, caseSensitive);
		assertTrue(!result);
	}


	
	/**
	 * Run the boolean filter(String,boolean) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testFilter_5()
		throws Exception {
		String filterString = "hitachi";
		boolean caseSensitive = false;

		boolean result = disk.filter(filterString, caseSensitive);
		assertTrue(result);
	}


	/**
	 * Run the Double getSpace() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testGetSpace_1()
		throws Exception {
		Double result = disk.getSpace();
		assertNotNull(result);
		assertTrue(result instanceof Double);
	}



	/**
	 * Run the Double getSpaceInUse() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testGetSpaceInUse_1()
		throws Exception {
		Double result = disk.getSpaceInUse();
		assertNotNull(result);
		assertTrue(result instanceof Double);
	}


	/**
	 * Run the boolean hasPartitions() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testHasPartitions_1()
		throws Exception {
		disk.setPartitions(new ArrayList<Partition>());
		boolean result = disk.hasPartitions();
		assertTrue(!result);
	}

	

	/**
	 * Run the boolean isReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testIsReady_1()
		throws Exception {
		boolean result = disk.isReady();
		System.out.println("Disk status is [" + result + "] on [" + disk.getName() +"] and Status is [" + disk.getStatusStr() +"]");
		assertTrue(result);
	}

	/**
	 * Run the boolean isReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testIsReady_2()
		throws Exception {
		disk.setPartitions(new ArrayList<Partition>());
		boolean result = disk.isReady();
		assertTrue(result);
	}

	/**
	 * Run the boolean isReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testIsReady_3()
		throws Exception {
		disk.setStatus(DEVICE_STATUS.UNINITIALIZED);
		boolean result = disk.isReady();
		assertTrue(!result);
	}

	/**
	 * Run the boolean isReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testIsReady_4()
		throws Exception {
		disk.setStatus(DEVICE_STATUS.IO_ERROR);
		boolean result = disk.isReady();

		assertTrue(!result);
	}

	/**
	 * Run the boolean isReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testIsReady_5()
		throws Exception {
		disk.setStatus(DEVICE_STATUS.INITIALIZING);
		boolean result = disk.isReady();

		assertTrue(!result);
	}

	/**
	 * Run the boolean isReady() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Test
	public void testIsReady_6()
		throws Exception {
		disk.setStatus(DEVICE_STATUS.UNKNOWN);
		boolean result = disk.isReady();

		assertTrue(!result);
	}

	
	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		Server server = new Server("Server1", null, 2, 25D, 5000D, 2000D);
		disk = new Disk(server,"sda1", "/md0/sda1", 5000D, 3000D, DEVICE_STATUS.INITIALIZED);
		disk.setDescription("Hitachi HTS72323 ATA");
		disk.setFsType("ext4");
		disk.setFsVersion("3.2.3");
		disk.setType(DEVICE_TYPE.DATA);
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 10/19/11 6:26 PM
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
	 * @generatedBy CodePro at 10/19/11 6:26 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(DiskTest.class);
	}
}