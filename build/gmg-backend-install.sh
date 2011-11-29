#!/bin/bash

#------------------------------------------------------------------
# Copyright (c) 2006-2011 Gluster, Inc. <http://www.gluster.com>
# This file is part of Gluster Management Gateway.
#
# Gluster Management Gateway is free software; you can redistribute
# it and/or modify it under the terms of the GNU General Public
# License as published by the Free Software Foundation; either
# version 3 of the License, or (at your option) any later version.
#
# Gluster Management Gateway is distributed in the hope that it
# will be useful, but WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE.  See the GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see
# <http://www.gnu.org/licenses/>.
#------------------------------------------------------------------

# Variables
USAGE_ERR=1
FUSE_ERR=2
TAR_ERR=3
GMGBE_ROOT_DIR="/opt/glustermg"

function quit()
{
	echo ${1}
	echo
	exit ${2} 
}

function pre()
{
	modprobe -q fuse
	if ! lsmod | grep -qw fuse; then
    	quit "FATAL: fuse kernel module is not found." ${FUSE_ERR}
	fi
}

function check_tar_gz()
{
    file $GMGBE_ARCHIVE_PATH | grep "gzip" > /dev/null;
    if [ $? != 0 ] ; then
		quit "The given filename is not a gunzipped tarball. The file name must be of the form glustermg-backend-version.tar.gz" ${TAR_ERR}
    fi
}

function get_gmg_version()
{
	# Format is /path/to/glustermg-backend-version.tar.gz
	# Remove prefix
	PART1=${GMGBE_ARCHIVE_PATH#*glustermg-backend-}
	# Remove suffix
	GMG_VERSION=${PART1%.tar.gz}

	GMGBE_DIR="${GMGBE_ROOT_DIR}/${GMG_VERSION}/backend";
}

function make_dirs()
{
    mkdir -p $GMGBE_DIR /var/lib/rrd
}

function extract_archive()
{
	tar xvfz ${GMGBE_ARCHIVE_PATH}

	# The tar contains files in path glustermg-backend-version/gmg-scripts/*.py
	SRC_DIR=glustermg-backend-${GMG_VERSION}
	mv ${SRC_DIR}/gmg-scripts/* ${GMGBE_DIR}
	rm -rf ${SRC_DIR}
}

function create_links()
{
	ln -fs ${GMGBE_DIR}/multicast-discoverd.py /usr/sbin/multicast-discoverd
	ln -fs ${GMGBE_DIR}/gluster_cifs_volume_startup.py /usr/sbin/gluster_cifs_volume_startup
	ln -fs ${GMGBE_DIR}/multicast-discoverd.init.d /etc/init.d/multicast-discoverd
	ln -fs ${GMGBE_DIR}/gluster-volume-settings.init.d /etc/init.d/gluster-volume-settings
}

function post()
{
	if [ -f /etc/sudoers ]; then
		chmod 644 /etc/sudoers
		sed -i '/^Defaults.*requiretty/d' /etc/sudoers
		chmod 0440 /etc/sudoers
	fi

	if ! grep -q rrd_cpu.pl /etc/crontab; then
		echo '*/5 * * * * root /opt/glustermg/%{GMG_VERSION}/backend/rrd_cpu.pl' >> /etc/crontab
	fi
	if ! grep -q rrd_mem.pl /etc/crontab; then
		echo '*/5 * * * * root /opt/glustermg/%{GMG_VERSION}/backend/rrd_mem.pl' >> /etc/crontab
	fi
	if ! grep -q rrd_net.pl /etc/crontab; then
		echo '*/5 * * * * root /opt/glustermg/%{GMG_VERSION}/backend/rrd_net.pl' >> /etc/crontab
	fi
	/sbin/chkconfig --add multicast-discoverd
	/sbin/chkconfig --level 345 multicast-discoverd on
	if /etc/init.d/multicast-discoverd status >/dev/null; then
		/etc/init.d/multicast-discoverd restart
	else
		/etc/init.d/multicast-discoverd start
	fi
	/etc/init.d/crond reload
	/sbin/chkconfig smb on
	/sbin/chkconfig --add gluster-volume-settings
}

#-----------------------------------
# Main Action Body
#-----------------------------------

if [ $# -ne 1 ]; then
	quit "Usage: $0 <path to glustermg-backend-version.tar.gz>" ${USAGE_ERR}
fi

GMGBE_ARCHIVE_PATH=${1}

pre
check_tar_gz
get_gmg_version

make_dirs
extract_archive
create_links

