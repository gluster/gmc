/*******************************************************************************
 * Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Gateway.
 *
 * Gluster Management Gateway is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Gluster Management Gateway is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.gluster.storage.management.gateway.resources.v1_0;

import static org.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_KEYS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.utils.FileUtil;
import org.gluster.storage.management.core.utils.ProcessResult;
import org.gluster.storage.management.core.utils.ProcessUtil;
import org.gluster.storage.management.gateway.utils.SshUtil;

import com.sun.jersey.multipart.FormDataParam;

@Path(RESOURCE_PATH_KEYS)
public class KeysResource extends AbstractResource {
	private static final Logger logger = Logger.getLogger(KeysResource.class);

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportSshkeys() {
		File archiveFile = new File(createSskKeyZipFile());
		byte[] data = FileUtil.readFileAsByteArray(archiveFile);
		archiveFile.delete();
		return streamingOutputResponse(createStreamingOutput(data));
	}
	
	private String createSskKeyZipFile() {
		String targetDir = FileUtil.getTempDirName();
		String zipFile = targetDir + File.separator + "ssh-keys.tar";
		String sourcePrivateKeyFile = SshUtil.PRIVATE_KEY_FILE.getAbsolutePath();
		String sourcePublicKeyFile = SshUtil.PUBLIC_KEY_FILE.getAbsolutePath();
		String targetPrivateKeyFile = targetDir + File.separator + SshUtil.PRIVATE_KEY_FILE.getName();
		String targetPubKeyFile = targetDir + File.separator + SshUtil.PUBLIC_KEY_FILE.getName();

		if (!SshUtil.PRIVATE_KEY_FILE.isFile()) {
			throw new GlusterRuntimeException("No private key file [" + SshUtil.PRIVATE_KEY_FILE.getName() + "] found!");
		}
		
		if (!SshUtil.PUBLIC_KEY_FILE.isFile()) {
			throw new GlusterRuntimeException("No public key file [" + SshUtil.PUBLIC_KEY_FILE.getName() + "] found!");
		}
		
		// Copy keys to temp folder
		ProcessResult result = ProcessUtil.executeCommand("cp", sourcePrivateKeyFile, targetPrivateKeyFile);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Failed to copy key files! [" + result.getOutput() + "]");
		}
		result = ProcessUtil.executeCommand("cp", sourcePublicKeyFile, targetPubKeyFile);
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Failed to copy key files! [" + result.getOutput() + "]");
		}
		
		// To compress the key files
		result = ProcessUtil.executeCommand("tar", "cvf", zipFile, "-C", targetDir, SshUtil.PRIVATE_KEY_FILE.getName(),
				SshUtil.PUBLIC_KEY_FILE.getName());
		if (!result.isSuccess()) {
			throw new GlusterRuntimeException("Failed to compress key files! [" + result.getOutput() + "]");
		}

		// To remove the copied key files
		try {
			ProcessUtil.executeCommand("rm", "-f", targetPrivateKeyFile, targetPubKeyFile); // Ignore the errors if any
		} catch (Exception e) {
			logger.warn(e.toString());
		}
		return zipFile;
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response importSshKeys(@FormDataParam("file") InputStream uploadedInputStream) {
		File uploadedFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "keys.tar");
		String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		writeToFile(uploadedInputStream, uploadedFile.getAbsolutePath());

		// To backup existing SSH private and public keys, if exist.
		if (SshUtil.PRIVATE_KEY_FILE.isFile()) {
			if (!SshUtil.PRIVATE_KEY_FILE.renameTo(new File(SshUtil.PRIVATE_KEY_FILE.getAbsolutePath() + "-" + timestamp))) {
				throw new GlusterRuntimeException("Unable to backup private key!");
			}
		}

		if (SshUtil.PUBLIC_KEY_FILE.isFile()) {
			if (!SshUtil.PUBLIC_KEY_FILE
					.renameTo(new File(SshUtil.PUBLIC_KEY_FILE.getAbsolutePath() + "-" + timestamp))) {
				throw new GlusterRuntimeException("Unable to backup public key!");
			}
		}
		// Extract SSH private and public key files.
		ProcessResult output = ProcessUtil.executeCommand("tar", "xvf", uploadedFile.getAbsolutePath(), "-C",
				SshUtil.SSH_AUTHORIZED_KEYS_DIR_LOCAL);
		uploadedFile.delete();
		if (!output.isSuccess()) {
			String errMsg = "Error in importing SSH keys: [" + output.toString() + "]";
			logger.error(errMsg);
			throw new GlusterRuntimeException(errMsg);
		}
		return createdResponse("SSH Key imported successfully");
	}

	// save uploaded file to the file (with path)
	private void writeToFile(InputStream inputStream, String toFile) {
		try {
			int read = 0;
			byte[] bytes = new byte[1024];

			OutputStream out = new FileOutputStream(new File(toFile));
			while ((read = inputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new GlusterRuntimeException(e.getMessage());
		}
	}
}
