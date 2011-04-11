WAR_NAME="glustermc.war"
TAR_NAME=${WAR_NAME}.tar
SERVER_DIST_DIR="${WORKSPACE}/../../gms-test1/lastSuccessful"

prepare-dist-dir()
{
	if [ -d ${WAR_NAME} ]; then
		rm -rf ${WAR_NAME}
	fi
	mkdir ${WAR_NAME}
}

get-server-war()
{
	cd ${WAR_NAME}
	WAR_FILE=`find -L ${SERVER_DIST_DIR} -name ${WAR_NAME}`
	jar xvf ${WAR_FILE}
	cd -
}

get-dist()
{
	ARCH=${1}
	OS=${2}
	WS=${3}

	OUT_DIR="${WORKSPACE}/../../gmc-test1/workspace/arch/${ARCH}/os/${OS}/ws/${WS}/buckminster.output/com.gluster.storage.management.gui.feature.webstart*.feature/glustermc"
	NEW_DIR=${WAR_NAME}/${OS}.${WS}.${ARCH}
	cp -R ${OUT_DIR} ${NEW_DIR}
}

get-console-dists()
{
	get-dist x86 win32 win32
	get-dist x86_64 win32 win32
	get-dist x86 linux gtk
	get-dist x86_64 linux gtk
	get-dist x86 macosx cocoa
	get-dist x86_64 macosx cocoa
}

#---------------------------------------------
# Main Action Body
#---------------------------------------------
echo "Packaging Gluster Management Server..."

prepare-dist-dir
get-server-war
get-console-dists

/bin/rm -rf ${TAR_NAME} ${TAR_NAME}.gz
tar cvf ${TAR_NAME} ${WAR_NAME}
gzip ${TAR_NAME}

echo "Done!"
