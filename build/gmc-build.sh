#!/bin/bash

#------------------------------------------------------------------
# Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
# This file is part of Gluster Management Console.
#
# Gluster Management Console is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# Gluster Management Console is distributed in the hope that it
# will be useful, but WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see
# <http://www.gnu.org/licenses/>.
#------------------------------------------------------------------

USAGE_ERR=1

BUCKMINSTER_URL=http://download.eclipse.org/tools/buckminster/headless-3.7/
BUCKMINSTER_PRODUCT_NAME=org.eclipse.buckminster.cmdline.product
GMC_WEBSTART_PROJECT=org.gluster.storage.management.console.feature.webstart
GMC_CORE_PROJECT=org.gluster.storage.management.core
GMG_PROJECT=org.gluster.storage.management.gateway

TYPE_ALL="a"
TYPE_SETUP="s"
TYPE_BUILD="b"

startBold() {
	tput bold
}

stopBold() {
	tput sgr0
}

# Shows given text in bold
showBold() {
	startBold
	echo ${1}
	stopBold
}

# Get the director that can be used to install headless buckminster
get_director()
{
	rm -rf ${TOOLS_DIR}
	mkdir -p ${TOOLS_DIR}
	cd ${TOOLS_DIR}

	echo "Downloading `startBold`director`stopBold`..."
	wget http://ftp.daum.net/eclipse//tools/buckminster/products/director_latest.zip
	echo "Installing director..."
	unzip director_latest.zip
}

install_buckminster()
{
	rm -rf ${BUCKMINSTER_HOME}
	mkdir -p ${BUCKMINSTER_HOME}

	echo "Installing `startBold`Buckminster`stopBold`..."
	cd ${TOOLS_DIR}/director
	./director -r ${BUCKMINSTER_URL} -d ${BUCKMINSTER_HOME} -p Buckminster -i ${BUCKMINSTER_PRODUCT_NAME}

	echo "Setting up Buckminster..."
	cd ${BUCKMINSTER_HOME}
	echo "  => core"
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.core.headless.feature
	echo "  => pde"
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.pde.headless.feature
	echo "  => git"
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.git.headless.feature
	echo "  => emma"
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.emma.headless.feature
}

# Create keystore for jar signing (self signed)
setup_keys()
{
	mkdir -p ${KEYS_DIR}
	cd ${KEYS_DIR}
	keytool -genkeypair -keystore gluster.keystore -storepass gluster -alias gluster -keyalg RSA << EOF
Gluster
Gluster
Gluster
Dummy
Dummy
US
yes
EOF

	keytool -selfcert -alias gluster -keystore gluster.keystore << EOF
gluster
EOF
	cd -
}

configure_workspace()
{
	echo "Configuring the workspace..."
	rm -rf ${WORKSPACE_DIR}
	mkdir -p ${WORKSPACE_DIR}
	cd ${WORKSPACE_DIR}

	cp -R ${BASE_DIR}/gmc/* . 2>/dev/null
	ln -fs ${BASE_DIR}/gmc-target .

	echo "Importing target platform..."
	${BUCKMINSTER_HOME}/buckminster importtarget -data ${WORKSPACE_DIR} --active gmc-target/org.gluster.storage.management.console.target/gmc.target
	cd -
}

build_gmc()
{
	os=${1}
	ws=${2}
	arch=${3}
	cd ${WORKSPACE_DIR}
	DIST_DIR=${DIST_BASE}/gmc/${os}.${ws}.${arch}
	if [ ! -d ${DIST_DIR} ]; then
		mkdir -p ${DIST_DIR}
	fi

	echo "Importing component query for glustermc..."
	${BUCKMINSTER_HOME}/buckminster import -data ${WORKSPACE_DIR} build/org.gluster.storage.management.console.feature.webstart.cquery

	echo "Building GMC for [${os}.${ws}.${arch}]"
	${BUCKMINSTER_HOME}/buckminster perform -Dbuckminster.output.root=${DIST_DIR} -data ${WORKSPACE_DIR} -Dtarget.os=${os} -Dtarget.ws=${ws} -Dtarget.arch=${arch} -Dcbi.include.source=false --properties ${PROPERTIES_FILE} ${GMC_WEBSTART_PROJECT}#create.eclipse.jnlp.product
	${BUCKMINSTER_HOME}/buckminster perform -Dbuckminster.output.root=${DIST_DIR} -data ${WORKSPACE_DIR} --properties ${PROPERTIES_FILE} ${GMC_WEBSTART_PROJECT}#copy.root.files

	# buckminster signs the jars using eclipse certificate - hence unsign and sign them again
	echo "Signing product jars..."
	${BUCKMINSTER_HOME}/buckminster perform -data ${WORKSPACE_DIR} -Dbuckminster.output.root=${DIST_DIR} --properties ${PROPERTIES_FILE} ${GMC_WEBSTART_PROJECT}#unsign.jars
	${BUCKMINSTER_HOME}/buckminster perform -data ${WORKSPACE_DIR} -Dbuckminster.output.root=${DIST_DIR} -Djar.signing.keystore=${KEYS_DIR}/gluster.keystore --properties ${PROPERTIES_FILE} ${GMC_WEBSTART_PROJECT}#sign.jars
}

build_gmg()
{
	cd ${WORKSPACE_DIR}
	export DIST_DIR=${DIST_BASE}/gmg
	if [ ! -d ${DIST_DIR} ]; then
		mkdir -p ${DIST_DIR}
	fi

	echo "Importing component query for glustermg..."
	${BUCKMINSTER_HOME}/buckminster import -data ${WORKSPACE_DIR} build/org.gluster.storage.management.core.cquery
	${BUCKMINSTER_HOME}/buckminster import -data ${WORKSPACE_DIR} build/org.gluster.storage.management.gateway.cquery

	echo "Building CORE..."
	${BUCKMINSTER_HOME}/buckminster perform -Dbuckminster.output.root=${DIST_DIR} -data ${WORKSPACE_DIR} -Dcbi.include.source=false --properties ${PROPERTIES_FILE} ${GMC_CORE_PROJECT}#bundle.jar

	echo "Building Gateway..."
	${BUCKMINSTER_HOME}/buckminster perform -Dbuckminster.output.root=${DIST_DIR} -data ${WORKSPACE_DIR} -Dcbi.include.source=false --properties ${PROPERTIES_FILE} ${GMG_PROJECT}#archive

	echo "Packaging Gateway..."
	${SCRIPT_DIR}/package-gateway.sh ${DIST_DIR} ${DIST_BASE}/gmc
}

package_backend()
{
	cd ${WORKSPACE_DIR}

	echo "Packaging backend scripts"
	export DIST_DIR=${DIST_BASE}/gmg-backend
	if [ ! -d ${DIST_DIR} ]; then
		mkdir -p ${DIST_DIR}
	fi

	${SCRIPT_DIR}/package-backend.sh ${DIST_DIR}
}

build_gmc_all()
{
	build_gmc linux gtk x86
	build_gmc linux gtk x86_64
	build_gmc win32 win32 x86
	build_gmc win32 win32 x86_64
	build_gmc macosx cocoa x86
	build_gmc macosx cocoa x86_64
}

build()
{
	export VERSION=1.0.0
	build_gmc_all
	build_gmg
	package_backend
}

#-----------------------------------
# Main Action Body
#-----------------------------------

echo
if [ $# -ne 1 ]; then
	echo "Usage: ${0} <build-type>"
	echo "build-type value can be one of:"
	echo "	`startBold` ${TYPE_ALL}`stopBold` - Sets up the build directoryand then triggers a full build"
	echo "	`startBold` ${TYPE_SETUP}`stopBold` - Sets up the build directory; doesn't trigger build"
	echo "	`startBold` ${TYPE_BUILD}`stopBold` - Assumes that build directory is set up and simply triggers the build"
	echo
	exit ${USAGE_ERR}
fi

BUILD_MODE=${1}
BASE_DIR=${PWD}/../..
TOOLS_DIR=${BASE_DIR}/tools
DIST_BASE=${BASE_DIR}/dist
KEYS_DIR=${TOOLS_DIR}/keys
BUCKMINSTER_HOME=${TOOLS_DIR}/buckminster
WORKSPACE_DIR=${BUCKMINSTER_HOME}/workspace
PROPERTIES_FILE=${WORKSPACE_DIR}/build/glustermc_build.properties
SCRIPT_DIR=${PWD}

if [ "${BUILD_MODE}" == "${TYPE_ALL}" -o "${BUILD_MODE}" == "${TYPE_SETUP}" ]; then
	get_director
	install_buckminster
	setup_keys
fi

if [ "${BUILD_MODE}" == "${TYPE_ALL}" -o "${BUILD_MODE}" == "${TYPE_BUILD}" ]; then
	configure_workspace
	build
fi
