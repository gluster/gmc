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
import java.io.InputStream;
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
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.response.GenericResponse;
import com.gluster.storage.management.core.utils.ProcessResult;
import com.gluster.storage.management.core.utils.ProcessUtil;

@Component
public class ServerUtil {
	@Autowired
	ServletContext servletContext;
	
	@Autowired
	private SshUtil sshUtil;

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
	 * @param expectedClass
	 *            Class of the object expected from script execution
	 * @return Object of the expected class from remote execution of the command. In case the remote execution fails
	 *         ungracefully, an object of class {@link Status} will be returned.
	 */
	@SuppressWarnings("rawtypes")
	public Object executeOnServer(boolean runInForeground, String serverName, String commandWithArgs,
			Class expectedClass) {
		try {
			String output = executeOnServer(serverName, commandWithArgs);
System.out.println(output);
			// In case the script execution exits ungracefully, the agent would return a GenericResponse.
			// hence pass last argument as true to try GenericResponse unmarshalling in such cases.
			Object response = unmarshal(expectedClass, output, expectedClass != GenericResponse.class);
			if (expectedClass != GenericResponse.class && response instanceof GenericResponse) {
				// expected class was not GenericResponse, but that's what we got. This means the
				// script failed ungracefully. Extract and return the status object from the response
				return ((GenericResponse) response).getStatus();
			}
			return response;
		} catch (Exception e) {
			// any other exception means unexpected error. return status with error from exception.
			return new Status(e);
		}
	}

	private String executeOnServer(String serverName, String commandWithArgs) {
		ProcessResult result = sshUtil.executeRemote(serverName, commandWithArgs);
		if(!result.isSuccess()) {
			throw new GlusterRuntimeException("Command [" + commandWithArgs + "] failed on [" + serverName
					+ "] with error [" + result.getExitValue() + "][" + result.getOutput() + "]");
		}
		return result.getOutput();
	}
	
	// This is the old executeOnServer that used socket communication.
	// We can keep it commented for the time being.
	// private String executeOnServerUsingSocket(String serverName, String commandWithArgs) {
	// try {
	// InetAddress address = InetAddress.getByName(serverName);
	// Socket connection = new Socket(address, 50000);
	//
	// PrintWriter writer = new PrintWriter(connection.getOutputStream(), true);
	// writer.println(commandWithArgs);
	// writer.println(); // empty line means end of request
	//
	// InputStream inputStream = connection.getInputStream();
	// int available = inputStream.available();
	//
	// StringBuffer output = new StringBuffer();
	// if( available > 0 ) {
	// // This happens when PeerAgent sends complete file
	// byte[] responseData = new byte[available];
	// inputStream.read(responseData);
	// output.append(new String(responseData, "UTF-8"));
	// } else {
	// // This happens in case of normal XML response from PeerAgent
	// BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	//
	// String line;
	// while (!(line = reader.readLine()).trim().isEmpty()) {
	// output.append(line + CoreConstants.NEWLINE);
	// }
	// }
	// connection.close();
	//
	// return output.toString();
	// } catch (Exception e) {
	// throw new GlusterRuntimeException("Error during remote execution: [" + e.getMessage() + "]");
	// }
	// }
	
	public String getFileFromServer(String serverName, String fileName) {
		return executeOnServer(serverName, "get_file " + fileName); 
	}

	/**
	 * Unmarshals given input string into object of given class
	 * 
	 * @param expectedClass
	 *            Class whose object is expected
	 * @param input
	 *            Input string
	 * @param tryGenericResponseOnFailure
	 *            If true, and if the unmarshalling fails for given class, another unmarshalling will be attempted with
	 *            class {@link GenericResponse}. If this also fails, a status object with exception message is created
	 *            and returned.
	 * @return Object of given expected class, or a status object in case first unmarshalling fails.
	 */
	@SuppressWarnings("rawtypes")
	private Object unmarshal(Class expectedClass, String input, boolean tryGenericResponseOnFailure) {
		try {
			// create JAXB context and instantiate marshaller
			JAXBContext context = JAXBContext.newInstance(expectedClass);
			Unmarshaller um = context.createUnmarshaller();
			return um.unmarshal(new ByteArrayInputStream(input.getBytes()));
		} catch (JAXBException e) {
			if(tryGenericResponseOnFailure) {
				// unmarshalling failed. try to unmarshal a GenericResponse object
				return unmarshal(GenericResponse.class, input, false);
			}
			
			return new Status(Status.STATUS_CODE_FAILURE, "Error during unmarshalling string [" + input
					+ "] for class [" + expectedClass.getName() + ": [" + e.getMessage() + "]");
		}
	}

	public static void main(String args[]) throws Exception {
		// CreateVolumeExportDirectory.py md0 testvol
		System.out.println(new ServerUtil().getFileFromServer("localhost", "/tmp/python/PeerAgent.py"));
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
