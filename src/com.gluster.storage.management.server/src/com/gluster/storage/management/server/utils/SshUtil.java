/*******************************************************************************
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
 *******************************************************************************/
package com.gluster.storage.management.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.utils.LRUCache;
import com.gluster.storage.management.core.utils.ProcessResult;

/**
 *
 */
public class SshUtil {
	private LRUCache<String, Connection> sshConnCache = new LRUCache<String, Connection>(10);
	private static final File PEM_FILE = new File(CoreConstants.USER_HOME + "/" + ".ssh/id_rsa");
	// TODO: Make user name configurable
	private static final String USER_NAME = "root";
	// TODO: Make default password configurable
	private static final String DEFAULT_PASSWORD = "syst3m";

	private Connection getConnectionWithPassword(String serverName) {
		Connection conn = createConnection(serverName);
		authenticateWithPassword(conn);
		return conn;
	}

	private synchronized Connection getConnection(String serverName) {
		Connection conn = sshConnCache.get(serverName);
		if (conn != null) {
			return conn;
		}

		conn = createConnection(serverName);
		authenticateWithPublicKey(conn);
		sshConnCache.put(serverName, conn);
		return conn;
	}

	private void authenticateWithPublicKey(Connection conn) {
		try {
			if (!supportsPublicKeyAuthentication(conn)) {
				throw new GlusterRuntimeException("Public key authentication not supported on [" + conn.getHostname()
						+ "]");
			}

			// TODO: Introduce password for the PEM file (third argument) so that it is more secure
			if (!conn.authenticateWithPublicKey(USER_NAME, PEM_FILE, null)) {
				throw new GlusterRuntimeException("SSH Authentication (public key) failed for server ["
						+ conn.getHostname() + "]");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Exception during SSH authentication (public key) for server ["
					+ conn.getHostname() + "]", e);
		}
	}

	private void authenticateWithPassword(Connection conn) {
		try {
			if (!supportsPasswordAuthentication(conn)) {
				throw new GlusterRuntimeException("Password authentication not supported on [" + conn.getHostname()
						+ "]");
			}

			// TODO: Introduce password for the PEM file (third argument) so that it is more secure
			if (!conn.authenticateWithPassword(USER_NAME, DEFAULT_PASSWORD)) {
				throw new GlusterRuntimeException("SSH Authentication (password) failed for server ["
						+ conn.getHostname() + "]");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Exception during SSH authentication (password) for server ["
					+ conn.getHostname() + "]", e);
		}
	}

	private boolean supportsPasswordAuthentication(Connection conn) throws IOException {
		return Arrays.asList(conn.getRemainingAuthMethods(USER_NAME)).contains("password");
	}

	private boolean supportsPublicKeyAuthentication(Connection conn) throws IOException {
		return Arrays.asList(conn.getRemainingAuthMethods(USER_NAME)).contains("publickey");
	}

	private Connection createConnection(String serverName) {
		Connection conn;
		conn = new Connection(serverName);
		try {
			conn.connect();
		} catch (IOException e) {
			e.printStackTrace();
			throw new GlusterRuntimeException("Exception while creating SSH connection with server [" + serverName
					+ "]", e);
		}
		return conn;
	}

	private boolean wasTerminated(int condition) {
		return ((condition | ChannelCondition.EXIT_SIGNAL) == condition);
	}

	private boolean hasErrors(int condition, Session session) {
		return (hasErrorStream(condition) || (exitedGracefully(condition) && exitedWithError(session)));
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
		try {
			Session session = sshConnection.openSession();
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(
					session.getStdout())));
			BufferedReader stderrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(
					session.getStderr())));
			session.execCommand(command);
			ProcessResult result = getResultOfExecution(session, stdoutReader, stderrReader);
			session.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private ProcessResult getResultOfExecution(Session session, BufferedReader stdoutReader, BufferedReader stderrReader) {
		// Wait for program to come out either
		// a) gracefully with an exit status, OR
		// b) because of a termination signal
		int condition = session.waitForCondition(ChannelCondition.EXIT_SIGNAL | ChannelCondition.EXIT_STATUS, 5000);
		StringBuilder output = new StringBuilder();

		try {
			readFromStream(stdoutReader, output);
			if (hasErrors(condition, session)) {
				readFromStream(stderrReader, output);
			}

			return prepareProcessResult(session, condition, output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private ProcessResult prepareProcessResult(Session session, int condition, StringBuilder output) {
		ProcessResult result = null;
		if (wasTerminated(condition)) {
			result = new ProcessResult(ProcessResult.FAILURE, output.toString());
		} else {
			if (hasErrors(condition, session)) {
				Integer exitStatus = session.getExitStatus();
				int statusCode = (exitStatus == null ? ProcessResult.FAILURE : exitStatus);
				result = new ProcessResult(statusCode, output.toString());
			} else {
				result = new ProcessResult(ProcessResult.SUCCESS, output.toString());
			}
		}
		return result;
	}

	private void readFromStream(BufferedReader streamReader, StringBuilder output) throws IOException,
			UnsupportedEncodingException {
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
		return executeCommand(getConnectionWithPassword(serverName), command);
	}

	/**
	 * Executes given command on remote machine using public key authentication
	 * 
	 * @param serverName
	 * @param command
	 * @return Result of remote execution
	 */
	public ProcessResult executeRemote(String serverName, String command) {
		return executeCommand(getConnection(serverName), command);
	}

	/**
	 * Checks if public key of management gateway is configured on given server
	 * 
	 * @param serverName
	 * @return true if public key is configured, else false
	 */
	public boolean isPublicKeySetup(String serverName) {
		try {
			getConnection(serverName);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void cleanup() {
		for (Connection conn : sshConnCache.values()) {
			conn.close();
		}
	}

	public static void main(String[] args) {
		SshUtil sshUtil = new SshUtil();
		System.out.println(new Date());
		ProcessResult result = sshUtil.executeRemote("dev.gluster.com", "/bin/pwd");
		System.out.println(result.getOutput());
		result = sshUtil.executeRemote("dev.gluster.com", "/bin/pwd1");
		System.out.println(new Date() + " - " + result.getExitValue() + " - " + result.getOutput());
		result = sshUtil.executeRemote("dev.gluster.com", "/bin/ls -lrt");
		System.out.println(new Date() + " - " + result.getExitValue() + " - " + result.getOutput());

		sshUtil.cleanup();
	}
}
