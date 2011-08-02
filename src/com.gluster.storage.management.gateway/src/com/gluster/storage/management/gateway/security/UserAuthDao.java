/**
 * UserAuthDao.java
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
package com.gluster.storage.management.gateway.security;

import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

/**
 * 
 */
public class UserAuthDao extends JdbcDaoImpl implements GlusterUserDetailsService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gluster.storage.management.gateway.security.GlusterUserDetailsService#changePassword(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void changePassword(String username, String password) {
		getJdbcTemplate().update("UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?", password, username);
	}

}
