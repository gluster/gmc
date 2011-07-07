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
import time
from XmlHandler import ResponseXml
import DiskUtils
import Utils
from optparse import OptionParser

def clearVolumeDirectory(diskMountPoint, volumeName, todelete):
    rs = ResponseXml()
    if not DiskUtils.checkDiskMountPoint(diskMountPoint):
        Utils.log("failed to find disk mount point %s" % diskMountPoint) 
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "Error: Mount point does not exists")
        return rs.toprettyxml()

    if not os.path.exists(diskMountPoint):
        rs.appendTagRoute("status.code", "-2")
        rs.appendTagRoute("status.message", "Error: Mount point path does not exists")
        return rs.toprettyxml()

    # clear volume directory from the disk
    volumeDirectory = "%s/%s" % (diskMountPoint, volumeName)
    if not os.path.exists(volumeDirectory):
        rs.appendTagRoute("status.code", "-3")
        rs.appendTagRoute("status.message", "Error: Volume directory does not exists")
        return rs.toprettyxml()

    newVolumeDirectoryName = "%s_%s" % (volumeDirectory, time.time())
    command = ["sudo", "mv", "-f", volumeDirectory, newVolumeDirectoryName]
    rv = Utils.runCommandFG(command, stdout=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Utils.log("failed to rename volume directory %s, %s" % (volumeDirectory, error))
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toprettyxml()

    if not todelete:
        rv["Status"] = "0"
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toprettyxml()

    command = ["sudo", "rm", "-fr", newVolumeDirectoryName]
    rv = Utils.runCommandFG(command, stdout=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Utils.log("failed to clear volume directory %s, %s" % (newVolumeDirectoryName, error))
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toprettyxml()

    if not rv["Status"]:
        rv["Status"] = "0"
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toprettyxml()

def main():
    parser = OptionParser()
    parser.add_option("-d", "--delete", dest="deletedir", action="store_true", default=False, help="force delete")
    (options, args) = parser.parse_args()

    if len(args) != 2:
        sys.stderr.write("usage: %s <disk mount point> <volume name> [-d/--delete]\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    diskMountPoint = args[0]
    volumeName = args[1]
    print clearVolumeDirectory(diskMountPoint, volumeName, options.deletedir)
    sys.exit(0)

if __name__ == "__main__":
    main()
