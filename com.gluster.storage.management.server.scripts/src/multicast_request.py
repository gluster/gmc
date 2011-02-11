#!/usr/bin/python
#  Copyright (C) 2009 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#
#  Gluster Storage Platform is free software; you can redistribute it
#  and/or modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3 of
#  the License, or (at your option) any later version.
#
#  Gluster Storage Platform is distributed in the hope that it will be
#  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
#  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.

import sys
import socket
import signal
import struct
import syslog
import Globals
import Common

class TimeoutException(Exception):
    pass

def timeoutSignal(signum, frame):
    raise TimeoutException, "Timed out"

def serverDiscoveryRequest(multiCastGroup, port):
    servers = []
    # Sending request to all the servers
    socketSend = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    socketSend.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
    socketSend.sendto("ServerDiscovery", (multiCastGroup, port))

    # Waiting for the response
    socketReceive = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    socketReceive.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    socketReceive.bind(('', port))
    mreq = struct.pack("4sl", socket.inet_aton(multiCastGroup), socket.INADDR_ANY)

    socketReceive.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)
    socketSend.sendto("ServerDiscovery", (multiCastGroup, port))

    try:
        while True:
            response = socketReceive.recvfrom(200)
            if response and response[0].upper() != "SERVERDISCOVERY":
                servers.append(response[0] + "\n")
            signal.signal(signal.SIGALRM, timeoutSignal)
            signal.alarm(3)
    except TimeoutException:
        return servers
    return None

def main():
    syslog.openlog("discovery server request")
    servers = serverDiscoveryRequest(Globals.MULTICAST_GROUP, Globals.MULTICAST_PORT)
    if not servers:
        Common.log(syslog.LOG_ERR, "Failed to discover new servers")
        sys.exit(-1)

    servers = set(servers)
    try:
        fp = open(Globals.DISCOVERED_SERVER_LIST_FILENAME, "w")
        fp.writelines(list(servers))
        fp.close()
    except IOError:
        Common.log(syslog.LOG_ERR, "Unable to open file %s" % Globals.DISCOVERED_SERVER_LIST_FILENAME)
        sys.exit(-1)

    #for serverName in servers:
    #    print serverName
    sys.exit(0)

if __name__ == "__main__":
    main()
