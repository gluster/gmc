#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import os
import Utils
from DiskUtils import *
from XmlHandler import ResponseXml


def getmountpoint(path):
    if not path:
        Utils.log("Not a valid path:%s" % path)
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "Error: given path name is empty")
        return rs.toprettyxml()

    rs = ResponseXml()
    mountPoint = None
    fsTabEntry = None
    for line in readFsTab():
        if path.startswith(line['MountPoint']):
            if not mountPoint:
                mountPoint = line['MountPoint']
                fsTabEntry = line
            if len(line['MountPoint']) > len(mountPoint):
                mountPoint = line['MountPoint']
                fsTabEntry = line

    if "/" == mountPoint or not mountPoint:
        Utils.log("failed to find mount point of the given path:%s" % path)
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "Error: Unable to find disk mount point")
        return rs.toprettyxml()

    rs.appendTagRoute("status.code", "0")
    if fsTabEntry["Device"].startswith("UUID="):
        rs.appendTagRoute("status.message", getDiskPartitionByUuid(fsTabEntry["Device"].split("UUID=")[-1]))
    else:
        rs.appendTagRoute("status.message", "Unable to find disk name")
    return rs.toprettyxml()

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s <path>\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    path = sys.argv[1]
    print getmountpoint(path)
    sys.exit(0)

if __name__ == "__main__":
    main()

