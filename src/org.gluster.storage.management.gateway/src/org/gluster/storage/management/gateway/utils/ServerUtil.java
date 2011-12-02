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
package org.gluster.storage.management.gateway.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.exceptions.ConnectionException;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.model.Server;
import org.gluster.storage.management.core.model.Server.SERVER_STATUS;
import org.gluster.storage.management.core.model.Status;
import org.gluster.storage.management.core.utils.ProcessResult;
import org.gluster.storage.management.core.utils.ProcessUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;


@Component
public class ServerUtil {
	@Autowired
	ServletContext servletContext;

	@Autowired
	private SshUtil sshUtil;
	
	@Autowired
	private String appVersion;
	
	private static final Logger logger = Logger.getLogger(ServerUtil.class);

	private static final String SCRIPT_DIR = "scripts";
	private static final String SCRIPT_COMMAND = "python";
	private static final String REMOTE_SCRIPT_GET_DISK_FOR_DIR = "get_disk_for_dir.py";
	private static final String REMOTE_SCRIPT_GET_SERVER_DETAILS = "get_server_details.py";
	private static final String REMOTE_SCRIPT_GET_FILE_SYSTEM_TYPE = "get_filesystem_type.py";
	private static final String REMOTE_SCRIPT_BASE_DIR = "/opt/glustermg";
	private static final String REMOTE_SCRIPT_DIR_NAME = "backend";

	public void setSshUtil(SshUtil sshUtil) {
		this.sshUtil = sshUtil;
	}

	public ProcessResult executeGlusterScript(boolean runInForeground, String scriptName, String...arguments) {
		return executeGlusterScript(runInForeground, scriptName, Arrays.asList(arguments));
	}
	
	public ProcessResult executeGlusterScript(boolean runInForeground, String scriptName, List<String> arguments) {
		List<String> command = new ArrayList<String>();

		command.add(SCRIPT_COMMAND);
		command.add(getScriptPath(scriptName));
		command.addAll(arguments);
		return ProcessUtil.executeCommand(runInForeground, command);
	}

	private String getScriptPath(String scriptName) {
		return servletContext.getRealPath(SCRIPT_DIR) + CoreConstants.FILE_SEPARATOR + scriptName;
	}
	
	private String getRemoteScriptDir() {
		return REMOTE_SCRIPT_BASE_DIR + File.separator + appVersion + File.separator + REMOTE_SCRIPT_DIR_NAME;
	}
	
	/**
	 * Fetch details of the given server. The server name must be populated in the object before calling this method.
	 * 
	 * @param server
	 *            Server whose details are to be fetched
	 */
	public void fetchServerDetails(Server server) {
		try {
			Server serverDetails = fetchServerDetails(server.getName());
			server.copyFrom(serverDetails); // Update the details in <Server> object
			server.setDisks(serverDetails.getDisks());
		} catch (ConnectionException e) {
			logger.warn("Couldn't connect to server [" + server.getName() + "]. Marking it offline!", e);
			server.setStatus(SERVER_STATUS.OFFLINE);
		}
	}
	
	public boolean isServerOnline(Server server) {
		// fetch latest details and check if server is still online
		fetchServerDetails(server);
		return server.isOnline();
	}
	
	public String fetchHostName(String serverName) {
		Object response = fetchServerDetails(serverName);
		return ((Server) response).getName();
	}

	private Server fetchServerDetails(String serverName) {
		// fetch standard server details like cpu, disk, memory details
		return executeScriptOnServer(serverName, REMOTE_SCRIPT_GET_SERVER_DETAILS, Server.class);
	}

	/**
	 * Executes given script on all given servers in parallel, collects the output in objects of given class, and
	 * returns a list of all returned objects.
	 * 
	 * @param serverNames
	 * @param scriptWithArgs
	 * @param expectedClass
	 * @param failOnError
	 *            If true, an exception will be thrown as soon as the script execution fails on any of the servers. If
	 *            false, the exception will be caught and logged. Execution on all other servers will continue.
	 * @return
	 */
	public <T> List<T> executeScriptOnServers(List<String> serverNames, String scriptWithArgs,
			Class<T> expectedClass, boolean failOnError) {
		List<T> result = Collections.synchronizedList(new ArrayList<T>());
		try {
			List<Thread> threads = createScriptExecutionThreads(serverNames, getRemoteScriptDir() + File.separator
					+ scriptWithArgs, expectedClass, result, failOnError);
			ProcessUtil.waitForThreads(threads);
			return result;
		} catch (InterruptedException e) {
			String errMsg = "Exception while executing script [" + scriptWithArgs + "] on servers [" + serverNames + "]! Error: [" + e.getMessage() + "]";
			logger.error(errMsg, e);
			throw new GlusterRuntimeException(errMsg, e);
		}
	}

	/**
	 * Creates threads that will run in parallel and execute the given command on each of the given servers
	 * 
	 * @param serverNames
	 * @param commandWithArgs
	 * @param expectedClass
	 * @param result
	 * @param failOnError
	 *            If true, an exception will be thrown as soon as the script execution fails on any of the servers. If
	 *            false, the exception will be caught and logged. Execution on all other servers will continue.
	 * @return
	 * @throws InterruptedException
	 */
	private <T> List<Thread> createScriptExecutionThreads(List<String> serverNames, String commandWithArgs, Class<T> expectedClass, List<T> result,
			boolean failOnError)
			throws InterruptedException {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = serverNames.size()-1; i >= 0 ; i--) {
			Thread thread = new RemoteExecutionThread<T>(serverNames.get(i), commandWithArgs, expectedClass, result, failOnError);
			threads.add(thread);
			thread.start();
			if(i >= 5 && i % 5 == 0) {
				// After every 5 servers, wait for 1 second so that we don't end up with too many running threads
				Thread.sleep(1000);
			}
		}
		return threads;
	}


	public class RemoteExecutionThread<T> extends Thread {
		private String serverName;
		private String commandWithArgs;
		private List<T> result;
		private Class<T> expectedClass;
		private boolean failOnError = false;
		
		public RemoteExecutionThread(String serverName, String commandWithArgs, Class<T> expectedClass, List<T> result,
				boolean failOnError) {
			this.serverName = serverName;
			this.commandWithArgs = commandWithArgs;
			this.result = result;
			this.expectedClass = expectedClass;
			this.failOnError = failOnError;
		}
		
		@Override
		public void run() {
			try {
				result.add(executeOnServer(serverName, commandWithArgs, expectedClass));
			} catch(Exception e) {
				String errMsg = "Couldn't execute command [" + commandWithArgs + "] on [" + serverName + "]!";
				logger.error(errMsg, e);
				if(failOnError) {
					throw new GlusterRuntimeException(errMsg, e);
				}
			}
		}
	}


	/**
	 * Executes given script on given server. Since the remote server may contain multiple versions of backend, this
	 * method will invoke the script present in directory of same version as the gateway.
	 * 
	 * @param serverName
	 * @param scriptWithArgs
	 *            The script name followed by arguments to be passed. Note that the script name should not contain path
	 *            as it will be automatically identified by the method.
	 * @param expectedClass
	 *            Class of the object expected from script execution
	 * @return Output (console/error) from the script execution
	 * @throws GlusterRuntimeException in case the remote execution fails.
	 */
	public String executeScriptOnServer(String serverName, String scriptWithArgs) {
		return executeOnServer(serverName, getRemoteScriptDir() + File.separator + scriptWithArgs, String.class);
	}

	/**
	 * Executes given script on given server. Since the remote server may contain multiple versions of backend, this
	 * method will invoke the script present in directory of same version as the gateway.
	 * 
	 * @param serverName
	 * @param scriptWithArgs
	 *            The script name followed by arguments to be passed. Note that the script name should not contain path
	 *            as it will be automatically identified by the method.
	 * @param expectedClass
	 *            Class of the object expected from script execution
	 * @return Object of the expected class from remote execution of the command.
	 * @throws GlusterRuntimeException in case the remote execution fails.
	 */
	public <T> T executeScriptOnServer(String serverName, String scriptWithArgs,
			Class<T> expectedClass) {
		return executeOnServer(serverName, getRemoteScriptDir() + File.separator + scriptWithArgs,
				expectedClass);
	}
	
	/**
	 * Executes given command on given server
	 * 
	 * @param serverName
	 * @param commandWithArgs
	 * @param expectedClass
	 *            Class of the object expected from script execution
	 * @return Object of the expected class from remote execution of the command. In case the remote execution fails
	 *         ungracefully, an object of class {@link Status} will be returned.
	 */
	@SuppressWarnings("unchecked")
	public <T> T executeOnServer(String serverName, String commandWithArgs,
			Class<T> expectedClass) {
		String output = executeOnServer(serverName, commandWithArgs);
		if (expectedClass == String.class) {
			return (T) output;
		}

		return unmarshal(expectedClass, output);
	}

	public String executeOnServer(String serverName, String commandWithArgs) {
		ProcessResult result = sshUtil.executeRemote(serverName, commandWithArgs);
		
		if (!result.isSuccess()) {
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

	public void getFileFromServer(String serverName, String remoteFileName, String localDirName) {
		sshUtil.getFile(serverName, remoteFileName, localDirName);
	}

	/**
	 * Unmarshals given input string into object of given class
	 * 
	 * @param expectedClass
	 *            Class whose object is expected
	 * @param input
	 *            Input string
	 * @return Object of given expected class
	 */
	@SuppressWarnings("unchecked")
	public <T> T unmarshal(Class<T> expectedClass, String input) {
		try {
			// create JAXB context and instantiate marshaller
			JAXBContext context = JAXBContext.newInstance(expectedClass);
			Unmarshaller um = context.createUnmarshaller();
			return (T)um.unmarshal(new ByteArrayInputStream(input.getBytes()));
		} catch (JAXBException e) {
			String errMsg = "Error during unmarshalling string [" + input + "] for class [" + expectedClass.getName()
					+ ": [" + e.getMessage() + "]";
			logger.error(errMsg, e);
			throw new GlusterRuntimeException(errMsg, e);
		}
	}

	/**
	 * @param serverName
	 *            Server on which the directory is present
	 * @param brickDir
	 *            Directory whose disk is to be fetched
	 * @return Status object containing the disk name, or error message in case the remote script fails.
	 */
	public Status getDiskForDir(String serverName, String brickDir) {
		return executeScriptOnServer(serverName, REMOTE_SCRIPT_GET_DISK_FOR_DIR + " " + brickDir, Status.class);
	}
	
	public <T> T getBean(Class<T> clazz) {
		ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
		return ctx.getBean(clazz);
	}
	
	public List<String> getFsTypes(String serverName) {
		String output = executeScriptOnServer(serverName, REMOTE_SCRIPT_GET_FILE_SYSTEM_TYPE);
		return Arrays.asList(output.trim().split(CoreConstants.NEWLINE));
	}
}
