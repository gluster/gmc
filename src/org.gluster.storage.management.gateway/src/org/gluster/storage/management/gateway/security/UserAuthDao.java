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
package org.gluster.storage.management.gateway.security;

import org.gluster.storage.management.core.exceptions.GlusterRuntimeException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * 
 */
public class UserAuthDao extends JdbcDaoImpl implements GlusterUserDetailsService {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gluster.storage.management.gateway.security.GlusterUserDetailsService#changePassword(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void changePassword(String username, String password) {
		DataSourceTransactionManager txnManager = new DataSourceTransactionManager();
		txnManager.setDataSource(getDataSource());

		TransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus status = txnManager.getTransaction(def);
		try {
			getJdbcTemplate().update("UPDATE USERS SET PASSWORD = ? WHERE USERNAME = ?", password, username);
			txnManager.commit(status);
		} catch(Exception e) {
			txnManager.rollback(status);
			throw new GlusterRuntimeException("Exception while changing password of user [" + username + "]. Error: " + e.getMessage());
		}
	}
}
