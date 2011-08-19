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
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_MAX_COUNT;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_NEXT_TO;
import static com.gluster.storage.management.core.constants.RESTConstants.QUERY_PARAM_TO_TIMESTAMP;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_BRICKS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_DEFAULT_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_DOWNLOAD;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_LOGS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_OPTIONS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_CLUSTERS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_TASKS;
import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_VOLUMES;

import java.io.File;
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

import com.gluster.storage.management.core.constants.RESTConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.exceptions.GlusterValidationException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.Volume;
import com.gluster.storage.management.core.model.Volume.VOLUME_STATUS;
import com.gluster.storage.management.core.model.VolumeLogMessage;
import com.gluster.storage.management.core.response.LogMessageListResponse;
import com.gluster.storage.management.core.response.VolumeListResponse;
import com.gluster.storage.management.core.response.VolumeOptionInfoListResponse;
import com.gluster.storage.management.core.utils.FileUtil;
import com.gluster.storage.management.gateway.constants.VolumeOptionsDefaults;
import com.gluster.storage.management.gateway.services.ClusterService;
import com.gluster.storage.management.gateway.services.VolumeService;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path(RESOURCE_PATH_CLUSTERS + "/{" + PATH_PARAM_CLUSTER_NAME + "}/" + RESOURCE_VOLUMES)
public class VolumesResource extends AbstractResource {
	private static final Logger logger = Logger.getLogger(VolumesResource.class);

	@InjectParam
	private ClusterService clusterService;

	@InjectParam
	private VolumeOptionsDefaults volumeOptionsDefaults;

	@InjectParam
	private VolumeService volumeService;
	
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public Response getVolumesXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_MAX_COUNT) Integer maxCount,
			@QueryParam(QUERY_PARAM_NEXT_TO) String previousVolumeName) {
		return okResponse(new VolumeListResponse(volumeService.getVolumes(clusterName, maxCount, previousVolumeName)),
				MediaType.APPLICATION_XML);
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getVolumesJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@QueryParam(QUERY_PARAM_MAX_COUNT) Integer maxCount,
			@QueryParam(QUERY_PARAM_NEXT_TO) String previousVolumeName) {
		return okResponse(new VolumeListResponse(volumeService.getVolumes(clusterName, maxCount, previousVolumeName)),
				MediaType.APPLICATION_JSON);
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
			throw new GlusterValidationException("Parameter [" + missingParam + "] is missing in request!");
		}

		volumeService.createVolume(clusterName, volumeName, volumeType, transportType, replicaCount,
				stripeCount, bricks, accessProtocols, options, cifsUsers);
		return createdResponse(volumeName);
	}

	/**
	 * Returns name of the missing parameter if any. If all parameters are present, 
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
		return okResponse(volumeService.getVolume(clusterName, volumeName), MediaType.APPLICATION_XML);
	}

	@GET
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVolumeJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {
		return okResponse(volumeService.getVolume(clusterName, volumeName), MediaType.APPLICATION_JSON);
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
			throw new GlusterValidationException("Cluster name must not be empty!");
		}

		if (volumeName == null || volumeName.isEmpty()) {
			throw new GlusterValidationException("Volume name must not be empty!");
		}

		if (clusterService.getCluster(clusterName) == null) {
			throw new GlusterValidationException("Cluster [" + clusterName + "] not found!");
		}
		
		try {
			if (operation.equals(RESTConstants.TASK_REBALANCE_START)) {
				String taskId = volumeService.rebalanceStart(clusterName, volumeName, isFixLayout, isMigrateData, isForcedDataMigrate);
				return acceptedResponse(RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_TASKS
						+ "/" + taskId);
			} else if (operation.equals(RESTConstants.TASK_REBALANCE_STOP)) {
				volumeService.rebalanceStop(clusterName, volumeName);
			} else if (operation.equals(RESTConstants.FORM_PARAM_CIFS_CONFIG)) {
				if (enableCifs) {
					// After add/modify volume cifs users, start/restart the cifs service
					volumeService.createCIFSUsers(clusterName, volumeName, cifsUsers);
					volumeService.startCifsReExport(clusterName, volumeName);
				} else {
					// Stop the Cifs service and delete the users (!important)
					Volume newVolume = volumeService.getVolume(clusterName, volumeName);
					if (newVolume.getStatus() == VOLUME_STATUS.ONLINE) {
						volumeService.stopCifsReExport(clusterName, volumeName);
					}
					volumeService.deleteCifsUsers(clusterName, volumeName);
				}
			} else {
				volumeService.performVolumeOperation(clusterName, volumeName, operation);
			}
			return noContentResponse();
		} catch(Exception e) {
			return errorResponse(e.getMessage());
		}
	}
	
	@DELETE
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}")
	public Response deleteVolume(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName,
			@QueryParam(QUERY_PARAM_DELETE_OPTION) Boolean deleteFlag) {
		volumeService.deleteVolume(clusterName, volumeName, deleteFlag);
		
		return noContentResponse();
	}

	@DELETE
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_BRICKS)
	public Response removeBricks(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @QueryParam(QUERY_PARAM_BRICKS) String bricks,
			@QueryParam(QUERY_PARAM_DELETE_OPTION) Boolean deleteFlag) {
		volumeService.removeBricksFromVolume(clusterName, volumeName, bricks, deleteFlag);
		return noContentResponse();
	}

	@POST
	@Path("{" + PATH_PARAM_VOLUME_NAME + " }/" + RESOURCE_OPTIONS)
	public Response setOption(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName,
			@FormParam(RESTConstants.FORM_PARAM_OPTION_KEY) String key,
			@FormParam(RESTConstants.FORM_PARAM_OPTION_VALUE) String value) {
		volumeService.setVolumeOption(clusterName, volumeName, key, value);
		
		return createdResponse(key);
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + " }/" + RESOURCE_OPTIONS)
	public Response resetOptions(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName) {
		volumeService.resetVolumeOptions(clusterName, volumeName);
		return noContentResponse();
	}

	@GET
	@Path(RESOURCE_DEFAULT_OPTIONS)
	@Produces(MediaType.APPLICATION_XML)
	public VolumeOptionInfoListResponse getDefaultOptionsXML(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		return new VolumeOptionInfoListResponse(Status.STATUS_SUCCESS, volumeOptionsDefaults.getDefaults(clusterName));
	}

	@GET
	@Path(RESOURCE_DEFAULT_OPTIONS)
	@Produces(MediaType.APPLICATION_JSON)
	public VolumeOptionInfoListResponse getDefaultOptionsJSON(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName) {
		return new VolumeOptionInfoListResponse(Status.STATUS_SUCCESS, volumeOptionsDefaults.getDefaults(clusterName));
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
			final Volume volume = volumeService.getVolume(clusterName, volumeName);
			File archiveFile = new File(volumeService.downloadLogs(volume));
			byte[] data = FileUtil.readFileAsByteArray(archiveFile);
			archiveFile.delete();
			return streamingOutputResponse(createStreamingOutput(data));
		} catch (Exception e) {
			logger.error("Volume [" + volumeName + "] doesn't exist in cluster [" + clusterName + "]! ["
					+ e.getStackTrace() + "]");
			throw (GlusterRuntimeException) e;
		}
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

	private Response getLogs(String clusterName, String volumeName, String brickName, String severity,
			String fromTimestamp, String toTimestamp, Integer lineCount, String mediaType) {
		List<VolumeLogMessage> logMessages = volumeService.getLogs(clusterName, volumeName, brickName, severity,
				fromTimestamp, toTimestamp, lineCount);
		
		return okResponse(new LogMessageListResponse(logMessages), mediaType);
	}

	@POST
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_BRICKS)
	public Response addBricks(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @FormParam(FORM_PARAM_BRICKS) String bricks) {
		volumeService.addBricksToVolume(clusterName, volumeName, bricks);
		
		return createdResponse(volumeName + "/" + RESOURCE_BRICKS);
	}

	@PUT
	@Path("{" + PATH_PARAM_VOLUME_NAME + "}/" + RESOURCE_BRICKS)
	public Response migrateBrick(@PathParam(PATH_PARAM_CLUSTER_NAME) String clusterName,
			@PathParam(PATH_PARAM_VOLUME_NAME) String volumeName, @FormParam(FORM_PARAM_SOURCE) String fromBrick,
			@FormParam(FORM_PARAM_TARGET) String toBrick, @FormParam(FORM_PARAM_AUTO_COMMIT) Boolean autoCommit) {

		String taskId = volumeService.migrateBrickStart(clusterName, volumeName, fromBrick, toBrick, autoCommit);
		
		return acceptedResponse(RESTConstants.RESOURCE_PATH_CLUSTERS + "/" + clusterName + "/" + RESOURCE_TASKS + "/"
				+ taskId);
	}
}
