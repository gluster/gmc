#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
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
import DiskUtils


def main():
    if Utils.runCommand("wget -t 1 -T 1 -q -O /dev/null %s" % Globals.AWS_WEB_SERVICE_URL) == 0:
        sys.stderr.write("format device unsupported")
        sys.exit(1)

    if len(sys.argv) != 4:
        sys.stderr.write("usage: %s FSTYPE MOUNT_POINT DEVICE_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    fsType = sys.argv[1]
    mountPoint = sys.argv[2]
    device = DiskUtils.getDevice(sys.argv[3])

    if DiskUtils.isDataDiskPartitionFormatted(device):
        Utils.log("device %s already formatted" % device)
        sys.stderr.write("device %s already formatted\n" % sys.argv[3])
        sys.exit(2)

    if os.path.exists(mountPoint):
        if not os.path.isdir(mountPoint):
            Utils.log("mount point %s exists but not a directory" % mountPoint)
            sys.stderr.write("mount point %s exists but not a directory" % mountPoint)
            sys.exit(3)
        procMounts = Utils.readFile("/proc/mounts")
        if procMounts.find(" %s " % mountPoint) != -1:
            Utils.log("mount point %s already has a mount" % mountPoint)
            sys.stderr.write("mount point %s already has a mount\n" % mountPoint)
            sys.exit(4)
        if procMounts.find(" %s/" % mountPoint) != -1:
            Utils.log("mount point %s has a submount" % mountPoint)
            sys.stderr.write("mount point %s has a submount\n" % mountPoint)
            sys.exit(5)
    else:
        status = Utils.runCommand("mkdir -p %s" % mountPoint, output=True, root=True)
        if status["Status"] != 0:
            Utils.log("failed to create mount point %s" % mountPoint)
            sys.stderr.write("failed to create mount point %s\n" % mountPoint)
            sys.exit(6)

    if fsType not in Utils.getFileSystemType():
        Utils.log("invalid file system type %s" % fsType)
        sys.stderr.write("invalid file system type %s\n" % fsType)
        sys.exit(7)

    deviceFormatLockFile = Utils.getDeviceFormatLockFile(device)
    deviceFormatStatusFile = Utils.getDeviceFormatStatusFile(device)
    deviceFormatOutputFile = Utils.getDeviceFormatOutputFile(device)

    if os.path.exists(deviceFormatStatusFile):
        Utils.log("format status file %s exists" % deviceFormatStatusFile)
        line = Utils.readFile(deviceFormatStatusFile)
        if not line:
            sys.stderr.write("failed to read format status file %s\n" % deviceFormatStatusFile)
            sys.exit(-2)
        if line.strip().upper() == "COMPLETED":
            sys.stderr.write("Device %s already formatted\n" % sys.argv[3])
            sys.exit(8)
        else:
            sys.stderr.write("Formatting device %s already running\n" % sys.argv[3])
            sys.exit(9)

    if os.path.exists(deviceFormatLockFile):
        Utils.log("lock file %s exists" % deviceFormatLockFile)
        sys.stderr.write("Formatting device %s already running\n" % sys.argv[3])
        sys.exit(10)

    command = ["%s/format_device_background.py" % p1, fsType, mountPoint, sys.argv[3]]
    Utils.runCommandBG(command)
    sys.exit(0)


if __name__ == "__main__":
    main()
