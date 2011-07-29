#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
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
        os.mkdir(volumeMountDirName)
    except OSError, e:
        Utils.log("failed creating %s: %s\n" % (volumeMountDirName, str(e)))
        sys.exit(1)

    if VolumeUtils.writeVolumeCifsConfiguration(volumeName, userList):
        sys.exit(0)
    sys.exit(2)


if __name__ == "__main__":
    main()
