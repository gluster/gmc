#!/usr/bin/python
#  Copyright (C) 2010 Gluster, Inc. <http://www.gluster.com>
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

import os
import sys
import signal
import atexit
import socket
import syslog
import pwd
from optparse import OptionParser

import Globals
import Socket
import Utils
from XmlHandler import ResponseXml


ME = os.path.basename(sys.argv[0])
PID_FILE = "/var/run/serveragent.pid"
serverSocket = None
clientSocket = None
clientAddress = None
clientInputStream = None
clientOutputStream = None


def sigTermHandler(signal, frame):
    sys.exit(0)


def cleanup():
    try:
        if os.path.exists(PID_FILE):
            os.unlink(PID_FILE)
    except OSError, e:
        Utils.log("Failed to remove PID file %s: %s" % (PID_FILE, str(e)))

    try:
        if clientSocket:
            clientSocket.close()
    except socket.error, e:
        Utils.log("Failed to close client socket: %s" % str(e))

    try:
        if serverSocket:
            serverSocket.close()
    except socket.error, e:
        Utils.log("Failed to close server socket: " % str(e))

def stripEmptyLines(content):
    ret = ""
    for line in content.split("\n"):
	    if line.strip() != "":
		    ret += line
    return ret

def executeCommand(command):
    rv = Utils.runCommandFG(command, stdout=True, root=True)
    statusCode = rv["Status"]
    if statusCode != 0:
        output = "output: [" + stripEmptyLines(rv["Stdout"]) + "] error: [" + stripEmptyLines(rv["Stderr"]) + "]";
        rs = ResponseXml()
        rs.appendTagRoute("status.code", statusCode);
        rs.appendTagRoute("status.message", output);
        return rs.toprettyxml()
    else:
        return rv["Stdout"]

def main():
    global PID_FILE
    global serverSocket
    global clientSocket
    global clientAddress
    global clientInputStream
    global clientOutputStream

    username = Globals.SERVER_AGENT_RUN_USERNAME

    Utils.openLog(Globals.PEER_AGENT_LOG_FILE)

    parser = OptionParser(version="%s %s" % (ME, Globals.GLUSTER_PLATFORM_VERSION))

    parser.add_option("-N", "--no-daemon",
                      action="store_false", dest="daemonMode", default=True,
                      help="Run in foreground")
    parser.add_option("-r", "--run-as", dest="username",
                      help="Run the daemon as USERNAME (default: %s)" % Globals.SERVER_AGENT_RUN_USERNAME,
                      metavar="USERNAME")
    (options, args) = parser.parse_args()

    if options.username:
        username = options.username
    try:
        userInfo = pwd.getpwnam(username)
    except KeyError, e:
        sys.stderr.write("%s\n" % str(e))
        serverSocket.close()
        sys.exit(-1)
    uid = userInfo.pw_uid
    gid = userInfo.pw_gid

    try:
        Utils.log("__DEBUG__ Opening server socket on port %s" % Globals.SERVER_AGENT_PORT)
        serverSocket = Socket.openServerSocket()
    except socket.error, e:
        sys.stderr.write("Failed to open server socket: %s\n" % str(e))
        sys.exit(-1)

    if options.daemonMode:
        if os.path.exists(PID_FILE):
            sys.stderr.write("fatal: %s file exists\n" % PID_FILE)
            serverSocket.close()
            sys.exit(-1)

        if not Utils.daemonize():
            sys.stderr.write("fatal: unable to run as daemon\n")
            serverSocket.close()
            sys.exit(-1)
        try:
            fp = open(PID_FILE, "w")
            fp.write("%s\n" % os.getpid())
            fp.close()
        except IOError, e:
            Utils.log("Pid file %s: %s" % (PID_FILE, str(e)))
            serverSocket.close()
            sys.exit(-1)
        try:
            os.chown(PID_FILE, uid, gid)
        except OSError, e:
            Utils.log("Pid file %s: %s" % (PID_FILE, str(e)))
            serverSocket.close()
            try:
                os.unlink(PID_FILE)
            except OSError, ex:
                Utils.log("Failed to remove PID file %s: %s" % (PID_FILE, str(ex)))
            sys.exit(-1)
    else:
        Globals.GLUSTER_DEBUG = True

    try:
        os.setregid(gid, gid)
    except OSError, e:
        Utils.log("Failed to set effective and real gid to %s: %s" % (gid, str(e)))
        cleanup()
        sys.exit(-1)
    try:
        os.setreuid(uid, uid)
    except OSError, e:
        Utils.log("Failed to set effective and real uid to %s: %s" % (uid, str(e)))
        cleanup()
        sys.exit(-1)

    atexit.register(cleanup)
    signal.signal(signal.SIGTERM, sigTermHandler)

    while True:
        Utils.log("__DEBUG__ Waiting for new connection on port %s" % Globals.SERVER_AGENT_PORT)
        try:
            clientSocket, clientAddress, clientInputStream, clientOutputStream = Socket.acceptClient(serverSocket)
        except socket.error, e:
            Utils.log("Failed to accept new connection: %s" % str(e))
            sys.exit(-1)

        Utils.log('__DEBUG__ Connected by %s' % str(clientAddress))
        try:
            requestString = Socket.readPacket(clientInputStream)
            Utils.log('__DEBUG__ Received %s' % repr(requestString))
            requestParts = requestString.split(None, 3)
                
            if "get_file" == requestParts[0]:
                if len(requestParts) != 2:
                    rs = ResponseXml()
                    rs.appendTagRoute("status.code", "-1")
                    rs.appendTagRoute("status.message", "File path not passed")
                    Socket.writePacket(clientOutputStream, rs.toprettyxml())
                else:
                    filePath = requestParts[1]
                    fp = open(filePath)
                    clientSocket.sendall(fp.read())
                    fp.close()
                clientOutputStream.flush()
            else:
                responseString = executeCommand(requestString)
                if responseString:
                    Socket.writePacket(clientOutputStream, responseString)
                    clientOutputStream.flush()
                else:
                    Utils.log('__DEBUG__ empty response string')
            Utils.log('__DEBUG__ Closing client %s' % str(clientAddress))
            clientSocket.close()
        except socket.error, e:
            Utils.log("Socket error on client: %s" % str(e))
    sys.exit(0)

if __name__ == "__main__":
    main()
