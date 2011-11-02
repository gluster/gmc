package com.gluster.storage.management.gateway.services;

import java.util.List;
import java.util.Map;

import com.gluster.storage.management.core.model.TaskStatus;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.TRANSPORT_TYPE;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;

/**
 * Interface for interacting with GlusterFS. Every version of GlusterFS supported by the Gluster Management Gateway will
 * have a corresponding implementation of this interface.
 */
public interface GlusterInterface {

	/**
	 * Returns the GlusterFS version on given server.
	 * 
	 * @param serverName
	 *            Server on which Gluster version is to be checked.
	 * @return
	 */
	public abstract String getVersion(String serverName);

	/**
	 * Adds the new server to an existing cluster.
	 * 
	 * @param existingServer
	 *            Server part of the existing cluster.
	 * @param newServer
	 *            Server to be added to the cluster.
	 */
	public abstract void addServer(String existingServer, String newServer);

	/**
	 * Removes given server from the cluster by executing appropriate Gluster command on given server.
	 * 
	 * @param existingServer
	 *            Server part of the existing cluster.
	 * @param serverName
	 *            Server to be removed from the cluster.
	 */
	public abstract void removeServer(String existingServer, String serverName);

	/**
	 * Starts the given volume by executing appropriate Gluster command on given server.
	 * 
	 * @param volumeName
	 *            Volume to be started.
	 * @param serverName
	 *            Server on which the Gluster command is to be executed. This server must be part of the cluster to
	 *            which the volume belongs.
	 * @param force
	 *            Flag indicating whether the "force" option should be used for starting the Volume. This is typically
	 *            used when Volume is already started, but at least one of its bricks is offline, and results in
	 *            bringing up the offline bricks.
	 */
	public abstract void startVolume(String volumeName, String serverName, Boolean force);

	/**
	 * Stops the given volume by executing appropriate Gluster command on given server.
	 * 
	 * @param volumeName
	 *            Volume to be stopped.
	 * @param serverName
	 *            Server on which the Gluster command is to be executed. This server must be part of the cluster to
	 *            which the volume belongs.
	 * @param force
	 *            Flag indicating whether the Volume should be stopped forcefully. This is typically used if the regular
	 *            stop option fails because of issues like rebalance / brick migration / geo-replication being in
	 *            progress. This results in forcefully stopping the volume, leaving the other processes intact.
	 */
	public abstract void stopVolume(String volumeName, String serverName, Boolean force);

	/**
	 * Resets volume options on the given volume by executing appropriate Gluster command on given server.
	 * 
	 * @param volumeName
	 *            Volume on which options are to be reset.
	 * @param serverName
	 *            Server on which the Gluster command is to be executed. This server must be part of the cluster to
	 *            which the volume belongs.
	 */
	public abstract void resetOptions(String volumeName, String serverName);

	/**
	 * Creates a volume on given volume using given properties.
	 * 
	 * @param serverName
	 *            Server on which the Gluster command for creating the volume will be executed. This must be part of the
	 *            cluster in which the volume is to be created.
	 * @param volumeName
	 *            Name of the volume.
	 * @param volumeType
	 *            Type of the volume e.g. DISTRIBUTE, REPLICATE, STRIPE, etc. See {@link VOLUME_TYPE} for full list of
	 *            valid values.
	 * @param transportType
	 *            Transport type of the volume e.g. ETHERNET. See {@link TRANSPORT_TYPE} for full list of valid values.
	 * @param replOrStripeCount
	 *            Replica Count or Stripe count depending on the volume type. Ignored in case of pure distribute volumes
	 *            (no replicate, no stripe).
	 * @param bricks
	 *            Comma separated list of volume brick directories in following format: <br>
	 *            server1:dir1,server2:dir2,server3:dir3,...,servern:dirn
	 * @param accessProtocols
	 *            Optional parameter indicating access protocols to be enabled for the volume. If empty/null, GLUSTERFS
	 *            and NFS will be enabled.
	 * @param options
	 *            A comma separated list of volume options to be set on the newly created volume in following format: <br>
	 *            key1=value1,key2=value2,key3=value3,...,keyn=valuen
	 */
	public abstract void createVolume(String serverName, String volumeName, String volumeType, String transportType,
			Integer replOrStripeCount, String bricks, String accessProtocols, String options);

	/**
	 * Creates / Sets the given options on the given volume by executing appropriate Gluster command on the given
	 * server.
	 * 
	 * @param volumeName
	 *            Volume on which the options are to be set.
	 * @param options
	 *            Map containing the volume options to be set. Key = option key, Value = option value.
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 */
	public abstract void createOptions(String volumeName, Map<String, String> options, String serverName);

	/**
	 * Sets the given option on given volume by executing appropriate Gluster command on the given server.
	 * 
	 * @param volumeName
	 *            Volume on which the option is to be set.
	 * @param key
	 *            Option key (name)
	 * @param value
	 *            Option value
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 */
	public abstract void setOption(String volumeName, String key, String value, String serverName);

	/**
	 * Deletes the given volume by executing appropriate Gluster command on the given server.
	 * 
	 * @param volumeName
	 *            Volume to be deleted.
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 */
	public abstract void deleteVolume(String volumeName, String serverName);

	/**
	 * Fetches properties of the given Volume by executing appropriate Gluster command on the given server.
	 * 
	 * @param volumeName
	 *            Volume whose properties are to be fetched.
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @return A {@link Volume} object containing all properties of the given volume
	 */
	public abstract Volume getVolume(String volumeName, String serverName);

	/**
	 * Fetches the list of all volumes (along with their properties) by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @return A list of {@link Volume} objects representing every volume present in the cluster to which the given
	 *         server belongs.
	 */
	public abstract List<Volume> getAllVolumes(String serverName);

	/**
	 * Adds given list of bricks to given Volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param volumeName
	 *            Volume to which the bricks are to be added.
	 * @param bricks
	 *            List of bricks to be added, each in the format serverName:brickDirectory
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 */
	public abstract void addBricks(String volumeName, List<String> bricks, String serverName);

	/**
	 * Removes given list of bricks from given volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param volumeName
	 *            Volume from which the bricks are to be removed
	 * @param bricks
	 *            List of bricks to be removed, each in the format serverName:brickDirectory
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 */
	public abstract void removeBricks(String volumeName, List<String> bricks, String serverName);

	/**
	 * Returns the log location of given brick of given volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param volumeName
	 *            Volume for which log location is to be fetched.
	 * @param brickName
	 *            Brick of the volume for which log location is to be fetched.
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @return Full path of the log file location (directory) for the given Volume Brick.
	 */
	public abstract String getLogLocation(String volumeName, String brickName, String serverName);

	/**
	 * Returns the log file name for given brick directory.
	 * 
	 * @param brickDir
	 *            Brick directory for which log file name is to be returned.
	 * @return The log file name (without path) for the given brick directory.
	 */
	public abstract String getLogFileNameForBrickDir(String brickDir);

	/**
	 * Checks the status of "Rebalance" operation on given Volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume whose rebalance status is to be checked.
	 * @return Object of {@link TaskStatus} representing the status of Volume Rebalance.
	 */
	public abstract TaskStatus checkRebalanceStatus(String serverName, String volumeName);

	/**
	 * Stops "Rebalance" operation running on given Volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume whose Rebalance is to be stopped.
	 */
	public abstract void stopRebalance(String serverName, String volumeName);

	/**
	 * Starts Brick Migration (replace-brick) on given Volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume whose Brick is to be migrated/replaced.
	 * @param fromBrick
	 *            The source Brick (to be replaced).
	 * @param toBrick
	 *            The destination Brick (will replace the source Brick).
	 */
	public abstract void startBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick);

	/**
	 * Pauses Brick Migration (replace-brick) running on given Volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume whose Brick is being migrated/replaced.
	 * @param fromBrick
	 *            The source Brick (being replaced).
	 * @param toBrick
	 *            The destination Brick (which is replacing the source Brick).
	 */
	public abstract void pauseBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick);

	/**
	 * Aborts Brick Migration (replace-brick) running on given Volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume whose Brick is being migrated/replaced.
	 * @param fromBrick
	 *            The source Brick (being replaced).
	 * @param toBrick
	 *            The destination Brick (which is replacing the source Brick)
	 */
	public abstract void stopBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick);

	/**
	 * Commits Brick Migration (replace-brick) running on given Volume by executing appropriate Gluster command on the
	 * given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume whose Brick is being migrated/replaced.
	 * @param fromBrick
	 *            The source Brick (being replaced).
	 * @param toBrick
	 *            The destination Brick (which is replacing the source Brick)
	 */
	public abstract void commitBrickMigration(String serverName, String volumeName, String fromBrick, String toBrick);

	/**
	 * Checks status of Brick Migration (replace-brick) running on given Volume by executing appropriate Gluster command
	 * on the given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @param volumeName
	 *            Volume whose Brick is being migrated/replaced.
	 * @param fromBrick
	 *            The source Brick (being replaced).
	 * @param toBrick
	 *            The destination Brick (which is replacing the source Brick)
	 * @return A {@link TaskStatus} object representing the status of Brick Migration
	 */
	public abstract TaskStatus checkBrickMigrationStatus(String serverName, String volumeName, String fromBrick,
			String toBrick);

	/**
	 * Returns information about all the supported Volume Options by executing appropriate Gluster command
	 * on the given server.
	 * 
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 * @return A {@link VolumeOptionInfoListResponse} object containing information about each and every supported
	 *         Volume Option
	 */
	public abstract VolumeOptionInfoListResponse getVolumeOptionsInfo(String serverName);

	/**
	 * Rotates the logs for given Bricks of given Volume by executing appropriate Gluster command
	 * on the given server.
	 * 
	 * @param volumeName
	 *            Volume whose logs are to be rotated.
	 * @param brickList
	 *            List of bricks whose logs are to be rotated, each in the format serverName:brickDirectory <br>
	 *            This is an optional parameter. If null or empty, all logs of the Volume will be rotated.
	 * @param serverName
	 *            The server on which the Gluster command will be executed. This must be part of the cluster to which
	 *            the volume belongs.
	 */
	public abstract void logRotate(String volumeName, List<String> brickList, String serverName);
}