#!/bin/bash
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway (GlusterMG).
#
#  GlusterMG is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 3 of the License,
#  or (at your option) any later version.
#
#  GlusterMG is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.
#
# chkconfig: - 90 10
### BEGIN INIT INFO
# Required-Start: $network syslog glusterd
# Default-Start:  3 5
# Description: Setup/cleanup CIFS settings of Gluster volumes
### END INIT INFO

case "$1" in
    start)
	modprobe -q fuse
	sleep 3
	if ! lsmod | grep -qw fuse; then
	    echo "FATAL: fuse kernel module is not found.  Gluster CIFS volume access will not work" >&2
	    exit 1
	fi
	/usr/sbin/gluster_cifs_volume_startup
	;;
esac
