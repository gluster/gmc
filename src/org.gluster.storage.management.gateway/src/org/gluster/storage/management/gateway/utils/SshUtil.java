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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.core.exceptions.ConnectionException;
import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.gluster.storage.management.core.utils.FileUtil;
import org.gluster.storage.management.core.utils.LRUCache;
import org.gluster.storage.management.core.utils.ProcessResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;


/**
 *
 */
@Component
public class SshUtil {
	private static final String TEMP_DIR = "/tmp/";
	public static final String SSH_AUTHORIZED_KEYS_DIR_LOCAL = "/opt/glustermg/keys/";
	public static final String SSH_AUTHORIZED_KEYS_DIR_REMOTE = "/root/.ssh/";
	private static final String SSH_AUTHORIZED_KEYS_FILE = "authorized_keys";
	private static final String SSH_AUTHORIZED_KEYS_PATH_REMOTE = SSH_AUTHORIZED_KEYS_DIR_REMOTE + SSH_AUTHORIZED_KEYS_FILE;
	public static final File PRIVATE_KEY_FILE = new File(SSH_AUTHORIZED_KEYS_DIR_LOCAL + "gluster.pem");
	public static final File PUBLIC_KEY_FILE = new File(SSH_AUTHORIZED_KEYS_DIR_LOCAL + "gluster.pub");
	private LRUCache<String, Connection> sshConnCache = new LRUCache<String, Connection>(10);

	// TODO: Make user name configurable
	private static final String USER_NAME = "root";
	// TODO: Make default password configurable
	private static final String DEFAULT_PASSWORD = "syst3m";
	
	private static final Logger logger = Logger.getLogger(SshUtil.class);
	
	@Autowired
	private Integer sshConnectTimeout;
	@Autowired
	private Integer sshKexTimeout;
	@Autowired
	private Integer sshExecTimeout;

	public boolean hasDefaultPassword(String serverName) {
		try {
			getConnectionWithPassword(serverName).close();
			return true;
		} catch(Exception e) {
			logger.warn("Couldn't connect to [" + serverName + "] with default password!", e);
			return false;
		}
	}
	
	/**
	 * Checks if public key of management gateway is configured on given server
	 * 
	 * @param serverName
	 * @return true if public key is configured, else false
	 */
	public boolean isPublicKeyInstalled(String serverName) {
		try {
			getConnectionWithPubKey(serverName).close();
			return true;
		} catch(ConnectionException e) {
			logger.warn("Couldn't connect to [" + serverName + "] with public key!", e);
			return false;
		}
	}
	
	public void getFile(String serverName, String remoteFile, String localDir) {
		try {
			Connection conn = getConnection(serverName);
			SCPClient scpClient = new SCPClient(conn);
			scpClient.get(remoteFile, localDir);
		} catch (IOException e) {
			throw new GlusterRuntimeException("Error while fetching file [" + remoteFile + "] from server ["
					+ serverName + "]", e);
		}
	}
	
	public synchronized void installPublicKey(String serverName) {
		Connection conn = null;
		try {
			conn = getConnectionWithPassword(serverName);
		} catch(Exception e) {
			// authentication failed. close the connection.
			conn.close();
			if (e instanceof GlusterRuntimeException) {
				throw (GlusterRuntimeException) e;
			} else {
				throw new GlusterRuntimeException("Exception during authentication with public key on server ["
						+ serverName + "]", e);
			}
		}
		SCPClient scpClient = new SCPClient(conn);

		// delete file if it exists
		File localTempFile = new File(TEMP_DIR + SSH_AUTHORIZED_KEYS_FILE);
		if(localTempFile.exists()) {
			localTempFile.delete();
		}
		
		try {
			// get authorized_keys from server
			scpClient.get(SSH_AUTHORIZED_KEYS_PATH_REMOTE, TEMP_DIR);
		} catch (IOException e) {
			// file doesn't exist. it will get created.
			// create the .ssh directory in case it doesn't exist
			logger.info("Couldn't fetch file [" + SSH_AUTHORIZED_KEYS_PATH_REMOTE +"].", e);
			logger.info("Creating /root/.ssh on [" + serverName + "] in case it doesn't exist.");
			String command = "mkdir -p " + SSH_AUTHORIZED_KEYS_DIR_REMOTE;
			ProcessResult result = executeCommand(conn, command);
			if(!result.isSuccess()) {
				String errMsg = "Command [" + command + "] failed on server [" + serverName + "] with error: " + result;
				logger.error(errMsg);
				throw new GlusterRuntimeException(errMsg);
			}
		}
		
		byte[] publicKeyData;
		try {
			publicKeyData = FileUtil.readFileAsByteArray(PUBLIC_KEY_FILE);
		} catch (Exception e) {
			conn.close();
			throw new GlusterRuntimeException("Couldn't load public key file [" + PUBLIC_KEY_FILE + "]", e);
		}
		
		try {
			// append it
			FileOutputStream outputStream = new FileOutputStream(localTempFile, true);
			outputStream.write(CoreConstants.NEWLINE.getBytes());
			outputStream.write(publicKeyData);
			outputStream.close();
		} catch (Exception e) {
			conn.close();
			throw new GlusterRuntimeException("Couldnt append file [" + localTempFile + "] with public key!", e);
		} 
		
		try {
			scpClient.put(localTempFile.getAbsolutePath(), SSH_AUTHORIZED_KEYS_FILE, SSH_AUTHORIZED_KEYS_DIR_REMOTE, "0600");
		} catch (IOException e) {
			throw new GlusterRuntimeException("Couldn't add public key to server [" + serverName + "]", e);
		} finally {
			conn.close();
			localTempFile.delete();
		}
		
		// It was decided NOT to disable password login as this may not be acceptable in a bare-metal environment
		// disableSshPasswordLogin(serverName, scpClient);
	}

//	private void disableSshPasswordLogin(String serverName, SCPClient scpClient) {
//		ProcessResult result = executeRemote(serverName, SCRIPT_DISABLE_SSH_PASSWORD_AUTH);
//		if(!result.isSuccess()) {
//			throw new GlusterRuntimeException("Couldn't disable SSH password authentication on [" + serverName
//					+ "]. Error: " + result);
//		}
//	}

	private synchronized Connection getConnectionWithPassword(String serverName) {
		Connection conn = createConnection(serverName);
		if(!authenticateWithPassword(conn)) {
			conn.close();
			throw new ConnectionException("SSH Authentication (password) failed for server ["
					+ conn.getHostname() + "]");
		}
		return conn;
	}
	
	private synchronized Connection getConnectionWithPubKey(String serverName) {
		Connection conn = createConnection(serverName);
		if(!authenticateWithPublicKey(conn)) {
			conn.close();
			throw new ConnectionException("SSH Authentication (public key) failed for server ["
					+ conn.getHostname() + "]");
		}
		return conn;
	}

	private synchronized Connection getConnection(String serverName) {
		Connection conn = sshConnCache.get(serverName);
		if (conn != null) {
			return conn;
		}
		
		conn = createConnection(serverName);
		try {
			if(!authenticateWithPublicKey(conn)) {
				if(!authenticateWithPassword(conn)) {
					conn.close();
					throw new ConnectionException("SSH authentication failed on server [" + serverName + "]!");
				}
			}
		} catch(Exception e) {
			// authentication failed. close the connection.
			conn.close();
			if(e instanceof GlusterRuntimeException) {
				throw (GlusterRuntimeException)e;
			} else {
				throw new GlusterRuntimeException("Exception during authentication on server [" + serverName + "]", e);
			}
		}
		
		sshConnCache.put(serverName, conn);
		return conn;
	}

	private boolean authenticateWithPublicKey(Connection conn) {
		try {
			if (!supportsPublicKeyAuthentication(conn)) {
				throw new ConnectionException("Public key authentication not supported on [" + conn.getHostname()
						+ "]");
			}

			if (!conn.authenticateWithPublicKey(USER_NAME, PRIVATE_KEY_FILE, null)) {
				return false;
			}
			
			return true;
		} catch (IOException e) {
			throw new ConnectionException("Exception during SSH authentication (public key) for server ["
					+ conn.getHostname() + "]", e);
		}
	}

	private boolean authenticateWithPassword(Connection conn) {
		try {
			if (!supportsPasswordAuthentication(conn)) {
				throw new ConnectionException("Password authentication not supported on [" + conn.getHostname()
						+ "]");
			}

			if (!conn.authenticateWithPassword(USER_NAME, DEFAULT_PASSWORD)) {
				return false;
			}
			return true;
		} catch (IOException e) {
			throw new ConnectionException("Exception during SSH authentication (password) for server ["
					+ conn.getHostname() + "]", e);
		}
	}

	private boolean supportsPasswordAuthentication(Connection conn) throws IOException {
		return Arrays.asList(conn.getRemainingAuthMethods(USER_NAME)).contains("password");
	}

	private boolean supportsPublicKeyAuthentication(Connection conn) throws IOException {
		return Arrays.asList(conn.getRemainingAuthMethods(USER_NAME)).contains("publickey");
	}

	private synchronized Connection createConnection(String serverName) {
		Connection conn = new Connection(serverName);
		try {
			conn.connect(null, sshConnectTimeout, sshKexTimeout);
		} catch (IOException e) {
			logger.error("Couldn't establish SSH connection with server [" + serverName + "]", e);
			conn.close();
			throw new ConnectionException("Exception while creating SSH connection with server [" + serverName + "]", e);
		}
		return conn;
	}

	private boolean wasTerminated(int condition) {
		return ((condition | ChannelCondition.EXIT_SIGNAL) == condition);
	}

	private boolean hasErrors(int condition, Session session) {
		return (hasErrorStream(condition) || (exitedGracefully(condition) && exitedWithError(session)));
	}
	
	private boolean timedOut(int condition) {
		return (condition == ChannelCondition.TIMEOUT);
	}

	private boolean exitedWithError(Session session) {
		return session.getExitStatus() != ProcessResult.SUCCESS;
	}

	private boolean exitedGracefully(int condition) {
		return (condition | ChannelCondition.EXIT_STATUS) == condition;
	}

	private boolean hasErrorStream(int condition) {
		return (condition | ChannelCondition.STDERR_DATA) == condition;
	}

	private ProcessResult executeCommand(Connection sshConnection, String command) {
		Session session = null;
		try {
			session = sshConnection.openSession();
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(
					session.getStdout())));
			BufferedReader stderrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(
					session.getStderr())));
			session.execCommand(command);
			ProcessResult result = getResultOfExecution(session, stdoutReader, stderrReader);
			return result;
		} catch (Exception e) {
			String errMsg = "Exception while executing command [" + command + "] on [" + sshConnection.getHostname()
					+ "]";
			logger.error(errMsg, e);
			
			// remove the connection from cache and close it
			sshConnCache.remove(sshConnection.getHostname());
			sshConnection.close();
			if(e instanceof IllegalStateException || e instanceof IOException) {
				// The connection is no more valid. Create and throw a connection exception.
				throw new ConnectionException("Couldn't open SSH session on [" + sshConnection.getHostname() + "]!", e);
			} else {
				throw new GlusterRuntimeException(errMsg, e);
			}
		} finally {
			if(session != null) {
				session.close();
			}
		}
	}

	private ProcessResult getResultOfExecution(Session session, BufferedReader stdoutReader, BufferedReader stderrReader) {
		// Wait for program to come out either
		// a) gracefully with an exit status, OR
		// b) because of a termination signal
		// c) command takes to long to exit (timeout)
		int condition = session.waitForCondition(ChannelCondition.EXIT_SIGNAL | ChannelCondition.EXIT_STATUS,
				sshExecTimeout);
		StringBuilder output = new StringBuilder();

		try {
			if(!timedOut(condition)) {
				readFromStream(stdoutReader, output);
				if (hasErrors(condition, session)) {
					readFromStream(stderrReader, output);
				}
			}

			return prepareProcessResult(session, condition, output.toString().trim());
		} catch (IOException e) {
			String errMsg = "Error while reading output stream from SSH connection!";
			logger.error(errMsg, e);
			return new ProcessResult(ProcessResult.FAILURE, errMsg);
		}
	}

	private ProcessResult prepareProcessResult(Session session, int condition, String output) {
		ProcessResult result = null;
		
		if (wasTerminated(condition)) {
			result = new ProcessResult(ProcessResult.FAILURE, output);
		} else if (timedOut(condition)) {
			result = new ProcessResult(ProcessResult.FAILURE, "Command timed out!");
		} else if (hasErrors(condition, session)) {
			Integer exitStatus = session.getExitStatus();
			int statusCode = (exitStatus == null ? ProcessResult.FAILURE : exitStatus);
			result = new ProcessResult(statusCode, output);
		} else {
			result = new ProcessResult(ProcessResult.SUCCESS, output);
		}
		
		return result;
	}

	private void readFromStream(BufferedReader streamReader, StringBuilder output) throws IOException {
		while (true) {
			String line = streamReader.readLine();
			if (line == null) {
				break;
			}
			output.append(line + CoreConstants.NEWLINE);
		}
	}

	/**
	 * Executes given command on remote machine using password authentication
	 * 
	 * @param serverName
	 * @param command
	 * @return Result of remote execution
	 */
	public ProcessResult executeRemoteWithPassword(String serverName, String command) {
		logger.info("Executing command [" + command + "] on server [" + serverName + "] with default password.");
		Connection conn = null;
		try {
			conn = getConnectionWithPassword(serverName);
			return executeCommand(conn, command);
		} finally {
			// we don't cache password based connections. hence the connection must be closed.
			if(conn != null) {
				conn.close();
			}
		}
	}
	
	/**
	 * Executes given command on remote machine using public key authentication
	 * 
	 * @param serverName
	 * @param command
	 * @return Result of remote execution
	 */
	public ProcessResult executeRemote(String serverName, String command) {
		logger.info("Executing command [" + command + "] on server [" + serverName + "]"); 
		return executeCommand(getConnection(serverName), command);
	}

	public void cleanup() {
		for (Connection conn : sshConnCache.values()) {
			conn.close();
		}
	}
	
	public Integer getSshConnectTimeout() {
		return sshConnectTimeout;
	}

	public void setSshConnectTimeout(Integer sshConnectTimeout) {
		this.sshConnectTimeout = sshConnectTimeout;
	}

	public Integer getSshKexTimeout() {
		return sshKexTimeout;
	}

	public void setSshKexTimeout(Integer sshKexTimeout) {
		this.sshKexTimeout = sshKexTimeout;
	}

	public Integer getSshExecTimeout() {
		return sshExecTimeout;
	}

	public void setSshExecTimeout(Integer sshExecTimeout) {
		this.sshExecTimeout = sshExecTimeout;
	}
}
