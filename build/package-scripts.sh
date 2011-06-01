DIR_NAME=gmc-scripts
TAR_NAME=${DIR_NAME}.tar

prepare-script-dir()
{
	if [ -d ${DIR_NAME} ]; then
		rm -rf ${DIR_NAME}
	fi
	mkdir ${DIR_NAME}
}

get-scripts()
{
	cd ${DIR_NAME}
	cp ../src/com.gluster.storage.management.server.scripts/src/*.py .
	cd -
}

#---------------------------------------------
# Main Action Body
#---------------------------------------------
echo "Packaging Gluster Management Console Scripts..."

prepare-script-dir
get-scripts

/bin/rm -rf ${TAR_NAME} ${TAR_NAME}.gz
tar cvf ${TAR_NAME} ${DIR_NAME}
gzip ${TAR_NAME}

echo "Done!"
