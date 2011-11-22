ROOT_DIR=glustermg-backend-${VERSION}
DIR_NAME=${ROOT_DIR}/gmg-scripts
TAR_NAME=${ROOT_DIR}.tar

prepare-script-dir()
{
	if [ -d ${DIR_NAME} ]; then
		rm -rf ${DIR_NAME}
	fi
	mkdir -p ${DIR_NAME}
}

get-scripts()
{
	cd ${DIR_NAME}
	cp ../../src/com.gluster.storage.management.gateway.scripts/src/common/* .
	cp ../../src/com.gluster.storage.management.gateway.scripts/src/backend/* .
	chmod +x *
	cd -
}

#---------------------------------------------
# Main Action Body
#---------------------------------------------
if [ $? -eq 1 ]; then
	DIST_DIR=${1}
fi
echo "Packaging Gluster Management Gateway Back-end Scripts..."

prepare-script-dir
get-scripts

/bin/rm -rf ${TAR_NAME} ${TAR_NAME}.gz
tar cvf ${TAR_NAME} ${ROOT_DIR}
gzip ${TAR_NAME}
if [ ! -z "${DIST_DIR}" ]; then
	mv ${TAR_NAME}.gz ${DIST_DIR}
fi
/bin/rm -rf ${ROOT_DIR}

echo "Done!"
