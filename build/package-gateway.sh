WAR_NAME="glustermg.war"
WAR_SCRIPTS_DIR=${WAR_NAME}/scripts
NEW_WAR_NAME="glustermg"
TAR_NAME=${NEW_WAR_NAME}-${VERSION}.war.tar
SERVER_DIST_DIR=${DIST_DIR:-${WORKSPACE}/buckminster.output}

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
		OUT_DIR="${WORKSPACE}/../../${BRANCH}-glustermc/workspace/arch/${ARCH}/os/${OS}/ws/${WS}/buckminster.output/com.gluster.storage.management.console.feature.webstart*.feature/glustermc"
	else
		OUT_DIR="${GMC_DIST_DIR}/${OS}.${WS}.${ARCH}/com.gluster.storage.management.console.feature.webstart*.feature/glustermc"
	fi
	NEW_DIR=${WAR_NAME}/${OS}.${WS}.${ARCH}
	cp -R ${OUT_DIR} ${NEW_DIR}

	update-jnlp ${ARCH} ${NEW_DIR}/com.gluster.storage.management.console.feature_*.jnlp
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

get-scripts()
{
	cp src/com.gluster.storage.management.gateway.scripts/src/common/* ${WAR_SCRIPTS_DIR}
	cp src/com.gluster.storage.management.gateway.scripts/src/gateway/* ${WAR_SCRIPTS_DIR}
	chmod +x ${WAR_SCRIPTS_DIR}/*
}

#---------------------------------------------
# Main Action Body
#---------------------------------------------

if [ $# -eq 2 ]; then
	DIST_DIR=${1}
	GMC_DIST_DIR=${2}
fi

echo "Packaging GlusterFS Management Gateway..."

prepare-dist-dir
get-scripts
get-server-war
get-console-dists

/bin/mv -f ${WAR_NAME} ${NEW_WAR_NAME}
/bin/rm -rf ${TAR_NAME} ${TAR_NAME}.gz
tar cvf ${TAR_NAME} ${NEW_WAR_NAME}
gzip ${TAR_NAME}

if [ ! -z "${DIST_DIR} ]; then
	mv ${TAR_NAME}.gz ${DIST_DIR}
fi

echo "Done!"
