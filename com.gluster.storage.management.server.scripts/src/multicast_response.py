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

import socket
import struct
import Globals

def response(multiCastGroup, port):
    # waiting for the request!
    socketRequest = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    socketRequest.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    socketRequest.bind(('', port))
    mreq = struct.pack("4sl", socket.inet_aton(multiCastGroup), socket.INADDR_ANY)
    socketRequest.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

    socketSend = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    socketSend.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)

    #TODO: Remove infinite loop and make this as a deamon (service)
    while True:
        request = socketRequest.recvfrom(1024)
        if request and request[0].upper() == "SERVERDISCOVERY":
            socketSend.sendto(socket.gethostname(), (multiCastGroup, port))
            request = None

def main():
    response(Globals.MULTICAST_GROUP, Globals.MULTICAST_PORT)

if __name__ == "__main__":
    main()
