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
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.derby.tools.ij;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import com.gluster.storage.management.core.constants.CoreConstants;
import com.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import com.gluster.storage.management.core.utils.FileUtil;

/**
 * Initializes the Gluster Management Server.
 */
public class InitServerTask extends JdbcDaoSupport {
	@Autowired
	private PasswordEncoder passwordEncoder;

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

	private void executeScript(String script) {
		ByteArrayOutputStream sqlOut = new ByteArrayOutputStream();
		int numOfExceptions;
		try {
			numOfExceptions = ij.runScript(getJdbcTemplate().getDataSource().getConnection(),
					new FileUtil().loadResource(script), CoreConstants.ENCODING_UTF8, sqlOut,
					CoreConstants.ENCODING_UTF8);
			String output = sqlOut.toString();
			sqlOut.close();
			logger.debug("Data script [" + script + "] returned with exit status [" + numOfExceptions
					+ "] and output [" + output + "]");
			if (numOfExceptions != 0) {
				throw new GlusterRuntimeException("Server data initialization script [ " + script + "] failed with ["
						+ numOfExceptions + "] exceptions! [" + output + "]");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new GlusterRuntimeException("Server data initialization script [" + script + "] failed!", ex);
		}
	}

	private void initDatabase() {
		logger.debug("Initializing server data...");
		executeScript("data/scripts/security-schema.sql");
		executeScript("data/scripts/users-authorities-groups.sql");
		securePasswords(); // encrypt the passwords
	}

	/**
	 * Initializes the server database, if running for the first time.
	 */
	public synchronized void initServer() {
		try {
			// Query to check whether the user table exists
			getJdbcTemplate().queryForInt("select count(*) from users");
			logger.debug("Server data is already initialized!");
		} catch (DataAccessException ex) {
			// Database not created yet. Create it!
			initDatabase();
		}
	}
}
