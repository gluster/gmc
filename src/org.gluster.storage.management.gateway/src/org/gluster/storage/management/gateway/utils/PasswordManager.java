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

import org.apache.derby.jdbc.EmbeddedDriver;
import org.gluster.storage.management.core.constants.CoreConstants;
import org.gluster.storage.management.gateway.security.UserAuthDao;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;


/**
 * Tool to reset password of default user gluster
 */
public class PasswordManager {
	private static final int USAGE_ERR = 1;
	private static final int SQL_ERR = 2;

	private void resetPassword(String username) {
		try {
			UserAuthDao userAuthDao = createUserAuthDao();
			ReflectionSaltSource saltSource = createSaltSource();

			String encodedPassword = new ShaPasswordEncoder(256).encodePassword(CoreConstants.DEFAULT_PASSWORD,
					saltSource.getSalt(userAuthDao.loadUserByUsername(username)));
			
			userAuthDao.changePassword(username, encodedPassword);
			
			System.out.println("Password for user [" + username + "] reset successsfully to default value of ["
					+ CoreConstants.DEFAULT_PASSWORD + "]." + CoreConstants.NEWLINE);
			
			DBUtil.shutdownDerby();
		} catch (Exception e) {
			System.err.println(CoreConstants.NEWLINE + CoreConstants.NEWLINE + "Password reset for user [" + username
					+ "] failed! " + CoreConstants.NEWLINE
					+ "Make sure that the Management Gateway is not running while performing password reset."
					+ CoreConstants.NEWLINE);
			System.exit(SQL_ERR);
		}
	}
	
	private ReflectionSaltSource createSaltSource() {
		ReflectionSaltSource saltSource = new ReflectionSaltSource();
		saltSource.setUserPropertyToUse("username");
		return saltSource;
	}

	private UserAuthDao createUserAuthDao() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		UserAuthDao authDao = new UserAuthDao();
		EmbeddedDriver driver = (EmbeddedDriver) Class.forName(EmbeddedDriver.class.getName()).newInstance();
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource(driver, "jdbc:derby:/opt/glustermg/data", "gluster", "syst3m");
		
		authDao.setDataSource(dataSource);
		return authDao;
	}

	public static void main(String args[]) {
		if (args.length != 2 || !args[0].equals("reset")) {
			System.err.println("Usage: java " + PasswordManager.class.getName() + " reset <username>\n");
			System.exit(USAGE_ERR);
		}
		
		new PasswordManager().resetPassword(args[1]);
	}
}
