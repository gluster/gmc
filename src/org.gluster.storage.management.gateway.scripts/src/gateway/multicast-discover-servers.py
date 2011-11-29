#!/usr/bin/python
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

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import socket
import select
import signal
import random
import string
import Utils
import Globals

running = True


def exitHandler(signum, frame):
    running = False


def sendMulticastRequest(idString):
    multicastSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    multicastSocket.setsockopt(socket.IPPROTO_IP, socket.IP_MULTICAST_TTL, 2)
    multicastSocket.sendto("%s,%s,%s\n" % (Globals.GLUSTER_PROBE_STRING, Globals.GLUSTER_PROBE_VERSION, idString),
                           (Globals.MULTICAST_GROUP, Globals.MULTICAST_PORT))
    return multicastSocket


def openServerSocket():
    try:
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.setblocking(0)
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind(('', Globals.SERVER_PORT))
        server.listen(Globals.DEFAULT_BACKLOG)
        return server
    except socket.error, e:
        Utils.log("failed to open server socket on port %s: %s" % (Globals.SERVER_PORT, str(e)))
        sys.stderr.write("failed to open server socket on port %s: %s\n" % (Globals.SERVER_PORT, str(e)))
        sys.exit(1)


def main():
    signal.signal(signal.SIGINT, exitHandler)
    signal.signal(signal.SIGTERM, exitHandler)
    signal.signal(signal.SIGALRM, exitHandler)

    idString = ''.join(random.choice(string.ascii_lowercase +
                                     string.ascii_uppercase +
                                     string.digits) for x in range(Globals.DEFAULT_ID_LENGTH))

    multicastSocket = sendMulticastRequest(idString)

    serverInfoDict = {}
    serverSocket = openServerSocket()
    rlist = [serverSocket]
    signal.alarm(Globals.DEFAULT_TIMEOUT)
    while running:
        try:
            ilist,olist,elist = select.select(rlist, [], [], 0.1)
        except select.error, e:
            Utils.log("failed to read from connections: %s" % str(e))
            break
        for sock in ilist:
            # handle new connection
            if sock == serverSocket:
                clientSocket, address = serverSocket.accept()
                clientSocket.setblocking(0)
                #print "connection from %s on %s" % (address, clientSocket)
                rlist.append(clientSocket)
                continue

            # handle all other sockets
            data = sock.recv(Globals.DEFAULT_BUFSIZE)
            if not data:
                #print "closing socket %s" % sock
                sock.close()
                rlist.remove(sock)
            tokens =  data.strip().split(",")
            if len(tokens) != 6:
                continue
            if not (tokens[0] == Globals.GLUSTER_PROBE_STRING and \
                    tokens[1] == Globals.GLUSTER_PROBE_VERSION and \
                    tokens[2] == idString):
                continue
            serverInfoDict[tokens[3]] = [tokens[4], tokens[5]]
            #print "closing socket %s" % sock
            sock.close()
            rlist.remove(sock)

    for sock in rlist:
        sock.close()

    for k, v in serverInfoDict.iteritems():
        if v[0]:
            print v[0]
        else:
            print k

    sys.exit(0)


if __name__ == "__main__":
    main()
