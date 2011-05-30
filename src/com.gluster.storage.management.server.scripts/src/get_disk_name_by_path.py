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
import syslog
import Common
from DiskUtils import *
from XmlHandler import ResponseXml


def getmountpoint(path):
    if not path:
        Common.log(syslog.LOG_ERR, "Not a valid path:%s" % path)
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
        Common.log(syslog.LOG_ERR, "failed to find mount point of the given path:%s" % path)
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
        print >> sys.stderr, "usage: %s <path>" % sys.argv[0]
        sys.exit(-1)

    path = sys.argv[1]
    print getmountpoint(path)
    sys.exit(0)

if __name__ == "__main__":
    main()

