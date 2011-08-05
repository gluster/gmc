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

    if not VolumeUtils.writeVolumeCifsConfiguration(volumeName, userList):
        sys.exit(1)
    if Utils.runCommand("service smb reload") != 0:
        Utils.log("Failed to reload smb service")
        sys.exit(2)
    sys.exit(0)


if __name__ == "__main__":
    main()
