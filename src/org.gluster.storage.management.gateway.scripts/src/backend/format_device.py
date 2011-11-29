#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway (GlusterMG).
#
#  GlusterMG is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 3 of the License,
#  or (at your option) any later version.
#
#  GlusterMG is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.
#

import os
import sys
import stat
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import Globals
import Utils
import DiskUtils

SIZE_TB_16 = 17179869184L

def main():
    if Utils.runCommand("wget -t 1 -T 1 -q -O /dev/null %s" % Globals.AWS_WEB_SERVICE_URL) == 0:
        sys.stderr.write("format device unsupported\n")
        sys.exit(1)

    if len(sys.argv) != 4:
        sys.stderr.write("usage: %s FSTYPE MOUNT_POINT DEVICE_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    fsType = sys.argv[1]
    mountPoint = sys.argv[2]
    device = DiskUtils.getDevice(sys.argv[3])
    deviceName = DiskUtils.getDeviceName(sys.argv[3])

    if not os.path.exists(device):
        sys.stderr.write("device %s not found\n" % sys.argv[3])
        sys.exit(2)

    try:
        if not stat.S_ISBLK(os.stat(device).st_mode):
            sys.stderr.write("%s is not a block device\n" % sys.argv[3])
            sys.exit(3)
    except OSError, e:
        Utils.log("unable to get device %s mode: %s" % (device, str(e)))
        sys.stderr.write("unable to get device %s mode\n" % sys.argv[3])
        sys.exit(-2)

    if fsType in ['ext3', 'ext4', 'ext4dev']:
        deviceSize = DiskUtils.getProcPartitions()[deviceName]['Size']
        if deviceSize >= SIZE_TB_16:
            Utils.log("device %s, size %s is greater than %s size for fstype %s" % (device, deviceSize, SIZE_TB_16, fsType))
            sys.stderr.write("size of device %s is unsupported for fstype %s\n" % (sys.argv[3], fsType))
            sys.exit(4)

    if DiskUtils.isDataDiskPartitionFormatted(device):
        sys.stderr.write("device %s already formatted\n" % sys.argv[3])
        sys.exit(5)

    if os.path.exists(mountPoint):
        if not os.path.isdir(mountPoint):
            sys.stderr.write("mount point %s exists but not a directory" % mountPoint)
            sys.exit(6)
        procMounts = Utils.readFile("/proc/mounts")
        if procMounts.find(" %s " % mountPoint) != -1:
            sys.stderr.write("mount point %s already has a mount\n" % mountPoint)
            sys.exit(7)
        if procMounts.find(" %s/" % mountPoint) != -1:
            sys.stderr.write("mount point %s has a submount\n" % mountPoint)
            sys.exit(8)
    else:
        status = Utils.runCommand("mkdir -p %s" % mountPoint, output=True, root=True)
        if status["Status"] != 0:
            sys.stderr.write("failed to create mount point %s\n" % mountPoint)
            sys.exit(9)

    if fsType not in Utils.getFileSystemType():
        sys.stderr.write("unsupported file system type %s\n" % fsType)
        sys.exit(10)

    deviceFormatLockFile = Utils.getDeviceFormatLockFile(device)
    deviceFormatStatusFile = Utils.getDeviceFormatStatusFile(device)
    deviceFormatOutputFile = Utils.getDeviceFormatOutputFile(device)

    if os.path.exists(deviceFormatStatusFile):
        Utils.log("format status file %s exists" % deviceFormatStatusFile)
        line = Utils.readFile(deviceFormatStatusFile)
        if not line:
            sys.stderr.write("failed to read format status file %s\n" % deviceFormatStatusFile)
            sys.exit(-3)
        if line.strip().upper() == "COMPLETED":
            sys.stderr.write("Device %s already formatted\n" % sys.argv[3])
            sys.exit(11)
        else:
            sys.stderr.write("Formatting device %s already running\n" % sys.argv[3])
            sys.exit(12)

    if os.path.exists(deviceFormatLockFile):
        Utils.log("lock file %s exists" % deviceFormatLockFile)
        sys.stderr.write("Formatting device %s already running\n" % sys.argv[3])
        sys.exit(13)

    command = ["%s/format_device_background.py" % p1, fsType, mountPoint, sys.argv[3]]
    Utils.runCommandBG(command)
    sys.exit(0)


if __name__ == "__main__":
    main()
