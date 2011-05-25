/**
 * GlusterServerInitializer.java
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
package com.gluster.storage.management.server.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.derby.tools.ij;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.server.data.ClusterInfo;
import com.gluster.storage.management.server.data.PersistenceDao;

/**
 * Initializes the Gluster Management Server.
 */
public class InitServerTask extends JdbcDaoSupport {
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private String appVersion;

	@Autowired
	private PersistenceDao<ClusterInfo> clusterDao;

	@Autowired
	ServletContext servletContext;

	private static final String SCRIPT_DIR = "data/scripts/";

	public void securePasswords() {
		getJdbcTemplate().query("select username, password from users", new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				String username = rs.getString(1);
				String password = rs.getString(2);
				String encodedPassword = passwordEncoder.encodePassword(password, null);
				getJdbcTemplate().update("update users set password = ? where username = ?", encodedPassword, username);
				logger.debug("Updating password for username: " + username);
			}
		});
	}

	private void executeScript(File script) {
		ByteArrayOutputStream sqlOut = new ByteArrayOutputStream();
		int numOfExceptions;
		try {
			numOfExceptions = ij.runScript(getJdbcTemplate().getDataSource().getConnection(), new FileInputStream(
					script), CoreConstants.ENCODING_UTF8, sqlOut, CoreConstants.ENCODING_UTF8);
			String output = sqlOut.toString();
			sqlOut.close();
			logger.debug("Data script [" + script.getName() + "] returned with exit status [" + numOfExceptions
					+ "] and output [" + output + "]");
			if (numOfExceptions != 0) {
				throw new GlusterRuntimeException("Server data initialization script [ " + script.getName()
						+ "] failed with [" + numOfExceptions + "] exceptions! [" + output + "]");
			}
		} catch (Exception ex) {
			throw new GlusterRuntimeException("Server data initialization script [" + script.getName() + "] failed!",
					ex);
		}
	}

	private void initDatabase() {
		logger.debug("Initializing server data...");
		executeScriptsFrom(getDirFromRelativePath(SCRIPT_DIR + appVersion));

		securePasswords(); // encrypt the passwords
	}

	private File getDirFromRelativePath(String relativePath) {
		String scriptDirPath = servletContext.getRealPath(relativePath);
		File scriptDir = new File(scriptDirPath);
		return scriptDir;
	}

	private void executeScriptsFrom(File scriptDir) {
		if (!scriptDir.exists()) {
			throw new GlusterRuntimeException("Script directory [" + scriptDir.getAbsolutePath() + "] doesn't exist!");
		}
		
		List<File> scripts = Arrays.asList(scriptDir.listFiles());
		if(scripts.size() == 0) {
			throw new GlusterRuntimeException("Script directory [" + scriptDir.getAbsolutePath() + "] is empty!");
		}
		
		Collections.sort(scripts);
		for (File script : scripts) {
			executeScript(script);
		}
	}

	/**
	 * Initializes the server database, if running for the first time.
	 */
	public synchronized void initServer() {
		try {
			String dbVersion = getDBVersion();
			if (!appVersion.equals(dbVersion)) {
				logger.info("App version [" + appVersion + "] differs from data version [" + dbVersion
						+ "]. Trying to upgrade data...");
				upgradeData(dbVersion, appVersion);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// Database not created yet. Create it!
			initDatabase();
		}

		// For development time debugging. To be removed later.
		List<ClusterInfo> clusters = clusterDao.findAll();
		logger.info(clusters.size());

		if (clusters.size() > 0) {
			for (ClusterInfo cluster : clusters) {
				logger.info("Cluster: [" + cluster.getId() + "][" + cluster.getName() + "]");
			}
		} else {
			logger.info("No cluster created yet.");
		}
	}

	private void upgradeData(String fromVersion, String toVersion) {
		executeScriptsFrom(getDirFromRelativePath(SCRIPT_DIR + fromVersion + "-" + toVersion));
	}

	private String getDBVersion() {
		return (String) clusterDao.getSingleResultFromSQL("select version from version");
	}
}
