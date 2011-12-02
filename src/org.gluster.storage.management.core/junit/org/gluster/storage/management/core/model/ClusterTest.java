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
package org.gluster.storage.management.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.gluster.storage.management.core.model.Brick;
import org.gluster.storage.management.core.model.Cluster;
import org.gluster.storage.management.core.model.Disk;
import org.gluster.storage.management.core.model.GlusterServer;
import org.gluster.storage.management.core.model.Server;
import org.gluster.storage.management.core.model.Volume;
import org.gluster.storage.management.core.model.Brick.BRICK_STATUS;
import org.gluster.storage.management.core.model.Device.DEVICE_STATUS;
import org.gluster.storage.management.core.model.Device.DEVICE_TYPE;
import org.gluster.storage.management.core.model.Server.SERVER_STATUS;
import org.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import org.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import org.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * The class <code>ClusterTest</code> contains tests for the class <code>{@link Cluster}</code>.
 *
 * @generatedBy CodePro at 10/18/11 2:53 PM
 * @author root
 * @version $Revision: 1.0 $
 */
public class ClusterTest {
	private Cluster fixture;
	
	public List<Disk> getDisks(Server server) {
		List<Disk> disks = new ArrayList<Disk>();
		disks.add(new Disk(server, "sda", "", 12456.0, 0.0, DEVICE_STATUS.UNINITIALIZED));
		Disk disk = new Disk(server, "sdb1", "/export", 134342456.0, 120343.0, DEVICE_STATUS.INITIALIZED);
		disk.setType(DEVICE_TYPE.DATA);
		disks.add(disk);
		disk = new Disk(server, "sdc2", "/export", 876534346.0, 56334.0, DEVICE_STATUS.INITIALIZED);
		disk.setType(DEVICE_TYPE.DATA);
		disks.add(disk);
		return disks;
	}
	
	public List<GlusterServer> getServers() {
		List<GlusterServer> servers = new ArrayList<GlusterServer>();
		GlusterServer server1 = new GlusterServer("Server1", null, SERVER_STATUS.ONLINE, 2, 10, 8, 4);
		server1.addDisks(getDisks(server1));
		servers.add(server1);
		
		GlusterServer server2 = new GlusterServer("Server2", null, SERVER_STATUS.ONLINE, 1, 90, 10, 9);
		server2.addDisks(getDisks(server2));
		servers.add(server2);
		
		GlusterServer server3 = new GlusterServer("Server3", null, SERVER_STATUS.ONLINE, 4, 50, 6, 5);
		server3.addDisks(getDisks(server3));
		servers.add(server3);
		
		GlusterServer server4 = new GlusterServer("Server4", null, SERVER_STATUS.ONLINE, 2, 40, 4, 3);
		server4.addDisks(getDisks(server4));
		servers.add(server4);

		return servers;
	}
	
	public List<Brick> getBricks(String volumeName) {
		List<Brick> bricks = new ArrayList<Brick>();
		Brick brick1 = new Brick("Server1", BRICK_STATUS.ONLINE, "/sda1/"+volumeName);
		bricks.add(brick1);
		Brick brick2 = new Brick("Server2", BRICK_STATUS.ONLINE, "/sdb1/"+volumeName);
		bricks.add(brick2);
		Brick brick3 = new Brick("Server3", BRICK_STATUS.ONLINE, "/sdc1/"+volumeName);
		bricks.add(brick3);
		Brick brick4 = new Brick("Server4", BRICK_STATUS.OFFLINE, "/sda2/"+volumeName);
		bricks.add(brick4);
		return bricks;
	}
	
	
	public void populateVolumes(Cluster cluster) {
		Volume volume1 = new Volume("Songs", cluster, VOLUME_TYPE.DISTRIBUTE, TRANSPORT_TYPE.ETHERNET,
				VOLUME_STATUS.ONLINE);
		volume1.addBricks(getBricks(volume1.getName()));
		cluster.addVolume(volume1);

		Volume volume2 = new Volume("Movie", cluster, VOLUME_TYPE.DISTRIBUTE, TRANSPORT_TYPE.ETHERNET,
				VOLUME_STATUS.ONLINE);
		volume2.addBricks(getBricks(volume1.getName()));
		cluster.addVolume(volume2);

		Volume volume3 = new Volume("graphics", cluster, VOLUME_TYPE.DISTRIBUTE, TRANSPORT_TYPE.INFINIBAND,
				VOLUME_STATUS.ONLINE);
		volume3.addBricks(getBricks(volume1.getName()));
		cluster.addVolume(volume3);

		Volume volume4 = new Volume("cartoon", cluster, VOLUME_TYPE.DISTRIBUTE, TRANSPORT_TYPE.ETHERNET,
				VOLUME_STATUS.ONLINE);
		volume4.addBricks(getBricks(volume1.getName()));
		cluster.addVolume(volume4);
		return;
	}
	
	/**
	 * Run the double getDiskSpaceInUse() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/18/11 2:53 PM
	 */
	@Test
	public void testGetDiskSpaceInUse_1()
		throws Exception {
		double result = fixture.getDiskSpaceInUse();

		assertEquals(706708.0, result, 0.1);
	}

	/**
	 * Run the GlusterServer getServer(String) method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/18/11 2:53 PM
	 */
	@Test
	public void testGetServer_1()
		throws Exception {
		GlusterServer result = fixture.getServer("Server1");

		assertNotNull(result);
		assertEquals("Server1", result.getName());
		assertEquals(2, result.getNumOfCPUs());
		assertEquals(3, result.getNumOfDisks() );
	}

	/**
	 * Run the double getTotalDiskSpace() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/18/11 2:53 PM
	 */
	@Test
	public void testGetTotalDiskSpace_1()
		throws Exception {
		double result = fixture.getTotalDiskSpace();
		assertEquals(4043557032.0, result, 0.1);   // Including unformatted disks(!)
	}

	/**
	 * Run the double getTotalDiskSpace() method test.
	 *
	 * @throws Exception
	 *
	 * @generatedBy CodePro at 10/18/11 2:53 PM
	 */
	@Test
	public void testGetVolume_1()
		throws Exception {
		populateVolumes(fixture);	
		Volume result = fixture.getVolume("Songs");
		assertNotNull(result);
		assertTrue(result instanceof Volume);
	}


	/**
	 * Perform pre-test initialization.
	 *
	 * @throws Exception
	 *         if the initialization fails for some reason
	 *
	 * @generatedBy CodePro at 10/18/11 2:53 PM
	 */
	@Before
	public void setUp()
		throws Exception {
		fixture = new Cluster();
		fixture.setServers(getServers());
	}

	/**
	 * Perform post-test clean-up.
	 *
	 * @throws Exception
	 *         if the clean-up fails for some reason
	 *
	 * @generatedBy CodePro at 10/18/11 2:53 PM
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
	 * @generatedBy CodePro at 10/18/11 2:53 PM
	 */
	public static void main(String[] args) {
		new org.junit.runner.JUnitCore().run(ClusterTest.class);
	}
}