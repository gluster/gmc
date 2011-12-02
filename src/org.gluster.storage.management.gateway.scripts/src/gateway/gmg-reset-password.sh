#!/bin/bash
#*******************************************************************************
# Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
# This file is part of Gluster Management Gateway.
#
# Gluster Management Gateway is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# Gluster Management Gateway is distributed in the hope that it
# will be useful, but WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see
# <http://www.gnu.org/licenses/>.
#*******************************************************************************

#-----------------------------------------------------------------------------------
# gmg-reset-password.sh - script to reset password of given user to default password
#-----------------------------------------------------------------------------------

USAGE_ERR=1

if [ $# -ne 1 ]; then
	echo "Usage: ${0} <username>"
	echo
	exit ${USAGE_ERR}
fi

CURR_DIR=${PWD}
SCRIPT_PATH=`readlink -f ${0}`
GLUSTERMG_DIR=`dirname ${SCRIPT_PATH}`

# Main action body
cd ${GLUSTERMG_DIR}
cd ..
for FILE in WEB-INF/lib/*.jar
do
	export CLASSPATH=${CLASSPATH}:${PWD}/${FILE}
done
export CLASSPATH=${PWD}/WEB-INF/classes:${CLASSPATH}
cd ${CURR_DIR}
java org.gluster.storage.management.gateway.utils.PasswordManager reset ${1}
