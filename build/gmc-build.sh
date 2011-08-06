USAGE_ERR=1

BUILD_DIR=/tmp/buckminster/glustermc
BUCKMINSTER_URL=http://download.eclipse.org/tools/buckminster/headless-3.6/
TARGET_PLATFORM_URL=/data/private/gmc-target.git
SRC_URL=/data/private/gmc-src.git

BUCKMINSTER_PRODUCT_NAME=org.eclipse.buckminster.cmdline.product
MAIN_FEATURE=com.gluster.storage.management.console.feature.webstart

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
	echo "Downloading `startBold`director`stopBold`..."
	wget http://ftp.daum.net/eclipse//tools/buckminster/products/director_latest.zip
	echo "Installing director..."
	unzip director_latest.zip
}

install_buckminster()
{
	echo "Installing `startBold`Buckminster`stopBold`..."
	cd director
	./director -r ${BUCKMINSTER_URL} -d ${BUCKMINSTER_HOME} -p Buckminster -i ${BUCKMINSTER_PRODUCT_NAME}

	echo "Setting up Buckminster..."
	cd ${BUCKMINSTER_HOME}
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.core.headless.feature
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.pde.headless.feature
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.git.headless.feature
	./buckminster install ${BUCKMINSTER_URL} org.eclipse.buckminster.emma.headless.feature
}

configure_workspace()
{
	echo "Configuring the workspace..."
	cd ${WORKSPACE_DIR}
	rm -rf *
	git clone ${TARGET_PLATFORM_URL}
	git clone ${SRC_URL}

	echo "Importing target platform..."
	../buckminster importtarget --active gmc-src/com.gluster.storage.management.releng/gluster-management-console.target
	echo "Importing component query for glustermc..."
	../buckminster import gmc-src/com.gluster.storage.management.releng/com.gluster.storage.management.console.feature.webstart.cquery
	cd -
}

build()
{
	echo "Cleaning build directory..."
	rm -rf ${BUILD_DIR}/*

	echo "Exporting the product..."
	./buckminster perform --properties ${PROPERTIES_FILE} ${MAIN_FEATURE}#create.eclipse.jnlp.product
	./buckminster perform --properties ${PROPERTIES_FILE} ${MAIN_FEATURE}#copy.root.files

	# buckminster signs the jars using eclipse certificate - hence unsign and sign them again
	echo "Signing product jars..."
	./buckminster perform --properties ${PROPERTIES_FILE} ${MAIN_FEATURE}#unsign.jars
	./buckminster perform --properties ${PROPERTIES_FILE} ${MAIN_FEATURE}#sign.jars
}

#-----------------------------------
# Main Action Body
#-----------------------------------

echo
if [ $# -ne 2 ]; then
	echo "Usage: ${0} <build-type> <build-directory>"
	echo "build-type value can be one of:"
	echo "	`startBold` ${TYPE_ALL}`stopBold` - Sets up the build directoryand then triggers a full build"
	echo "	`startBold` ${TYPE_SETUP}`stopBold` - Sets up the build directory; doesn't trigger build"
	echo "	`startBold` ${TYPE_BUILD}`stopBold` - Assumes that build directory is set up and simply triggers the build"
	echo
	exit ${USAGE_ERR}
fi

BUILD_MODE=${1}
BUCKMINSTER_HOME=${2}
WORKSPACE_DIR=${BUCKMINSTER_HOME}/workspace
PROPERTIES_FILE=${WORKSPACE_DIR}/gmc-src/com.gluster.storage.management.releng/glustermc_build.properties

if [ "${BUILD_MODE}" == "${TYPE_ALL}" -o "${BUILD_MODE}" == "${TYPE_SETUP}" ]; then
	get_director
	install_buckminster
fi

if [ "${BUILD_MODE}" == "${TYPE_ALL}" -o "${BUILD_MODE}" == "${TYPE_BUILD}" ]; then
	configure_workspace
	build
fi
