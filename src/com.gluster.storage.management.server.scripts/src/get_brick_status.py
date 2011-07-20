#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Console.
#

import os
import sys
import Utils
from XmlHandler import ResponseXml

def main():
    if len(sys.argv) != 3:
        sys.stderr.write("usage: %s VOLUME_NAME BRICK_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeName = sys.argv[1]
    brickName = sys.argv[2]
    pidFile = "/etc/glusterd/vols/%s/run/%s.pid" % (volumeName, brickName.replace(":", "-").replace("/", "-"))

    responseDom = ResponseXml()
    responseDom.appendTagRoute("volumeName", volumeName)
    responseDom.appendTagRoute("brickName", brickName)
    if not os.path.exists(pidFile):
        responseDom.appendTagRoute("brickStatus", "OFFLINE")
    else:
        try:
            fp = open(pidFile)
            pidString = fp.readline()
            fp.close()
            os.getpgid(int(pidString))
            responseDom.appendTagRoute("brickStatus", "ONLINE")
        except IOError, e:
            Utils.log("failed to open file %s: %s" % (pidFile, str(e)))
            responseDom.appendTagRoute("brickStatus", "UNKNOWN")
        except ValueError, e:
            Utils.log("invalid pid %s in file %s: %s" % (pidString, pidFile, str(e)))
            responseDom.appendTagRoute("brickStatus", "UNKNOWN")
        except OSError, e:
            #Utils.log("failed to get process detail of pid %s: %s" % (pidString, str(e)))
            responseDom.appendTagRoute("brickStatus", "OFFLINE")
    print responseDom.toxml()
    sys.exit(0)

if __name__ == "__main__":
    main()
