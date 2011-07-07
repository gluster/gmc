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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.Status;
import com.gluster.storage.management.core.model.VolumeOption;
import com.gluster.storage.management.core.model.VolumeOptions;
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

	public void setSshUtil(SshUtil sshUtil) {
		this.sshUtil = sshUtil;
	}

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
	 * Fetch details of the given server. The server name must be populated in the object before calling this method.
	 * 
	 * @param server
	 *            Server whose details are to be fetched
	 */
	public void fetchServerDetails(Server server) {
		// fetch standard server details like cpu, disk, memory details
		Object response = executeOnServer(true, server.getName(), "get_server_details.py", Server.class);
		if (response instanceof Status) {
			throw new GlusterRuntimeException(((Status)response).getMessage());
		}
		server.copyFrom((Server) response); // Update the details in <Server> object
		server.setDisks(((Server) response).getDisks());
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

			// In case the script execution exits ungracefully, the agent would return a GenericResponse.
			// hence pass last argument as true to try GenericResponse unmarshalling in such cases.
			Object response = unmarshal(expectedClass, output, expectedClass != GenericResponse.class);
			if (expectedClass != GenericResponse.class && response instanceof GenericResponse) {
				// expected class was not GenericResponse, but that's what we got. This means the
				// script failed ungracefully. Extract and return the status object from the response
				return ((GenericResponse) response).getStatus();
			}
			return response;
		} catch (RuntimeException e) {
			// Except for connection exception, wrap any other exception in the a object and return it.
			if (e instanceof ConnectionException) {
				throw e;
			} else {
				// error during unmarshalling. return status with error from exception.
				return new Status(e);
			}
		}
	}

	private String executeOnServer(String serverName, String commandWithArgs) {
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
			if (tryGenericResponseOnFailure) {
				// unmarshalling failed. try to unmarshal a GenericResponse object
				return unmarshal(GenericResponse.class, input, false);
			}

			return new Status(Status.STATUS_CODE_FAILURE, "Error during unmarshalling string [" + input
					+ "] for class [" + expectedClass.getName() + ": [" + e.getMessage() + "]");
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
		return (Status) executeOnServer(true, serverName, REMOTE_SCRIPT_GET_DISK_FOR_DIR + " " + brickDir, Status.class);
	}

	public static void main(String args[]) throws Exception {
		//String diskStr = "<server><name>devserver1</name><domainname/><dns1>10.1.10.1</dns1><dns2>8.8.4.4</dns2><networkInterfaces><networkInterface><name>eth0</name><hwAddr>00:50:56:82:00:1d</hwAddr><speed>1000</speed><model>ETHERNET</model><onboot>yes</onboot><bootProto>none</bootProto><ipAddress>10.1.12.41</ipAddress><netMask>255.255.255.0</netMask><defaultGateway>10.1.12.1</defaultGateway></networkInterface></networkInterfaces><numOfCPUs>2</numOfCPUs><cpuUsage>0.0</cpuUsage><totalMemory>2010.5234375</totalMemory><memoryInUse>1267.6015625</memoryInUse><status>ONLINE</status><uuid/><disks><disk><name>sdd</name><description>VMware Virtual disk</description><uuid/><status>UNINITIALIZED</status><init>false</init><type>false</type><interface>pci</interface><fsType/><fsVersion/><mountPoint/><size>10240</size><spaceInUse/><partitions><partition><name>sdd1</name><uuid/><status>UNINITIALIZED</status><init>false</init><type>false</type><interface/><fsType/><mountPoint/><size>10236</size><spaceInUse/></partition></partitions></disk><disk><name>sda</name><description>VMware Virtual disk</description><uuid/><status>UNINITIALIZED</status><init>false</init><type>false</type><interface>pci</interface><fsType/><fsVersion/><mountPoint/><size>10240</size><spaceInUse>2019</spaceInUse><partitions><partition><name>sda1</name><uuid>345d880e-822a-4e46-a518-75cc48b1869f</uuid><status>INITIALIZED</status><init>true</init><type>false</type><interface/><fsType>ext3</fsType><mountPoint>/boot</mountPoint><size>125</size><spaceInUse>11</spaceInUse></partition><partition><name>sda2</name><uuid/><status>UNINITIALIZED</status><init>false</init><type>false</type><interface/><fsType>swap</fsType><mountPoint/><size>125</size><spaceInUse/></partition><partition><name>sda3</name><uuid>f94a0b2a-5ebc-4c13-a618-0328af97a31e</uuid><status>INITIALIZED</status><init>true</init><type>false</type><interface/><fsType>ext3</fsType><mountPoint>/</mountPoint><size>9985</size><spaceInUse>2008</spaceInUse></partition></partitions></disk><disk><name>sdb</name><description>VMware Virtual disk</description><uuid>97ee7ea3-d235-424c-bdda-f5b697f204a2</uuid><status>READY</status><init>true</init><type>true</type><interface>pci</interface><fsType>ext3</fsType><fsVersion>1.0</fsVersion><mountPoint>/export/sdb</mountPoint><size>1024</size><spaceInUse>427</spaceInUse><partitions/></disk><disk><name>sdc</name><description>VMware Virtual disk</description><uuid>87679044-6395-42fb-a80d-41c3b648f248</uuid><status>READY</status><init>true</init><type>true</type><interface>pci</interface><fsType>ext3</fsType><fsVersion>1.0</fsVersion><mountPoint>/export/sdc</mountPoint><size>8192</size><spaceInUse>602</spaceInUse><partitions/></disk></disks></server>";
		String diskStr = "<options><option><key>auth.allow</key><value>*</value></option><option><key>cluster.stripe-block-size</key><value>*:128KB</value></option></options>";
		//diskStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><options><option><key>auth.allow</key><value>*</value></option><option><key>cluster.stripe-block-size</key><value>*:128KB</value></option></options>";
		VolumeOptions disk = (VolumeOptions)new ServerUtil().unmarshal(VolumeOptions.class, diskStr, false);
		System.out.println(disk.size());
		for(VolumeOption option : disk.getOptions()) {
			System.out.println(option.toString());
		}
	}
}
