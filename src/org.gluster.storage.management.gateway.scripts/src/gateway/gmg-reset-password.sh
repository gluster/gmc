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
