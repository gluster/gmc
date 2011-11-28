#!/bin/bash

#------------------------------------------------------------------
# Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
# This file is part of GlusterFS.
# 
# Gluster Management Gateway is free software; you can redistribute 
# it and/or modify it under the terms of the GNU General Public 
# License as published by the Free Software Foundation; either 
# version 3 of the License, or (at your option) any later version.
# 
# GlusterFS is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see
# <http://www.gnu.org/licenses/>.
#------------------------------------------------------------------

# Variables
GMG_LOG_DIR="/var/log/glustermg";
GMG_ROOT_DIR="/opt/glustermg"
GMG_KEYS_DIR="${GMG_ROOT_DIR}/keys"
USAGE_ERR=1
TOMCAT_ERR=2
JAVA_ERR=3
TAR_ERR=4

function quit()
{
	echo ${1}
	echo
	exit ${2} 
} 

function post_install()
{
    if [ ! -f /etc/init.d/$TOMCAT_BIN ]; then
		echo "All operations completed. Please restart tomcat."
    else
		echo "Re-starting [${TOMCAT_BIN}].."
		service $TOMCAT_BIN restart;
    fi
	echo
}

function create_links()
{
	ln -fs ${GMG_HOME_DIR}/glustermg ${WEBAPPS_DIR}

	GMG_SCRIPTS_DIR="${GMG_HOME_DIR}/glustermg/scripts"
    ln -sf ${GMG_SCRIPTS_DIR}/grun.py /usr/sbin/grun.py
    ln -sf ${GMG_SCRIPTS_DIR}/add_user_cifs_all.py /usr/sbin/add_user_cifs_all.py
    ln -sf ${GMG_SCRIPTS_DIR}/delete_user_cifs_all.py /usr/sbin/delete_user_cifs_all.py
    ln -sf ${GMG_SCRIPTS_DIR}/setup_cifs_config_all.py /usr/sbin/setup_cifs_config_all.py
    ln -sf ${GMG_SCRIPTS_DIR}/gmg-reset-password.sh /usr/sbin/gmg-reset-password.sh
}

# Update tomcat sysconfig file with java options
function set_java_options()
{
	TOMCAT_CONFIG_FILE="/etc/sysconfig/$TOMCAT_BIN"
	if [ -f ${TOMCAT_CONFIG_FILE} ]; then
    	if ! grep -q '^JAVA_HOME="/usr/lib/jvm/jre-1.6.0-openjdk.x86_64"' ${TOMCAT_CONFIG_FILE}; then
			sed -i 's/^JAVA_HOME=/# JAVA_HOME=/g' ${TOMCAT_CONFIG_FILE}
			echo 'JAVA_HOME="/usr/lib/jvm/jre-1.6.0-openjdk.x86_64"' >> ${TOMCAT_CONFIG_FILE}
    	fi
	
    	if ! grep -q '# Added by Gluster: JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' ${TOMCAT_CONFIG_FILE}; then
			echo '# Added by Gluster: JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' >> ${TOMCAT_CONFIG_FILE}
			echo 'JAVA_OPTS="${JAVA_OPTS} -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m"' >> ${TOMCAT_CONFIG_FILE}
    	fi
	fi
}

function configure_ssl()
{
	TOMCAT_SERVER_CONFIG_FILE=${TOMCAT_DIR}/conf/server.xml
	SSL_KEYSTORE_FILE=${WEBAPPS_DIR}/glustermg/ssl/gmg-ssl.keystore
    if ! grep -q ${SSL_KEYSTORE_FILE} ${TOMCAT_SERVER_CONFIG_FILE}; then
		sed -i '/<\/Service>/i \
    	<Connector SSLEnabled="true" \
               clientAuth="false" \
               executor="tomcatThreadPool" \
               maxThreads="150" \
               port="8443" \
               keystoreFile="$TOMCAT_DIR/webapps/glustermg/ssl/gmg-ssl.keystore" \
               keystorePass="gluster" \
               protocol="org.apache.coyote.http11.Http11Protocol" \
               scheme="https" \
               secure="true" \
               sslProtocol="TLS" />' ${TOMCAT_SERVER_CONFIG_FILE}
		sed -i "s,keystoreFile=\"\$TOMCAT_DIR/webapps/glustermg/ssl/gmg-ssl.keystore\",keystoreFile=\"${SSL_KEYSTORE_FILE}\"," ${TOMCAT_SERVER_CONFIG_FILE}
    fi
}

function enable_proxy_caching()
{
    if ! grep -q "org.apache.catalina.authenticator.NonLoginAuthenticator" $TOMCAT_DIR/conf/context.xml; then
		sed -i '/<\/Context>/i \
    	<Valve className="org.apache.catalina.authenticator.NonLoginAuthenticator" \
           	disableProxyCaching="false" />' $TOMCAT_DIR/conf/context.xml
    fi
}

function configure_server()
{
	set_java_options
	enable_proxy_caching	
}

function make_dirs()
{
    mkdir -p $GMG_HOME_DIR $GMG_KEYS_DIR $GMG_LOG_DIR;
    if [ ! -f ${GMG_KEYS_DIR}/gluster.pem ]; then
		ssh-keygen -t rsa -f /opt/glustermg/keys/gluster.pem -N ''
		mv -f /opt/glustermg/keys/gluster.pem.pub /opt/glustermg/keys/gluster.pub
    fi
    chown -R tomcat:tomcat $GMG_HOME_DIR $GMG_LOG_DIR;
}

function check_tar_gz()
{
    file $GMG_ARCHIVE_PATH | grep "gzip" > /dev/null;
    if [ $? != 0 ] ; then
		quit "The given filename is not a gunzipped tarball. The file name must be of the form glustermg-version.war.tar.gz" ${TAR_ERR}
    fi
}

function get_gmg_version()
{
	# Format is /path/to/glustermg-version.war.tar.gz
	# Remove prefix
	PART1=${GMG_ARCHIVE_PATH#*glustermg-}
	# Remove suffix
	GMG_VERSION=${PART1%.war.tar.gz}

	GMG_HOME_DIR="${GMG_ROOT_DIR}/${GMG_VERSION}";
}

function check_tomcat_dir()
{
	WEBAPPS_DIR="${TOMCAT_DIR}/webapps"
    if [ ! -d "${WEBAPPS_DIR}" ]; then
		quit "There is no webapps directory in [${TOMCAT_DIR}]." ${TOMCAT_ERR}
    fi
    TOMCAT_BIN=$(basename /usr/sbin/tomcat* );
}

function check_java_version()
{
	java -version 2>/dev/null || quit "java command not available. Please make sure that Java >=1.6.0 is installed and is present in \$PATH" ${JAVA_ERR}
	JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); gsub(/_.*/, ""); print $3 }'`
	MINVERSION=1.6

	if expr ${JAVA_VERSION} \>= ${MINVERSION} > /dev/null; then
		echo "Found java version [${JAVA_VERSION}]"
	else
		quit "Java minimum version expected [${MINVERSION}], found [${JAVA_VERSION}]!" ${JAVA_ERR}
	fi
}

function install_gmg()
{
	tar -xvf ${GMG_ARCHIVE_PATH} -C ${GMG_HOME_DIR}
	create_links
}

#-----------------------------------
# Main Action Body
#-----------------------------------

if [ "x$1" == "x" ] || [ "x$1$2" == "x$1" ] || [ $# -gt 2 ]; then
	echo "Usage: $0 <path to glustermg-version.war.tar.gz> <path to tomcat directory>";
	exit 1;
fi

GMG_ARCHIVE_PATH="$1";
TOMCAT_DIR="$2";

check_tomcat_dir
check_java_version
check_tar_gz
get_gmg_version

make_dirs
install_gmg

configure_server
post_install
