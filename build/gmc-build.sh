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

set -e

GMC_TARGET_URL='git@github.com:gluster/gmc-target.git'
BUCKMINSTER_URL=http://download.eclipse.org/tools/buckminster/headless-3.7/
BUCKMINSTER_PRODUCT_NAME=org.eclipse.buckminster.cmdline.product
GMC_WEBSTART_PROJECT=org.gluster.storage.management.console.feature.webstart
GMC_CORE_PROJECT=org.gluster.storage.management.core
GMC_CONSOLE_PROJECT=org.gluster.storage.management.console
GMG_PROJECT=org.gluster.storage.management.gateway

startBold() 
{
	tput bold
}

stopBold() 
{
	tput sgr0
}

# Shows given text in bold
showBold() 
{
	startBold
	echo ${1}
	stopBold
}

# Get the director that can be used to install headless buckminster
get_director()
{
    mkdir -p ${TOOLS_DIR}
    cd ${TOOLS_DIR}

    echo "Downloading buckminster director..."
    wget -c http://ftp.daum.net/eclipse//tools/buckminster/products/director_latest.zip
    if ! unzip -tqq director_latest.zip; then
		rm -f director_latest.zip
		wget http://ftp.daum.net/eclipse//tools/buckminster/products/director_latest.zip
    fi
    unzip -q director_latest.zip
    cd -
}

install_buckminster()
{
    mkdir -p ${BUCKMINSTER_HOME}

    echo "Installing buckminster..."
    cd ${TOOLS_DIR}/director
    ./director -r ${BUCKMINSTER_URL} -d ${BUCKMINSTER_HOME} -p Buckminster -i ${BUCKMINSTER_PRODUCT_NAME}

    echo "Setting up buckminster..."
    cd ${BUCKMINSTER_HOME}
    echo "  => core"
    ./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.core.headless.feature
    echo "  => pde"
    ./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.pde.headless.feature
    echo "  => git"
    ./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.git.headless.feature
    echo "  => emma"
    ./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.emma.headless.feature
    cd -
}

# Create keystore for jar signing (self signed)
setup_keys()
{
    mkdir -p ${KEYS_DIR}
    cd ${KEYS_DIR}
    keytool -genkeypair -keystore gluster.keystore -storepass gluster -alias gluster -keyalg RSA << EOF
Gluster Temp Build
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

    for f in $src_dir/*; do
		ln -s $f
    done

    if [ ! -e gmc-target ]; then
		ln -s $gmc_target_dir gmc-target
    fi

    echo "Importing target platform..."
    ${BUCKMINSTER_HOME}/buckminster importtarget -data ${WORKSPACE_DIR} --active gmc-target/org.gluster.storage.management.console.target/gmc.target
    cd -
}

buckminster_perform()
{
    ${BUCKMINSTER_HOME}/buckminster perform --properties ${PROPERTIES_FILE} -Dbuckminster.output.root=${DIST_DIR} -data ${WORKSPACE_DIR} $*
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
    buckminster_perform ${GMC_WEBSTART_PROJECT}#buckminster.clean
	buckminster_perform -Dproduct.version=${VERSION} ${GMC_CONSOLE_PROJECT}#update.version
    buckminster_perform -Dtarget.os=${os} -Dtarget.ws=${ws} -Dtarget.arch=${arch} ${GMC_WEBSTART_PROJECT}#create.eclipse.jnlp.product
    buckminster_perform ${GMC_WEBSTART_PROJECT}#copy.root.files
    buckminster_perform -Dproduct.version=${VERSION} ${GMC_WEBSTART_PROJECT}#update.version

    # buckminster signs the jars using eclipse certificate - hence unsign and sign them again
    echo "Signing product jars..."
    buckminster_perform ${GMC_WEBSTART_PROJECT}#unsign.jars
    buckminster_perform -Djar.signing.keystore=${KEYS_DIR}/gluster.keystore ${GMC_WEBSTART_PROJECT}#sign.jars
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
    buckminster_perform ${GMC_CORE_PROJECT}#bundle.jar

    echo "Building Gateway..."
    buckminster_perform -Dproduct.version=${VERSION} ${GMG_PROJECT}#archive

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

# Clean the workspace (class files, jar files created during previous build)
# and the dist directory
clean()
{
	# Remove the core jar file created by previous build
	/bin/rm -f ${WORKSPACE_DIR}/src/org.gluster.storage.management.gateway/WebContent/WEB-INF/lib/org.gluster.storage.management.core*jar
	# Remove compiled class files
	/bin/rm -rf ${WORKSPACE_DIR}/src/org.gluster.storage.management.gateway/WebContent/WEB-INF/classes/*
	# Remove old build artifacts
	/bin/rm -rf ${DIST_BASE}/*
}

build()
{
    export VERSION=${VERSION:-1.0.0alpha}
	clean
    build_gmc_all
    build_gmg
    package_backend
}

#-----------------------------------
# Main Action Body
#-----------------------------------
ME=$(basename $0)
GMC_DIR=$(dirname $(dirname $(readlink -e $0)))


function show_help()
{
    cat <<EOF

Usage:  `startBold`$ME [-f] [-h] [GMC-TARGET-DIR] [BUILD-DIR]`stopBold`

Build Gluster Management Console from source.

  GMC-TARGET-DIR  -> Directory where gmc-target.git has been or should be cloned
  BUILD-DIR       -> Directory where build tasks will be performed and binaries will be created

  If not passed, these two directories will be created parallel to ${GMC_DIR}

  Options:
    -f              -> Force build (re-create build directory and perform build).
    -h              -> Display this help and exit

  Examples:
    $ME
    $ME ~/gmc-target
    $ME ~/gmc-target ~/gmc-build

EOF
}


function main()
{
    # Parse command line arguments.
    while getopts :fh OPT; do
	case "$OPT" in
	    h)
		show_help
		exit 0
		;;
	    f)
		force=yes
		;;
	    \?)
                # getopts issues an error message
		echo "Invalid option: -$OPTARG"
		show_help
		exit 1
		;;
	    :)
		echo "Option -$OPTARG requires an argument."
		show_help
		exit 1
		;;
	esac
    done

    # Remove the switches we parsed above.
    shift `expr $OPTIND - 1`

    # We want only one non-option argument.
    if [ $# -gt 2 ]; then
		show_help
		exit 1
    fi

    src_dir=$(dirname $(dirname $(readlink -e $0)))
    parent_dir=$(dirname $src_dir)

    gmc_target_dir=$1
    build_dir=$2

    if [ -z "$gmc_target_dir" ]; then
		gmc_target_dir=$parent_dir/gmc-target
    fi

    if [ -z "$build_dir" ]; then
		build_dir=$parent_dir/gmc-build
    fi

    if [ ! -e "$gmc_target_dir" ]; then
		echo "Getting gmc-target from $GMC_TARGET_URL"
		git clone $GMC_TARGET_URL $gmc_target_dir
    fi

    if [ "$force" = "yes" ]; then
		rm -fr $build_dir
    fi

    TOOLS_DIR=${build_dir}/tools
    DIST_BASE=${build_dir}/dist
    KEYS_DIR=${TOOLS_DIR}/keys
    BUCKMINSTER_HOME=${TOOLS_DIR}/buckminster
    WORKSPACE_DIR=${BUCKMINSTER_HOME}/workspace
    PROPERTIES_FILE=${WORKSPACE_DIR}/build/glustermc_build.properties
    SCRIPT_DIR=$src_dir/build

    if [ ! -e $build_dir ]; then
		mkdir -p $build_dir
		if [ ! -e ${TOOLS_DIR} ]; then
	    	get_director
		fi
		if [ ! -e ${BUCKMINSTER_HOME} ]; then
	    	install_buckminster
		fi
		if [ ! -e ${KEYS_DIR} ]; then
	    	setup_keys
		fi
    fi

    configure_workspace
    build
	echo
	echo "Build artifacts:"
    showBold "    ${DIST_BASE}/gmg/gmg-installer-$VERSION.tar.gz"
	showBold "    ${DIST_BASE}/gmg-backend/gmg-backend-installer-$VERSION.tar.gz"
	echo
}

main "$@"
