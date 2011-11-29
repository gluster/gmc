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
import struct
import signal
import time
import Utils
import Globals

PID_FILE = "/var/run/multicast-discoverd.pid"
GLUSTERD_UUID = "NA"

def exitHandler(signum, frame):
    try:
        if os.path.exists(PID_FILE):
            os.unlink(PID_FILE)
    except OSError, e:
        Utils.log("Failed to remove PID file %s: %s" % (PID_FILE, str(e)))
    sys.exit(0)


def updateGlusterdUuid(signum, frame):
    lines = Utils.readFile("/etc/glusterd/glusterd.info", lines=True)
    for line in lines:
        if line.strip().startswith("UUID="):
            GLUSTERD_UUID = line.strip().split("=")[1]
            return
    GLUSTERD_UUID = "NA"


def isInPeer():
    status = Utils.runCommand("gluster peer status", output=True)
    if status["Status"] == 0:
        if status["Stdout"].strip().upper() != "NO PEERS PRESENT":
            return True
    return False


def main():
    if os.path.exists(PID_FILE):
        sys.stderr.write("fatal: PID file %s exists\n" % PID_FILE)
        sys.exit(-1)
    if not Utils.daemonize():
        sys.stderr.write("fatal: unable to run as daemon\n")
        sys.exit(-1)
    try:
        fp = open(PID_FILE, "w")
        fp.write("%s\n" % os.getpid())
        fp.close()
    except IOError, e:
        Utils.log("failed to create PID file %s: %s" % (PID_FILE, str(e)))
        sys.exit(1)

    updateGlusterdUuid(None, None)

    signal.signal(signal.SIGINT, exitHandler)
    signal.signal(signal.SIGTERM, exitHandler)
    signal.signal(signal.SIGHUP, exitHandler)

    multicastSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
    multicastSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    multicastSocket.bind(('', Globals.MULTICAST_PORT))
    multicastSocket.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP,
                               struct.pack("4sl", socket.inet_aton(Globals.MULTICAST_GROUP),
                                           socket.INADDR_ANY))

    while True:
        request = multicastSocket.recvfrom(Globals.DEFAULT_BUFSIZE)
        if not request:
            continue
        #print "received [%s] from %s" % (request[0], request[1])
        tokens = request[0].strip().split(",")
        if len(tokens) != 3:
            continue
        if tokens[0] != Globals.GLUSTER_PROBE_STRING:
            continue
        if isInPeer():
            continue

        time.sleep(0.2)
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((request[1][0], Globals.SERVER_PORT))
            sock.send("%s,%s,%s,%s,%s,%s\n" % (tokens[0], tokens[1], tokens[2], socket.gethostname(), socket.getfqdn(), GLUSTERD_UUID))
            sock.close()
        except socket.error, e:
            Utils.log("failed to send reply to [%s:%s]: %s" % (request[1][0], Globals.SERVER_PORT, str(e)))
    sys.exit(0)


if __name__ == "__main__":
    main()
