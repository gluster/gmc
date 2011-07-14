/**
 * KeysResource.java
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
package com.gluster.storage.management.server.resources.v1_0;

import static com.gluster.storage.management.core.constants.RESTConstants.RESOURCE_PATH_KEYS;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.gluster.storage.management.core.utils.FileUtil;
import com.gluster.storage.management.core.utils.ProcessUtil;
import com.gluster.storage.management.server.utils.SshUtil;

@Path(RESOURCE_PATH_KEYS)
public class KeysResource extends AbstractResource {

	
	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportSshkeys() {
		try {
			StreamingOutput output = new StreamingOutput() {

				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					try {
						File archiveFile = new File(createSskKeyZipFile());
						output.write(FileUtil.readFileAsByteArray(archiveFile));
						archiveFile.delete();
					} catch (Exception e) {
						output.write(("Exception while archiving SSH Key files : " + e.getMessage()).getBytes());
					}
				}
			};
			return streamingOutputResponse(output);
		} catch (Exception e) {
			return errorResponse("Exporting SSH keys failed! [" + e.getMessage() + "]");
		}
	}

	public String createSskKeyZipFile() {
		String targetDir = System.getProperty("java.io.tmpdir");
		String zipFile = targetDir + "ssh-keys.tar";
		String sourcePemFile = SshUtil.PEM_FILE.getAbsolutePath();
		String sourcePubKeyFile = SshUtil.PUBLIC_KEY_FILE.getAbsolutePath();
		String targetPemFile = targetDir + File.separator + SshUtil.PEM_FILE.getName();
		String targetPubKeyFile = targetDir + File.separator + SshUtil.PUBLIC_KEY_FILE.getName();
		ProcessUtil processUtil = new ProcessUtil();

		// Copy keys to temp folder
		processUtil.executeCommand("cp", sourcePemFile, targetPemFile);
		processUtil.executeCommand("cp", sourcePubKeyFile, targetPubKeyFile);

		// To zip the key files
		processUtil.executeCommand("tar", "cvf", zipFile, "-C", "/tmp", SshUtil.PEM_FILE.getName(), SshUtil.PUBLIC_KEY_FILE.getName());

		// To remove the copied key files
		processUtil.executeCommand("rm", "-f", targetPubKeyFile, targetPubKeyFile);

		return zipFile;
	}
	
	
	

	public static void main(String[] args) {
		KeysResource key = new KeysResource();
		// key.exportSshkeys();
		System.out.println(System.getProperty("java.io.tmpdir"));
	}
}
