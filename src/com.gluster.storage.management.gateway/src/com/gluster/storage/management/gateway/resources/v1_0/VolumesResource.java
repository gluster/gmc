/**
 * VolumesResource.java
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
package com.gluster.storage.management.gateway.resources.v1_0;

import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_ACCESS_PROTOCOLS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_AUTO_COMMIT;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_BRICKS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_CIFS_ENABLE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_CIFS_USERS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_FIX_LAYOUT;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_FORCED_DATA_MIGRATE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_MIGRATE_DATA;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OPERATION;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OPTION_KEY;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_OPTION_VALUE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_REPLICA_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_SOURCE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_STRIPE_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_TARGET;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_TRANSPORT_TYPE;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VOLUME_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VOLUME_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.FORM_PARAM_VOLUME_TYPE;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_CLUSTER_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.PATH_PARAM_VOLUME_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_BRICKS;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_BRICK_NAME;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DELETE_OPTION;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_DOWNLOAD;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_FROM_TIMESTAMP;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_LINE_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_LOG_SEVERITY;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_TO_TIMESTAMP;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_BRICKS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_DEFAULT_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_DOWNLOAD;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_LOGS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_TASKS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_VOLUMES;
import static com.gluster.storage.management.core.constants.RESTConstants.TASK_START;
import static com.gluster.storage.management.core.constants.RESTConstants.TASK_STOP;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.Brick;
import com.gluster.storage.management.core.model.GlusterServer;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.NAS_PROTOCOL;
import com.gluster.storage.management.core.model.Volume.VOLUME_TYPE;
import com.gluster.storage.management.core.model.VolumeLogMessage;
import com.gluster.storage.management.core.response.LogMessageListResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.DateUtil;
import com.gluster.storage.management.core.utils.FileUtil;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.gluster.storage.management.gateway.constants.VolumeOptionsDefaults;
import com.gluster.storage.management.gateway.data.ClusterInfo;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.tasks.MigrateBrickTask;
import com.gluster.storage.management.gateway.tasks.RebalanceVolumeTask;
import com.gluster.storage.management.gateway.utils.GlusterUtil;
import com.gluster.storage.management.gateway.utils.ServerUtil;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_VOLUMES)
public class VolumesResource extends AbstractResource {
	private static final String VOLUME_DIRECTORY_CLEANUP_SCRIPT = "clear_volume_directory.py";
	
	private static final String VOLUME_CIFS_GRUN_SCRIPT = "grun.py";
	private static final String VOLUME_CREATE_CIFS_SCRIPT = "create_volume_cifs_all.py";
	private static final String VOLUME_GET_CIFS_USERS_SCRIPT = "get_volume_user_cifs.py";
	private static final String VOLUME_DELETE_CIFS_SCRIPT = "delete_volume_cifs_all.py";
	private static final String VOLUME_MODIFY_CIFS_SCRIPT = "update_volume_cifs_all.py";
	
	private static final String VOLUME_START_CIFS_PEER_SCRIPT = "start_volume_cifs.py";
	private static final String VOLUME_STOP_CIFS_PEER_SCRIPT = "stop_volume_cifs.py";

	private static final String ALL_SERVERS_FILE_NAME = "servers";

	private static final String VOLUME_BRICK_LOG_SCRIPT = "get_volume_brick_log.py";
	private static final Logger logger = Logger.getLogger(VolumesResource.class);

	@InjectParam
	private ServerUtil serverUtil;

	@InjectParam
	private GlusterUtil glusterUtil;

	@InjectParam
	private ClusterService clusterService;

	@InjectParam
	private VolumeOptionsDefaults volumeOptionsDefaults;

	@InjectParam
	private TasksResource taskResource;
	
	private ProcessUtil processUtil = new ProcessUtil();
	
	@GET
	@Produces({MediaType.APPLICATION_XML})
	public Response getVolumesXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		return getVolumes(clusterName, MediaType.APPLICATION_XML);
	}

	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getVolumesJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		return getVolumes(clusterName, MediaType.APPLICATION_JSON);
	}
	
	public Response getVolumes(String clusterName, String mediaType) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		ClusterInfo cluster = clusterService.getCluster(clusterName);
		if (cluster == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		if(cluster.getServers().size() == 0) {
			// no server added yet. return an empty array.
			return okResponse(new VolumeListResponse(), mediaType);
		}

		return okResponse(getVolumes(clusterName), mediaType);
	}

	public VolumeListResponse getVolumes(String clusterName) {
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			return new VolumeListResponse(new ArrayList<Volume>());
		}

		try {
			return new VolumeListResponse(getVolumesCifsUsers(clusterName, glusterUtil.getAllVolumes(onlineServer.getName())));
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				return new VolumeListResponse(new ArrayList<Volume>());
			}
			return new VolumeListResponse(getVolumesCifsUsers(clusterName, glusterUtil.getAllVolumes(onlineServer.getName())));
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response createVolume(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@FormParam(FORM_PARAM_VOLUME_NAME) String volumeName, @FormParam(FORM_PARAM_VOLUME_TYPE) String volumeType,
			@FormParam(FORM_PARAM_TRANSPORT_TYPE) String transportType,
			@FormParam(FORM_PARAM_REPLICA_COUNT) Integer replicaCount,
			@FormParam(FORM_PARAM_STRIPE_COUNT) Integer stripeCount, @FormParam(FORM_PARAM_BRICKS) String bricks,
			@FormParam(FORM_PARAM_ACCESS_PROTOCOLS) String accessProtocols,
			@FormParam(FORM_PARAM_VOLUME_OPTIONS) String options, @FormParam(FORM_PARAM_CIFS_USERS) String cifsUsers) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}
		
		String missingParam = checkMissingParamsForCreateVolume(volumeName, volumeType, transportType, replicaCount,
				stripeCount, bricks, accessProtocols, options);
		if (missingParam != null) {
			return badRequestResponse("Parameter [" + missingParam + "] is missing in request!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		if (volumeType.equals(VOLUME_TYPE.DISTRIBUTED_MIRROR) && replicaCount <= 0) {
			return badRequestResponse("Replica count must be a positive integer");
		}

		if (volumeType.equals(VOLUME_TYPE.DISTRIBUTED_STRIPE) && stripeCount <= 0) {
			return badRequestResponse("Stripe count must be a positive integer");
		}

		try {
			performCreateVolume(clusterName, volumeName, volumeType, transportType, replicaCount, stripeCount, bricks,
					accessProtocols, options, cifsUsers);
			return createdResponse(volumeName);
		} catch (Exception e) {
			return errorResponse(e.getMessage());
		}
	}

	public void performCreateVolume(String clusterName, String volumeName, String volumeType, String transportType,
			Integer replicaCount, Integer stripeCount, String bricks, String accessProtocols, String options,
			String cifsUsers) {
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			glusterUtil.createVolume(onlineServer.getName(), volumeName, volumeType, transportType, replicaCount,
					stripeCount, bricks, accessProtocols, options);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}

			glusterUtil.createVolume(onlineServer.getName(), volumeName, volumeType, transportType, replicaCount,
					stripeCount, bricks, accessProtocols, options);
		}

		List<String> nasProtocols = Arrays.asList(accessProtocols.split(","));
		// if cifs enabled
		if (nasProtocols.contains(NAS_PROTOCOL.CIFS.toString())) {
			try {
				createCIFSUsers(clusterName, volumeName, cifsUsers);
			} catch (Exception e) {
				throw new GlusterRuntimeException(CoreConstants.NEWLINE + e.getMessage());
			}
		}
	}
	
	/**
	 * Returns name of the missing parameter if any. If all parameters are present, 
	 * @param volumeName
	 * @param volumeType
	 * @param transportType
	 * @param replicaCount
	 * @param stripeCount
	 * @param bricks
	 * @param accessProtocols
	 * @param options
	 * @return
	 */
	private String checkMissingParamsForCreateVolume(String volumeName, String volumeType,
			String transportType, Integer replicaCount, Integer stripeCount, String bricks, String accessProtocols,
			String options) {
		
		return (volumeName == null || volumeName.isEmpty()) ? FORM_PARAM_VOLUME_NAME :
				(volumeType == null || volumeType.isEmpty()) ? FORM_PARAM_VOLUME_TYPE :
				(transportType == null || transportType.isEmpty()) ? FORM_PARAM_TRANSPORT_TYPE :
				(replicaCount == null) ? FORM_PARAM_REPLICA_COUNT :
				(stripeCount == null) ? FORM_PARAM_STRIPE_COUNT :
				(bricks == null || bricks.isEmpty()) ? FORM_PARAM_BRICKS :
				(accessProtocols == null || accessProtocols.isEmpty()) ? FORM_PARAM_ACCESS_PROTOCOLS :
				(options == null || options.isEmpty()) ? FORM_PARAM_VOLUME_OPTIONS :
				null;
	}

	@GET
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getVolumeXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {
		return getVolume(clusterName, volumeName, MediaType.APPLICATION_XML);
	}

	@GET
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVolumeJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {
		return getVolume(clusterName, volumeName, MediaType.APPLICATION_JSON);
	}

	private Response getVolume(String clusterName, String volumeName, String mediaType) {
		Volume volume = null;
		
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		try {
			volume = getVolume(clusterName, volumeName);
			return okResponse(volume, mediaType);
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}
	}

	private Volume getVolume(String clusterName, String volumeName) {
		Volume volume;
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			volume = glusterUtil.getVolume(volumeName, onlineServer.getName());
			// Collect the CIFS users if CIFS Re-exported 
			getVolumeCifsUsers(clusterName, volume);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}
			volume = glusterUtil.getVolume(volumeName, onlineServer.getName());
			// Collect the CIFS users if CIFS Re-exported 
			getVolumeCifsUsers(clusterName, volume);
		}
		return volume;
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	public Response performOperation(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @FormParam(FORM_PARAM_OPERATION) String operation,
			@FormParam(FORM_PARAM_FIX_LAYOUT) Boolean isFixLayout,
			@FormParam(FORM_PARAM_MIGRATE_DATA) Boolean isMigrateData,
			@FormParam(FORM_PARAM_FORCED_DATA_MIGRATE) Boolean isForcedDataMigrate,
			@FormParam(FORM_PARAM_CIFS_ENABLE) Boolean enableCifs, @FormParam(FORM_PARAM_CIFS_USERS) String cifsUsers) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		if (volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		try {
			if (operation.equals(RESTConstants.TASK_REBALANCE_START)) {
				String taskId = rebalanceStart(clusterName, volumeName, isFixLayout, isMigrateData, isForcedDataMigrate);
				return acceptedResponse(RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_TASKS
						+ "/" + taskId);
			} else if (operation.equals(RESTConstants.TASK_REBALANCE_STOP)) {
				rebalanceStop(clusterName, volumeName);
			} else if (operation.equals(RESTConstants.FORM_PARAM_CIFS_CONFIG)) {
				if (enableCifs) {
					modifyCIFSUsers(clusterName, volumeName, cifsUsers);
				} else {
					deleteCifsUsers(clusterName, volumeName);
					//TODO: workaround - If samba service are not stopped by "deleteCifsUsers" script, 
					// gateway needs to stop the services  
					// modifyCIFSUsers(clusterName, volumeName, "");
					// stopCifsReExport(clusterName, volumeName);
				}
			} else {
				performVolumeOperation(clusterName, volumeName, operation);
			}
			return noContentResponse();
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}
	}
	
	private void performVolumeOperation(String clusterName, String volumeName, String operation) {
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		try {
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}

			performOperation(clusterName, volumeName, operation, onlineServer);
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			performOperation(clusterName, volumeName, operation, onlineServer);
		}
	}

	private Status performOperation(String clusterName, String volumeName, String operation, GlusterServer onlineServer) {
		Status result;
		Volume volume = null;
		try {
			volume = getVolume(clusterName, volumeName);
		} catch (Exception e) {
			throw new GlusterRuntimeException("Could not fetch volume info for volume [" + volumeName + "]"
					+ e.getMessage());
		}

		if (operation.equals(TASK_START)) {
			result = glusterUtil.startVolume(volumeName, onlineServer.getName());

			// call the start_volume_cifs.py script only if the volume is cifs enabled
			if (volume.isCifsEnable() && result.isSuccess()) {
				startCifsReExport(clusterName, volumeName);
			}
			return result;
		} else if (operation.equals(TASK_STOP)) {
			result = glusterUtil.stopVolume(volumeName, onlineServer.getName());

			// call the stop_volume_cifs.py script only if the volume is cifs enabled
			if (volume.isCifsEnable() && result.isSuccess()) {
				stopCifsReExport(clusterName, volumeName);
			}
			return result;
		} else {
			throw new GlusterValidationException("Invalid operation code [" + operation + "]");
		}
	}
	
	private void startCifsReExport(String clusterName, String volumeName) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_CIFS_GRUN_SCRIPT,
					file.getAbsolutePath(), VOLUME_START_CIFS_PEER_SCRIPT, volumeName);
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in starting CIFS services for volume [" + volumeName + "]: "
					+ e.getMessage());
		}
	}

	private void stopCifsReExport(String clusterName, String volumeName) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_CIFS_GRUN_SCRIPT,
					file.getAbsolutePath(), VOLUME_STOP_CIFS_PEER_SCRIPT, volumeName);
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in stoping CIFS services for volume [" + volumeName + "]: "
					+ e.getMessage());
		}
	}
	
	private void deleteCifsUsers(String clusterName, String volumeName) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_DELETE_CIFS_SCRIPT,
					file.getAbsolutePath(), volumeName);
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in deleting CIFS configuration [" + volumeName + "]: "
					+ e.getMessage());
		}
	}

	private void createCIFSUsers(String clusterName, String volumeName, String cifsUsers) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_CREATE_CIFS_SCRIPT,
					file.getAbsolutePath(), volumeName, cifsUsers.replace(",", " "));
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in creating CIFS configuration [" + volumeName + "]: "
					+ e.getMessage());
		}
	}

	private void modifyCIFSUsers(String clusterName, String volumeName, String cifsUsers) {
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil.executeGlusterScript(true, VOLUME_MODIFY_CIFS_SCRIPT,
					file.getAbsolutePath(), volumeName, cifsUsers.replace(",", " "));
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in updating CIFS configuration [" + volumeName + "]: "
					+ e.getMessage());
		}
	}

	private void getVolumeCifsUsers(String clusterName, Volume volume) {
		List<String> users = new ArrayList<String>();
		try {
			File file = createOnlineServerList(clusterName);
			ProcessResult result = serverUtil
					.executeGlusterScript(true, VOLUME_GET_CIFS_USERS_SCRIPT, volume.getName());
			file.delete();
			if (!result.isSuccess()) {
				throw new GlusterRuntimeException(result.toString());
			}
			String output = result.getOutput().trim();
			if (output.isEmpty()) {
				volume.disableCifs();
			} else {
				users = Arrays.asList(output.split(CoreConstants.NEWLINE));
				volume.enableCifs();
				volume.setCifsUsers(users);
			}
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in fetching CIFS users [" + volume.getName() + "]: "
					+ e.getMessage());
		}
		return;
	}

	private List<Volume> getVolumesCifsUsers(String clusterName, List<Volume> volumes) {
		for (Volume volume: volumes) {
			getVolumeCifsUsers(clusterName, volume);
		}
		return volumes;
	}
	
	public File createOnlineServerList(String clusterName) {
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String clusterServersListFile = FileUtil.getTempDirName() + CoreConstants.FILE_SEPARATOR
		+ ALL_SERVERS_FILE_NAME + "_" + timestamp;
		
		try {
			GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
			List<GlusterServer> glusterServers = glusterUtil.getGlusterServers(onlineServer);
			File serversFile = new File(clusterServersListFile);
			FileOutputStream fos = new FileOutputStream(serversFile);
			for (GlusterServer server : glusterServers) {
				fos.write((server.getName() + CoreConstants.NEWLINE).getBytes());
			}
			fos.close();
			return serversFile;
		} catch (Exception e) {
			throw new GlusterRuntimeException("Error in preparing server list: [" + e.getMessage() + "]");
		}
	}

	@DELETE
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	public Response deleteVolume(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName,
			@QueryParam(QUERY_PARAM_DELETE_OPTION) Boolean deleteFlag) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty");
		}

		if (volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty");
		}
		
		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		if (deleteFlag == null) {
			deleteFlag = false;
		}
		
		Volume volume = null;
		try {
			volume = getVolume(clusterName, volumeName);			
		} catch (Exception e) {
			logger.error(e);
			return errorResponse(e.getMessage());
		}
		
		List<Brick> bricks = volume.getBricks();
		Status status = glusterUtil.deleteVolume(volumeName, clusterService.getOnlineServer(clusterName)
				.getName());
		if(!status.isSuccess()) {
			return errorResponse("Couldn't delete volume [" + volumeName + "]. Error: " + status);
		}

		try {
			postDelete(volumeName, bricks, deleteFlag);
		} catch(Exception e) {
			logger.error(e);
			return errorResponse("Volume [" + volumeName
					+ "] deleted from cluster, however following errors happened: " + CoreConstants.NEWLINE
					+ e.getMessage());
		}
		
		// call the delete_volume_cifs.py script only if the volume is cifs enabled
		if (volume.isCifsEnable()) {
			try {
				deleteCifsUsers(clusterName, volumeName);
			} catch (Exception e) {
				logger.error(e);
				return errorResponse(CoreConstants.NEWLINE + e.getMessage());
			}
		}
		return noContentResponse();
	}

	@DELETE
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_BRICKS)
	public Response removeBricks(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @QueryParam(QUERY_PARAM_BRICKS) String bricks,
			@QueryParam(QUERY_PARAM_DELETE_OPTION) Boolean deleteFlag) {
		List<String> brickList = Arrays.asList(bricks.split(",")); // Convert from comma separated string (query
																	// parameter)
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		if (volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty!");
		}

		if (bricks == null || bricks.isEmpty()) {
			return badRequestResponse("Parameter [" + QUERY_PARAM_BRICKS + "] is missing in request!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		if(deleteFlag == null) {
			deleteFlag = false;
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			return errorResponse("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			removeBricks(clusterName, volumeName, brickList, onlineServer);
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}

		try {
			cleanupDirectories(brickList, volumeName, brickList.size(), deleteFlag);
		} catch(Exception e) {
			// append cleanup error to prepare brick error
			return errorResponse(e.getMessage());
		}
		
		return noContentResponse();
	}

	public void removeBricks(String clusterName, String volumeName, List<String> brickList, GlusterServer onlineServer) {
		Status status;
		try {
			status = glusterUtil.removeBricks(volumeName, brickList, onlineServer.getName());
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				throw new GlusterRuntimeException("No online servers found in cluster [" + clusterName + "]");
			}
			status = glusterUtil.removeBricks(volumeName, brickList, onlineServer.getName());
		}
		if (!status.isSuccess()) {
			throw new GlusterRuntimeException(status.toString());
		}
	}

	@SuppressWarnings("rawtypes")
	private void cleanupDirectories(List<String> bricks, String volumeName, int maxIndex, boolean deleteFlag) {
		Status result;
		String errors = "";
		for (int i = 0; i < maxIndex; i++) {
			String[] brickInfo = bricks.get(i).split(":");
			String serverName = brickInfo[0];
			String brickDirectory = brickInfo[1];

			try {
			String output = serverUtil.executeScriptOnServer(true, serverName, VOLUME_DIRECTORY_CLEANUP_SCRIPT + " "
					+ brickDirectory + " " + (deleteFlag ? "-d" : ""), String.class);
			} catch(Exception e) {
				logger.error("Error while cleaning brick [" + serverName + ":" + brickDirectory + "] of volume ["
						+ volumeName + "] : " + e.getMessage(), e);
				errors += "[" + brickDirectory + "] => " + e.getMessage() + CoreConstants.NEWLINE;
			}
		}
		if(!errors.trim().isEmpty()) {
			throw new GlusterRuntimeException("Volume directory cleanup errors: " + errors.trim());
		}
	}

	private void postDelete(String volumeName, List<Brick> bricks, boolean deleteFlag) {
		for (Brick brick : bricks) {
			String brickDirectory = brick.getBrickDirectory();
			// String mountPoint = brickDirectory.substring(0, brickDirectory.lastIndexOf("/"));

			serverUtil.executeScriptOnServer(true, brick.getServerName(),
					VOLUME_DIRECTORY_CLEANUP_SCRIPT + " " + brickDirectory + " " + (deleteFlag ? "-d" : ""),
					String.class);
		}
	}

	@POST
	@Path("{" + PATH_PARAM_VOLUME_NAME + " }/" + RESOURCE_OPTIONS)
	public Response setOption(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName,
			@FormParam(RESTConstants.FORM_PARAM_OPTION_KEY) String key,
			@FormParam(RESTConstants.FORM_PARAM_OPTION_VALUE) String value) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}
		
		if(volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty!");
		}

		if(key == null || key.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_OPTION_KEY + "] is missing in request!");
		}
		
		if(value == null || value.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_OPTION_VALUE + "] is missing in request!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			return errorResponse("No online servers found in cluster [" + clusterName + "]");
		}
		
		try {
			glusterUtil.setOption(volumeName, key, value, onlineServer.getName());
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				return errorResponse("No online servers found in cluster [" + clusterName + "]");
			}
			
			try {
				glusterUtil.setOption(volumeName, key, value, onlineServer.getName());
			} catch(Exception e1) {
				return errorResponse(e1.getMessage());
			}
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}
		
		return createdResponse(key);
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + " }/" + RESOURCE_OPTIONS)
	public Response resetOptions(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}
		
		if(volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			return errorResponse("No online servers found in cluster [" + clusterName + "]");
		}

		try {
			glusterUtil.resetOptions(volumeName, onlineServer.getName());
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				return errorResponse("No online servers found in cluster [" + clusterName + "]");
			}
			
			try {
				glusterUtil.resetOptions(volumeName, onlineServer.getName());
			} catch(Exception e1) {
				return errorResponse(e1.getMessage());
			}
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}
		
		return noContentResponse();
	}

	@GET
	@Path(RESOURCE_DEFAULT_OPTIONS)
	@Produces(MediaType.APPLICATION_XML)
	public VolumeOptionInfoListResponse getDefaultOptionsXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		// TODO: Fetch all volume options with their default values from GlusterFS
		// whenever such a CLI command is made available in GlusterFS
		return new VolumeOptionInfoListResponse(Status.STATUS_SUCCESS, volumeOptionsDefaults.getDefaults(clusterName));
	}

	@GET
	@Path(RESOURCE_DEFAULT_OPTIONS)
	@Produces(MediaType.APPLICATION_JSON)
	public VolumeOptionInfoListResponse getDefaultOptionsJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		// TODO: Fetch all volume options with their default values from GlusterFS
		// whenever such a CLI command is made available in GlusterFS
		return new VolumeOptionInfoListResponse(Status.STATUS_SUCCESS, volumeOptionsDefaults.getDefaults(clusterName));
	}

	private List<VolumeLogMessage> getBrickLogs(Volume volume, Brick brick, Integer lineCount)
			throws GlusterRuntimeException {
		String logDir = glusterUtil.getLogLocation(volume.getName(), brick.getQualifiedName(), brick.getServerName());
		String logFileName = glusterUtil.getLogFileNameForBrickDir(brick.getBrickDirectory());
		String logFilePath = logDir + CoreConstants.FILE_SEPARATOR + logFileName;

		// Usage: get_volume_disk_log.py <volumeName> <diskName> <lineCount>
		LogMessageListResponse response = serverUtil.executeScriptOnServer(true, brick.getServerName(), VOLUME_BRICK_LOG_SCRIPT
				+ " " + logFilePath + " " + lineCount, LogMessageListResponse.class);

		// populate disk and trim other fields
		List<VolumeLogMessage> logMessages = response.getLogMessages();
		for (VolumeLogMessage logMessage : logMessages) {
			logMessage.setBrickDirectory(brick.getBrickDirectory());
		}
		return logMessages;
	}

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_LOGS + "/" + RESOURCE_DOWNLOAD)
	public Response downloadLogs(@PathParam(PATH_PARAM_CLUSTER_NAME) final String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) final String volumeName) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}
		
		if (volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty!");
		}
		
		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
	
		try {
			final Volume volume = getVolume(clusterName, volumeName);
			File archiveFile = new File(downloadLogs(volume));
			byte[] data = FileUtil.readFileAsByteArray(archiveFile);
			archiveFile.delete();
			return streamingOutputResponse(createStreamingOutput(data));
		} catch (Exception e) {
			logger.error("Volume [" + volumeName + "] doesn't exist in cluster [" + clusterName + "]! ["
					+ e.getStackTrace() + "]");
			throw (GlusterRuntimeException) e;
		}
	}
	

	private String downloadLogs(Volume volume) {
		// create temporary directory
		File tempDir = FileUtil.createTempDir();
		String tempDirPath = tempDir.getPath();

		for (Brick brick : volume.getBricks()) {
			String logDir = glusterUtil.getLogLocation(volume.getName(), brick.getQualifiedName(),
					brick.getServerName());
			String logFileName = glusterUtil.getLogFileNameForBrickDir(brick.getBrickDirectory());
			String logFilePath = logDir + CoreConstants.FILE_SEPARATOR + logFileName;

			serverUtil.getFileFromServer(brick.getServerName(), logFilePath, tempDirPath);

			String fetchedLogFile = tempDirPath + File.separator + logFileName;
			// append log file name with server name so that log files don't overwrite each other 
			// in cases where the brick log file names are same on multiple servers
			String localLogFile = tempDirPath + File.separator + brick.getServerName() + "-" + logFileName;

			FileUtil.renameFile(fetchedLogFile, localLogFile);
		}

		String gzipPath = FileUtil.getTempDirName() + CoreConstants.FILE_SEPARATOR + volume.getName() + "-logs.tar.gz";
		processUtil.executeCommand("tar", "czvf", gzipPath, "-C", tempDir.getParent(), tempDir.getName());

		// delete the temp directory
		FileUtil.recursiveDelete(tempDir);

		return gzipPath;
	}

	@GET
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_LOGS)
	@Produces(MediaType.APPLICATION_XML)
	public Response getLogsXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @QueryParam(QUERY_PARAM_BRICK_NAME) String brickName,
			@QueryParam(QUERY_PARAM_LOG_SEVERITY) String severity,
			@QueryParam(QUERY_PARAM_FROM_TIMESTAMP) String fromTimestamp,
			@QueryParam(QUERY_PARAM_TO_TIMESTAMP) String toTimestamp,
			@QueryParam(QUERY_PARAM_LINE_COUNT) Integer lineCount, @QueryParam(QUERY_PARAM_DOWNLOAD) Boolean download) {
		return getLogs(clusterName, volumeName, brickName, severity, fromTimestamp, toTimestamp, lineCount, MediaType.APPLICATION_XML);
	}

	@GET
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_LOGS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLogsJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @QueryParam(QUERY_PARAM_BRICK_NAME) String brickName,
			@QueryParam(QUERY_PARAM_LOG_SEVERITY) String severity,
			@QueryParam(QUERY_PARAM_FROM_TIMESTAMP) String fromTimestamp,
			@QueryParam(QUERY_PARAM_TO_TIMESTAMP) String toTimestamp,
			@QueryParam(QUERY_PARAM_LINE_COUNT) Integer lineCount, @QueryParam(QUERY_PARAM_DOWNLOAD) Boolean download) {
		return getLogs(clusterName, volumeName, brickName, severity, fromTimestamp, toTimestamp, lineCount, MediaType.APPLICATION_JSON);
	}

	public Response getLogs(String clusterName, String volumeName, String brickName, String severity,
			String fromTimestamp, String toTimestamp, Integer lineCount, String mediaType) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}
		
		if (volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}

		List<VolumeLogMessage> logMessages = null;
		Volume volume = null;
		try {
			volume = (Volume) getVolume(clusterName, volumeName);
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}

		if (brickName == null || brickName.isEmpty() || brickName.equals(CoreConstants.ALL)) {
			logMessages = getLogsForAllBricks(volume, lineCount);
		} else {
			// fetch logs for given brick of the volume
			for (Brick brick : volume.getBricks()) {
				if (brick.getQualifiedName().equals(brickName)) {
					logMessages = getBrickLogs(volume, brick, lineCount);
					break;
				}
			}
		}

		filterLogsBySeverity(logMessages, severity);
		filterLogsByTime(logMessages, fromTimestamp, toTimestamp);
		
		return okResponse(new LogMessageListResponse(logMessages), mediaType);
	}

	private void filterLogsByTime(List<VolumeLogMessage> logMessages, String fromTimestamp, String toTimestamp) {
		Date fromTime = null, toTime = null;

		if (fromTimestamp != null && !fromTimestamp.isEmpty()) {
			fromTime = DateUtil.stringToDate(fromTimestamp);
		}

		if (toTimestamp != null && !toTimestamp.isEmpty()) {
			toTime = DateUtil.stringToDate(toTimestamp);
		}

		List<VolumeLogMessage> messagesToRemove = new ArrayList<VolumeLogMessage>();
		for (VolumeLogMessage logMessage : logMessages) {
			Date logTimestamp = logMessage.getTimestamp();
			if (fromTime != null && logTimestamp.before(fromTime)) {
				messagesToRemove.add(logMessage);
				continue;
			}

			if (toTime != null && logTimestamp.after(toTime)) {
				messagesToRemove.add(logMessage);
			}
		}
		logMessages.removeAll(messagesToRemove);
	}

	private void filterLogsBySeverity(List<VolumeLogMessage> logMessages, String severity) {
		if (severity == null || severity.isEmpty()) {
			return;
		}

		List<VolumeLogMessage> messagesToRemove = new ArrayList<VolumeLogMessage>();
		for (VolumeLogMessage logMessage : logMessages) {
			if (!logMessage.getSeverity().equals(severity)) {
				messagesToRemove.add(logMessage);
			}
		}
		logMessages.removeAll(messagesToRemove);
	}

	private List<VolumeLogMessage> getLogsForAllBricks(Volume volume, Integer lineCount) {
		List<VolumeLogMessage> logMessages;
		logMessages = new ArrayList<VolumeLogMessage>();
		// fetch logs for every brick of the volume
		for (Brick brick : volume.getBricks()) {
			logMessages.addAll(getBrickLogs(volume, brick, lineCount));
		}

		// Sort the log messages based on log timestamp
		Collections.sort(logMessages, new Comparator<VolumeLogMessage>() {
			@Override
			public int compare(VolumeLogMessage message1, VolumeLogMessage message2) {
				return message1.getTimestamp().compareTo(message2.getTimestamp());
			}
		});

		return logMessages;
	}

	@POST
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_BRICKS)
	public Response addBricks(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @FormParam(FORM_PARAM_BRICKS) String bricks) {
		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}
		
		if (volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}

		if (bricks == null || bricks.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_BRICKS + "] is missing in request!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}
		
		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			return errorResponse("No online servers found in cluster [" + clusterName + "]");
		}

		List<String> brickList = Arrays.asList(bricks.split(",")); 
		try {
			glusterUtil.addBricks(volumeName, brickList, onlineServer.getName());
		} catch (ConnectionException e) {
			// online server has gone offline! try with a different one.
			onlineServer = clusterService.getNewOnlineServer(clusterName);
			if (onlineServer == null) {
				return errorResponse("No online servers found in cluster [" + clusterName + "]");
			}
			
			try {
				glusterUtil.addBricks(volumeName, brickList, onlineServer.getName());
			} catch(Exception e1) {
				return errorResponse(e1.getMessage());
			}
		} catch(Exception e1) {
			return errorResponse(e1.getMessage());
		}
		
		return createdResponse("");
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_BRICKS)
	public Response migrateBrick(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @FormParam(FORM_PARAM_SOURCE) String fromBrick,
			@FormParam(FORM_PARAM_TARGET) String toBrick, @FormParam(FORM_PARAM_AUTO_COMMIT) Boolean autoCommit) {

		if (clusterName == null || clusterName.isEmpty()) {
			return badRequestResponse("Cluster name must not be empty!");
		}
		
		if (volumeName == null || volumeName.isEmpty()) {
			return badRequestResponse("Volume name must not be empty!");
		}

		if (fromBrick == null || fromBrick.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_SOURCE + "] is missing in request!");
		}
		
		if (toBrick == null || toBrick.isEmpty()) {
			return badRequestResponse("Parameter [" + FORM_PARAM_TARGET + "] is missing in request!");
		}
		
		if (clusterService.getCluster(clusterName) == null) {
			return notFoundResponse("Cluster [" + clusterName + "] not found!");
		}

		GlusterServer onlineServer = clusterService.getOnlineServer(clusterName);
		if (onlineServer == null) {
			return errorResponse("No online servers found in cluster [" + clusterName + "]");
		}
		
		if(autoCommit == null) {
			autoCommit = false;
		}
		
		String taskId = null;
		try {
			taskId = migrateBrickStart(clusterName, volumeName, fromBrick, toBrick, autoCommit);
		}catch(Exception e) {
			return errorResponse(e.getMessage());
		}
		
		return acceptedResponse(RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_TASKS + "/"
				+ taskId);
	}
	
	private String migrateBrickStart(String clusterName, String volumeName, String fromBrick, String toBrick,
			Boolean autoCommit) {
		MigrateBrickTask migrateDiskTask = new MigrateBrickTask(clusterService, clusterName, volumeName, fromBrick,
				toBrick);
		migrateDiskTask.setAutoCommit(autoCommit);
		migrateDiskTask.start();
		taskResource.addTask(clusterName, migrateDiskTask);
		return migrateDiskTask.getTaskInfo().getName(); // Return Task ID
	}
	
	private String getLayout(Boolean isFixLayout, Boolean isMigrateData,
			Boolean isForcedDataMigrate) {
		String layout = "";
		if (isForcedDataMigrate) {
			layout = "forced-data-migrate";
		} else if (isMigrateData) {
			layout = "migrate-data";
		} else if (isFixLayout) {
			layout = "fix-layout";
		}
		return layout;
	}
	
	private String rebalanceStart(String clusterName, String volumeName, Boolean isFixLayout, Boolean isMigrateData,
			Boolean isForcedDataMigrate) {
		RebalanceVolumeTask rebalanceTask = new RebalanceVolumeTask(clusterService, clusterName, volumeName, getLayout(
				isFixLayout, isMigrateData, isForcedDataMigrate));
		rebalanceTask.start();
		taskResource.addTask(clusterName, rebalanceTask);
		return rebalanceTask.getId();
	}
	
	public void rebalanceStop(String clusterName, String volumeName) {
		// TODO: arrive at the task id and fetch it
		String taskId = "";
		
		taskResource.getTask(clusterName, taskId).stop();
	}
}
