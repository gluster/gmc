#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Console.
#

import os
import sys
import Utils

def main():
    if len(sys.argv) != 3:
        sys.stderr.write("usage: %s VOLUME_NAME BRICK_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeName = sys.argv[1]
    brickName = sys.argv[2]
    pidFile = "/etc/glusterd/vols/%s/run/%s.pid" % (volumeName, brickName.replace(":", "").replace("/", "-"))

    if not os.path.exists(pidFile):
        print "OFFLINE"
    else:
        try:
            fp = open(pidFile)
            pidString = fp.readline()
            fp.close()
            os.getpgid(int(pidString))
            print "ONLINE"
        except IOError, e:
            Utils.log("failed to open file %s: %s" % (pidFile, str(e)))
            print "UNKNOWN"
        except ValueError, e:
            Utils.log("invalid pid %s in file %s: %s" % (pidString, pidFile, str(e)))
            print "UNKNOWN"
        except OSError, e:
            #Utils.log("failed to get process detail of pid %s: %s" % (pidString, str(e)))
            print "OFFLINE"
    sys.exit(0)

if __name__ == "__main__":
    main()
