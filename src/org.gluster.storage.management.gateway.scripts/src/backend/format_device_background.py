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
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import Utils
import FsTabUtils
import DiskUtils

def writeStatus(deviceFormatStatusFile, message):
    try:
        fp = open(deviceFormatStatusFile, "w")
        fp.write(message)
        fp.close()
    except IOError, e:
        Utils.log("Failed to update log file %s: %s" % (deviceFormatStatusFile, str(e)))
        return False
    return True

def main():
    if len(sys.argv) != 4:
        sys.stderr.write("usage: %s FSTYPE MOUNT_POINT DEVICE_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    fsType = sys.argv[1]
    mountPoint = sys.argv[2]
    device = DiskUtils.getDevice(sys.argv[3])

    deviceFormatLockFile = Utils.getDeviceFormatLockFile(device)
    deviceFormatStatusFile = Utils.getDeviceFormatStatusFile(device)
    deviceFormatOutputFile = Utils.getDeviceFormatOutputFile(device)

    if os.path.exists(deviceFormatStatusFile):
        Utils.log("device format status file %s exists" % deviceFormatStatusFile)
        sys.exit(1)

    if os.path.exists(deviceFormatLockFile):
        Utils.log("device format lock file %s exists" % deviceFormatLockFile)
        sys.exit(2)

    try:
        fp = open(deviceFormatLockFile, "w")
        fp.close()
    except OSError, e:
        Utils.log("failed to create lock file %s: %s" % (deviceFormatLockFile, str(e)))
        writeStatus(deviceFormatStatusFile, "Lock file creation failed\n")
        sys.exit(-2)

    try:
        fptr = open(deviceFormatOutputFile, 'w')
    except IOError, e:
        Utils.log("failed to create output file %s" % deviceFormatOutputFile)
        writeStatus(deviceFormatStatusFile, "Output file creation failed\n")
        Utils.removeFile(deviceFormatLockFile)
        sys.exit(-3)

    if fsType in ['ext3', 'ext4', 'ext4dev']:
        command = "/sbin/mkfs.%s -F -I 512 %s" % (fsType, device)
    elif fsType == "xfs":
        command = "/sbin/mkfs.%s -f -i size=512 %s" % (fsType, device)
    else:
        command = "/sbin/mkfs.%s %s" % (fsType, device)

    status = Utils.runCommand(command, output=True, root=True)
    if status["Status"] != 0:
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        writeStatus(deviceFormatStatusFile, "Device format failed\n")
        sys.exit(3)

    if Utils.runCommand("udevadm trigger") != 0:
        Utils.log("failed running udevadm trigger")

    if Utils.runCommand("/usr/bin/lshal") != 0:
        Utils.log("failed running /usr/bin/lshal")

    deviceUuid = DiskUtils.getUuidByDiskPartition(device)
    if not deviceUuid:
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        Utils.log("UUID not found after device %s formatted" % device)
        writeStatus(deviceFormatStatusFile, "UUID not found after device %s formatted\n" % sys.argv[3])
        sys.exit(4)

    if DiskUtils.isDataDiskPartitionFormatted(device):
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        Utils.log("UUID device %s already has an entry in fstab" % device)
        writeStatus(deviceFormatStatusFile, "UUID device %s already has an entry in fstab\n" % sys.argv[3])
        sys.exit(5)

    newFsTabEntry = {"Device" : "UUID=%s" % deviceUuid,
                     "MountPoint" : mountPoint,
                     "FsType" : fsType,
                     "Options" : "defaults",
                     "DumpOption" : "0",
                     "fsckOrder" : "2"}
    if fsType in ['ext3', 'ext4', 'ext4dev']:
        newFsTabEntry["Options"] = "defaults,user_xattr"
    if not FsTabUtils.addFsTabEntry(newFsTabEntry):
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        writeStatus(deviceFormatStatusFile, "failed to update fstab")
        sys.exit(6)

    status = Utils.runCommand("mount %s" % mountPoint, output=True, root=True)
    if status["Status"] != 0:
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        Utils.log("Mounting device %s on %s failed" % (device, mountPoint))
        writeStatus(deviceFormatStatusFile, "Mounting device %s on %s failed\n" % (sys.argv[3], mountPoint))
        sys.exit(7)

    writeStatus(deviceFormatStatusFile, "Completed\n")
    Utils.removeFile(deviceFormatOutputFile)
    Utils.removeFile(deviceFormatLockFile)
    sys.exit(0)

if __name__ == "__main__":
    main()
