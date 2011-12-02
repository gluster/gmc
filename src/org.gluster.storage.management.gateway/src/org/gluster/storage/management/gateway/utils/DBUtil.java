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

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.gluster.storage.management.core.constants.CoreConstants;


/**
 *
 */
public class DBUtil {
	private static final Logger logger = Logger.getLogger(DBUtil.class);
	public static void shutdownDerby() {
		try {
			// the shutdown=true attribute shuts down Derby
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
			
			// To shut down a specific database only, but keep the
            // engine running (for example for connecting to other
            // databases), specify a database in the connection URL:
            //DriverManager.getConnection("jdbc:derby:" + dbName + ";shutdown=true");
		} catch (Exception e) {
			if(e instanceof SQLException) {
				SQLException se = (SQLException) e;
				if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState())))) {
					// we got the expected exception
					logger.info("Derby shut down normally");
					// Note that for single database shutdown, the expected
					// SQL state is "08006", and the error code is 45000.
				} else {
					// if the error code or SQLState is different, we have
					// an unexpected exception (shutdown failed)
					logger.error("Derby did not shut down normally!" + inspectSQLException(se), se);
				}
			} else {
				logger.error("Derby did not shut down normally! [" + e.getMessage() + "]", e);
			}
		}
		// force garbage collection to unload the EmbeddedDriver
        // so Derby can be restarted
        System.gc();
	}
	
	/**
     * Extracts details of an SQLException chain to <code>String</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    private static String inspectSQLException(SQLException e)
    {
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
    	String errMsg = "";
        while (e != null)
        {
			errMsg += "\n----- SQLException -----" + CoreConstants.NEWLINE + "  SQL State:  " + e.getSQLState()
					+ CoreConstants.NEWLINE + "  Error Code: " + e.getErrorCode() + CoreConstants.NEWLINE
					+ "  Message:    " + e.getMessage();
            e = e.getNextException();
        }
        return errMsg;
    }
}
