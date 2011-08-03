#!/bin/bash
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#
# chkconfig: - 85 15
# description: multicast discovery service
# processname: multicast-discoverd
# pidfile: /var/run/multicast-discoverd.pid

# Source function library.

. /etc/init.d/functions

case "$1" in
    start)
	echo -n "Starting multicast-discoverd:"
	daemon multicast-discoverd
	RETVAL=$?
	echo
	[ $RETVAL -eq 0 ] && touch /var/lock/subsys/multicast-discoverd
	;;
    stop)
	echo -n "Shutting down multicast-discoverd:"
	killproc multicast-discoverd
	RETVAL=$?
	echo
	[ $RETVAL -eq 0 ] && rm -f /var/lock/subsys/multicast-discoverd
	;;
    restart)
	$0 stop
	$0 start
	RETVAL=$?
	;;
    status)
	status multicast-discoverd
	RETVAL=$?
	;;
    reload)
	killproc multicast-discoverd -HUP
	RETVAL=$?
	echo
	;;
    *)
	echo $"Usage: $0 {start|stop|restart|status|reload}"
	exit 1
esac

exit $RETVAL
