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
import syslog
from XmlHandler import ResponseXml
import DiskUtils
import Utils
import Common

def createDirectory(disk, volumeName):

    # Retrieving disk uuid
    diskUuid = DiskUtils.getUuidByDiskPartition(DiskUtils.getDevice(disk))

    rs = ResponseXml()
    if not diskUuid:
        Common.log(syslog.LOG_ERR, "failed to find disk:%s uuid" % disk)
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "Error: Unable to find disk uuid")
        return rs.toprettyxml()

    # Retrieving disk mount point using disk uuid
    diskMountPoint = DiskUtils.getMountPointByUuid(diskUuid)
    if not os.path.exists(diskMountPoint):
        Common.log(syslog.LOG_ERR, "failed to retrieve disk:%s mount point" % disk) 
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "Error: Failed to retrieve disk details")
        return rs.toprettyxml()

    # creating volume directory under disk mount point
    volumeDirectory = "%s/%s" % (diskMountPoint, volumeName)
    if not os.path.exists(volumeDirectory):
        command = ["sudo", "mkdir", volumeDirectory]
        rv = Utils.runCommandFG(command, stdout=True, root=True)
        message = Common.stripEmptyLines(rv["Stdout"])
        if rv["Stderr"]:
            error = Common.stripEmptyLines(rv["Stderr"])
            message += "Error: [%s]" % (error)
            Common.log(syslog.LOG_ERR, "failed to create volume directory %s, %s" % (volumeDirectory, error))
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toprettyxml()

def main():
    if len(sys.argv) != 3:
        print >> sys.stderr, "usage: %s <disk name> <volume name>" % sys.argv[0]
        sys.exit(-1)

    disk = sys.argv[1]
    volumeName = sys.argv[2]
    print createDirectory(disk, volumeName)
    sys.exit(0)

main()