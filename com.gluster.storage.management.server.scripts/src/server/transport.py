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
import syslog
import signal
import datetime
from Globals import *
from Protocol import *
from TransportAgent import *
from optparse import OptionParser

class TimeoutException(Exception):
    pass

def timeoutSignal(signum, frame):
    raise TimeoutException, "Timed out"

def main():
    openLog(Globals.TRANSPORT_AGENT_LOG_FILE)
    parser = OptionParser(version="%transport " + Globals.GLUSTER_PLATFORM_VERSION)
    parser.add_option("-f", "--force",
                      action="store_true", dest="force", default=False,
                      help="Execute command forcefully")

    parser.add_option("-t", "--timeout",
                      type="int", nargs=1, dest="timeout",
                      help="Session time-out")

    parser.add_option("--debug",
                      action="store_true", dest="debug", default=False,
                      help="Enable debug mode")
    (options, args) = parser.parse_args()
    Globals.GLUSTER_DEBUG = options.debug

    if  len(args) != 1:
        #print "usage: Transport.py [-f | --force] [-t N | --timeout=N] [--debug] <input-file>"
        log(syslog.LOG_ERR, "invalid arguments")
        sys.exit(-1)

    signal.signal(signal.SIGALRM, timeoutSignal)
    signal.alarm(options.timeout)
    inputFile = args[0]
    #outputFile = args[1]
    try:
        requestString = open(inputFile).read()
        if not requestString:
            sys.exit(-1)
        fp = open("/tmp/transport.log", "a")
        fp.write("\n%s: Send: %s\n" % (str(datetime.now()), requestString))
        fp.close()
    except IOError:
        log(syslog.LOG_ERR, "Unable to read input xml file %s" % inputFile)
        sys.exit(-1)

    requestDom = RequestXml(requestString)
    if not requestDom:
        log(syslog.LOG_ERR, "error: invalid request: %s" % requestString)
        sys.exit(-1)
    
    responseDom = processRequest(requestDom)
    if not responseDom:
        log(syslog.LOG_ERR, "command execution failed")
        sys.exit(-1)

    #fp = open("/tmp/transport.log", "a")
    #fp.write("%s: Receive: %s\n" % (str(datetime.now()), responseDom.toxml()))
    #fp.close()

    #responseDom.writexml(outputFile)
    print responseDom.toxml()
    sys.exit(0)

if __name__ == "__main__":
    try:
        main()
    except TimeoutException:
        log(syslog.LOG_ERR, "session timed out")
        sys.exit(-1)
