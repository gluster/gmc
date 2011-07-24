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
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.ConnectionException;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.model.Server;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;
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
	
	private static final Logger logger = Logger.getLogger(ServerUtil.class);

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

	public ServerStats fetchCPUUsageData(String serverName) {
		Object output = executeOnServer(true, serverName, "get_rrd_cpu_details.py 1d", ServerStats.class);
		//String cpuUsageData = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <xport> <meta> <start>1310468100</start> <step>300</step> <end>1310471700</end> <rows>13</rows> <columns>3</columns> <legend> <entry>user</entry> <entry>system</entry> <entry>total</entry> </legend> </meta> <data> <row><t>1310468100</t><v>2.23802952e-1</v><v>4.3747778209e-01</v><v>6.6128073384e-01</v></row> <row><t>1310468400</t><v>2.3387347338e-01</v><v>4.4642717442e-01</v><v>6.8030064780e-01</v></row> <row><t>1310468700</t><v>5.5043873220e+00</v><v>6.2462376636e+00</v><v>1.1750624986e+01</v></row> <row><t>1310469000</t><v>2.4350593653e+01</v><v>2.6214585217e+01</v><v>5.0565178869e+01</v></row> <row><t>1310469300</t><v>4.0786489953e+01</v><v>4.6784713828e+01</v><v>8.7571203781e+01</v></row> <row><t>1310469600</t><v>4.1459955508e+01</v><v>5.2546309044e+01</v><v>9.4006264551e+01</v></row> <row><t>1310469900</t><v>4.2312286165e+01</v><v>5.2390588332e+01</v><v>9.4702874497e+01</v></row> <row><t>1310470200</t><v>4.2603794982e+01</v><v>5.1598861493e+01</v><v>9.4202656475e+01</v></row> <row><t>1310470500</t><v>3.8238751290e+01</v><v>4.5312089966e+01</v><v>8.3550841256e+01</v></row> <row><t>1310470800</t><v>1.7949961224e+01</v><v>2.1282058418e+01</v><v>3.9232019642e+01</v></row> <row><t>1310471100</t><v>1.2330371421e-01</v><v>4.6347832868e-01</v><v>5.8678204289e-01</v></row> <row><t>1310471400</t><v>1.6313260492e-01</v><v>5.4088119561e-01</v><v>7.0401380052e-01</v></row> <row><t>1310471700</t><v>NaN</v><v>NaN</v><v>NaN</v></row> </data> </xport>";
		//Object output = unmarshal(ServerStats.class, cpuUsageData, false);
		if(output instanceof Status) {
			throw new GlusterRuntimeException(((Status)output).toString());
		}
		return (ServerStats) output;
	}
	
	private ServerStats getFirstOnlineServerCPUStats(List<String> serverNames, boolean removeServerOnError, boolean removeOnlineServer) {
		for(int i = serverNames.size() - 1; i >= 0; i--) {
			String serverName = serverNames.get(i);
			try {
				ServerStats stats = fetchCPUUsageData(serverName);
				if(removeOnlineServer) {
					serverNames.remove(serverName);
				}
				return stats;
			} catch(Exception e) {
				// server might be offline - continue with next one
				logger.warn("Couldn't fetch CPU stats from server [" + serverName + "]!", e);
				if(removeServerOnError) {
					serverNames.remove(serverName);
				}
				continue;
			}
		}
		throw new GlusterRuntimeException("All servers offline!");
	}

	public ServerStats fetchAggregatedCPUStats(List<String> serverNames) {
		if(serverNames == null || serverNames.size() == 0) {
			throw new GlusterRuntimeException("No server names passed to fetchAggregaredCPUUsageData!");
		}
		
		ServerStats firstServerStats = getFirstOnlineServerCPUStats(serverNames, true, true);

		ServerStats aggregatedStats = new ServerStats(firstServerStats);
		aggregateCPUStats(serverNames, aggregatedStats);
		return aggregatedStats;
	}

	public static void main(String[] args) {
//		ServerStats stats = new ServerUtil().fetchCPUUsageData("s1", "1d");
//		for(ServerStatsRow row : stats.getRows()) {
//			System.out.println(row.getUsageData().get(2));
//		}
//		JAXBContext context;
//		try {
//			context = JAXBContext.newInstance(ServerStats.class);
//			Marshaller m = context.createMarshaller();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			m.marshal(stats, out);
//			ServerStats stats1 = (ServerStats)new ServerUtil().unmarshal(ServerStats.class, out.toString(), false);
//			for(ServerStatsRow row : stats1.getRows()) {
//				System.out.println(row.getUsageData().get(2));
//			}
//		} catch (JAXBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
