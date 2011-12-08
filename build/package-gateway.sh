#!/bin/bash

#------------------------------------------------------------------
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
#------------------------------------------------------------------

WAR_NAME="glustermg.war"
WAR_SCRIPTS_DIR=${WAR_NAME}/scripts
NEW_WAR_NAME="glustermg"
VERSION=${VERSION:-1.0.0-alpha}
TAR_NAME=${NEW_WAR_NAME}-${VERSION}.war.tar
INSTALL_SCRIPT_NAME=gmg-install.sh
INSTALLER_TAR_NAME=gmg-installer-${VERSION}.tar

prepare-dist-dir()
{
	if [ -d ${WAR_NAME} ]; then
		rm -rf ${WAR_NAME}
	fi
	mkdir -p ${WAR_SCRIPTS_DIR}
	if [ -d ${NEW_WAR_NAME} ]; then
		rm -rf ${NEW_WAR_NAME}
	fi
}

get-server-war()
{
	cd ${WAR_NAME}
	WAR_FILE=`find -L ${SERVER_DIST_DIR} -name ${WAR_NAME}`
	jar xvf ${WAR_FILE}
	chmod +x scripts/*
	cd -
}

# On some platforms like Mac and Windows 7, the entries for other architectures are causing problem
# e.g. The "x86" entries cause errors in a 64 bit Mac client. Hence we remove the unnecessary entries
# from the JNLP files i.e. Remove all "x86_64" related tags from the JNLP file of "x86" architecture,
# and vice versa
update-jnlp()
{
	CPU_ARCH=${1}
	JNLP_FILE=${2}
	if [ "${CPU_ARCH}" == "x86" ]; then
		TAG_START_EXPR="<resources.*arch=\"x86_64.*,.*amd64\""
	else
		TAG_START_EXPR="<resources.*arch=\"x86\""
	fi
	TAG_END_EXPR="<\/resources.*"

	sed -i "/${TAG_START_EXPR}/,/${TAG_END_EXPR}/d" ${JNLP_FILE} || exit 1
}

get-dist()
{
	ARCH=${1}
	OS=${2}
	WS=${3}

	if [ -z "${GMC_DIST_DIR}" ]; then
		OUT_DIR="${WORKSPACE}/../../${BRANCH}-glustermc/workspace/arch/${ARCH}/os/${OS}/ws/${WS}/buckminster.output/org.gluster.storage.management.console.feature.webstart*.feature/glustermc"
	else
		OUT_DIR="${GMC_DIST_DIR}/${OS}.${WS}.${ARCH}/org.gluster.storage.management.console.feature.webstart*.feature/glustermc"
	fi
	NEW_DIR=${WAR_NAME}/${OS}.${WS}.${ARCH}
	cp -R ${OUT_DIR} ${NEW_DIR}

	update-jnlp ${ARCH} ${NEW_DIR}/org.gluster.storage.management.console.feature_*.jnlp
}

get-console-dists()
{
	get-dist x86 linux gtk
	get-dist x86_64 linux gtk
	get-dist x86 win32 win32
	get-dist x86_64 win32 win32
	get-dist x86 macosx cocoa
	get-dist x86_64 macosx cocoa
}

get-scripts()
{
	cp src/org.gluster.storage.management.gateway.scripts/src/common/* ${WAR_SCRIPTS_DIR}
	cp src/org.gluster.storage.management.gateway.scripts/src/gateway/* ${WAR_SCRIPTS_DIR}
	chmod +x ${WAR_SCRIPTS_DIR}/*
}

#---------------------------------------------
# Main Action Body
#---------------------------------------------

if [ $# -eq 2 ]; then
	DIST_DIR=${1}
	GMC_DIST_DIR=${2}
fi
SERVER_DIST_DIR=${DIST_DIR:-${WORKSPACE}/buckminster.output}

echo "Packaging Gluster Management Gateway..."

prepare-dist-dir
get-scripts
get-server-war
get-console-dists

/bin/mv -f ${WAR_NAME} ${NEW_WAR_NAME}
/bin/rm -rf ${TAR_NAME} ${TAR_NAME}.gz
tar cvfz ${TAR_NAME}.gz ${NEW_WAR_NAME}
cp build/${INSTALL_SCRIPT_NAME} .

tar cvfz ${INSTALLER_TAR_NAME}.gz ${TAR_NAME}.gz ${INSTALL_SCRIPT_NAME}

if [ ! -z "${DIST_DIR}" ]; then
	mv ${INSTALLER_TAR_NAME}.gz ${DIST_DIR}
fi

echo "Done!"
