/**
 * ServerUtil.java
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
package com.gluster.storage.management.server.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;

@Component
public class ServerUtil {
	@Autowired
	ServletContext servletContext;

	private static final String SCRIPT_DIR = "scripts";
	private static final String SCRIPT_COMMAND = "python";
	private static final String REMOTE_SCRIPT_GET_DISK_FOR_DIR = "get_disk_for_dir.py";

	public ProcessResult executeGlusterScript(boolean runInForeground, String scriptName, List<String> arguments) {
		List<String> command = new ArrayList<String>();

		command.add(SCRIPT_COMMAND);
		command.add(getScriptPath(scriptName));
		command.addAll(arguments);
		return new ProcessUtil().executeCommand(runInForeground, command);
	}

	private String getScriptPath(String scriptName) {
		String scriptPath = servletContext.getRealPath(SCRIPT_DIR) + CoreConstants.FILE_SEPARATOR + scriptName;
		return scriptPath;
	}

	/**
	 * Executes given command on given server
	 * 
	 * @param runInForeground
	 * @param serverName
	 * @param commandWithArgs
	 * @param expectedClass Class of the object expected from script execution 
	 * @return Response from remote execution of the command
	 */
	@SuppressWarnings("rawtypes")
	public Object executeOnServer(boolean runInForeground, String serverName, String commandWithArgs, Class expectedClass) {
		StringBuffer output = new StringBuffer();
		try {
			InetAddress address = InetAddress.getByName(serverName);
			Socket connection = new Socket(address, 50000);

			PrintWriter writer = new PrintWriter(connection.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

			writer.println(commandWithArgs);
			writer.println(); // empty line means end of request

			String line;
			while (!(line = reader.readLine()).trim().isEmpty()) {
				output.append(line + CoreConstants.NEWLINE);
			}

			connection.close();
			return unmarshal(expectedClass, output.toString(), expectedClass != Status.class);
		} catch(Exception e) {
			// any other exception means unexpected error. return status with error from exception.
			return new Status(Status.STATUS_CODE_FAILURE, "Error during remote execution: [" + e.getMessage() + "]");
		}
	}

	/**
	 * Unmarshals given input string into object of given class
	 * 
	 * @param expectedClass
	 *            Class whose object is expected
	 * @param input
	 *            Input string
	 * @param tryStatusOnFailure
	 *            If true, and if the unmarshalling fails for given class, another unmarshalling will be attempted with
	 *            class Status. If that also fails, a status object with exception message is created and returned.
	 * @return Object of given expected class, or a status object in case first unmarshalling fails.
	 */
	@SuppressWarnings("rawtypes")
	private Object unmarshal(Class expectedClass, String input, boolean tryStatusOnFailure) {
		try {
			// create JAXB context and instantiate marshaller
			JAXBContext context = JAXBContext.newInstance(expectedClass);
			Unmarshaller um = context.createUnmarshaller();
			return um.unmarshal(new ByteArrayInputStream(input.getBytes()));
		} catch (JAXBException e) {
			if(tryStatusOnFailure) {
				// unmarshalling failed. try to unmarshal a Status object
				return unmarshal(Status.class, input, false);
			}
			
			return new Status(Status.STATUS_CODE_FAILURE, "Error during unmarshalling string [" + input
					+ "] for class [" + expectedClass.getName() + ": [" + e.getMessage() + "]");
		}
	}

	public static void main(String args[]) {
		// CreateVolumeExportDirectory.py md0 testvol
		System.out.println(new ServerUtil().executeOnServer(true, "localhost", "python CreateVolumeExportDirectory.py md0 testvol", Status.class));
	}

	/**
	 * @param serverName Server on which the directory is present
	 * @param brickDir Directory whose disk is to be fetched
	 * @return Status object containing the disk name, or error message in case the remote script fails.
	 */
	public Status getDiskForDir(String serverName, String brickDir) {
		return (Status) executeOnServer(true, serverName, REMOTE_SCRIPT_GET_DISK_FOR_DIR + " " + brickDir, Status.class);
	}
}