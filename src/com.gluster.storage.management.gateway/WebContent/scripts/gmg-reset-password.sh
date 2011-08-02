#-----------------------------------------------------------------------------------
# gmg-reset-password.sh - script to reset password of given user to default password
#-----------------------------------------------------------------------------------

USAGE_ERR=1

if [ $# -ne 1 ]; then
	echo "Usage: ${0} <username>"
	echo
	exit ${USAGE_ERR}
fi

# Main action body
cd ..
for FILE in WEB-INF/lib/*.jar
do
	export CLASSPATH=${CLASSPATH}:${PWD}/${FILE}
done
export CLASSPATH=${PWD}/WEB-INF/classes:${CLASSPATH}
cd -
java com.gluster.storage.management.gateway.utils.PasswordManager reset ${1}
