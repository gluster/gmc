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
import DiskUtils
import Utils


def main():
    if len(sys.argv) != 3:
        sys.stderr.write("usage: %s <disk name> <volume name>\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    disk = sys.argv[1]
    volumeName = sys.argv[2]

    # Retrieving disk uuid
    diskUuid = DiskUtils.getUuidByDiskPartition(DiskUtils.getDevice(disk))

    if not diskUuid:
        Utils.log("failed to find disk:%s uuid" % disk)
        sys.stderr.write("failed to find disk:%s uuid\n" % disk)
        sys.exit(1)

    # Retrieving disk mount point using disk uuid
    diskMountPoint = DiskUtils.getMountPointByUuid(diskUuid)
    if not os.path.exists(diskMountPoint):
        Utils.log("failed to retrieve disk:%s mount point" % disk) 
        sys.stderr.write("failed to retrieve disk:%s mount point\n" % disk)
        sys.exit(2)

    # creating volume directory under disk mount point
    volumeDirectory = "%s/%s" % (diskMountPoint, volumeName)
    if os.path.exists(volumeDirectory):
        Utils.log("Volume directory:%s already exists" % (volumeDirectory))
        sys.stderr.write("Volume directory:%s already exists\n" % (volumeDirectory))
        sys.exit(3)

    rv = Utils.runCommand("mkdir %s" % volumeDirectory, root=True)
    if rv != 0:
        sys.stderr.write("Failed to create volume directory\n")
    sys.exit(rv)

if __name__ == "__main__":
    main()
