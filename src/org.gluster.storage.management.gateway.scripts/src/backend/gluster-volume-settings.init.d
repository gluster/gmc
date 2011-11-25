#!/bin/bash
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
