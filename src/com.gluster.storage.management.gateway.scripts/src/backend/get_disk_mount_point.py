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
import Utils
import FsTabUtils
from XmlHandler import ResponseXml


def getmountpoint(path):
    if not path:
        Utils.log("Not a valid path:%s" % path)
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "Error: given path name is empty")
        return rs.toprettyxml()

    rs = ResponseXml()
    mountPoint = None

    for line in FsTabUtils.readFsTab():
        if path.startswith(line['MountPoint']):
            if not mountPoint:
                mountPoint = line['MountPoint']
            if len(line['MountPoint']) > len(mountPoint):
                mountPoint = line['MountPoint']

    if "/" == mountPoint or not mountPoint:
        Utils.log("failed to find mount point of the given path:%s" % path)
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "Error: Unable to find disk mount point")
        return rs.toprettyxml()

    rs.appendTagRoute("status.code", "0")
    rs.appendTagRoute("status.message", mountPoint)
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
