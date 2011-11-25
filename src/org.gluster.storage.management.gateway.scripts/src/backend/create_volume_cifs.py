#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import Globals
import Utils
import VolumeUtils

def main():
    if len(sys.argv) < 3:
        sys.stderr.write("usage: %s VOLUME_NAME USER1 USER2 ...\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeName = sys.argv[1]
    userList = sys.argv[2:]

    volumeMountDirName = "%s/%s" % (Globals.REEXPORT_DIR, volumeName)
    try:
        if not os.path.exists(volumeMountDirName):
            os.mkdir(volumeMountDirName)
    except OSError, e:
        Utils.log("failed creating %s: %s\n" % (volumeMountDirName, str(e)))
        sys.stderr.write("Failed creating %s: %s\n" % (volumeMountDirName, str(e)))
        sys.exit(1)

    if not VolumeUtils.writeVolumeCifsConfiguration(volumeName, userList):
        sys.stderr.write("Failed to write volume cifs configuration\n")
        sys.exit(2)

    if Utils.runCommand("service smb reload") != 0:
        Utils.log("Failed to reload smb service")
        sys.stderr.write("Failed to reload smb service\n")
        sys.exit(3)
    sys.exit(0)


if __name__ == "__main__":
    main()
