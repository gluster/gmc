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
import subprocess
import Utils
from optparse import OptionParser

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
    parser = OptionParser()
    parser.add_option("-t", "--type", action="store", type="string", dest="fstype")
    (options, args) = parser.parse_args()

    if len(args) != 1:
        sys.stderr.write("usage: %s [-t FSTYPE] DEVICE" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    device = args[0]
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
        sys.exit(3)

    try:
        fptr = open(deviceFormatOutputFile, 'w')
    except IOError, e:
        Utils.log("failed to create output file %s" % deviceFormatOutputFile)
        writeStatus(deviceFormatStatusFile, "Output file creation failed\n")
        Utils.removeFile(deviceFormatLockFile)
        sys.exit(4)

    if options.fstype:
        command = "gluster-provision-block -t %s %s" % (options.fstype, device)
    else:
        command = "gluster-provision-block %s" % (device)

    process = Utils.runCommandBG(command,
                                 stdinFileObj=subprocess.PIPE,
                                 stdoutFileObj=fptr,
                                 stderrFileObj=subprocess.PIPE)
    if process:
        status = process.wait()
    else:
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        writeStatus(deviceFormatStatusFile, "Device format failed\n")
        sys.exit(5)

    if status != 0:
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        writeStatus(deviceFormatStatusFile, "Device format failed\n")
        sys.exit(6)

    if Utils.runCommand("/sbin/udevtrigger") != 0:
        Utils.log("failed running /sbin/udevtrigger")

    if Utils.runCommand("/usr/bin/lshal") != 0:
        Utils.log("failed running /usr/bin/lshal")
    writeStatus(deviceFormatStatusFile, "Completed\n")
    Utils.removeFile(deviceFormatOutputFile)
    Utils.removeFile(deviceFormatLockFile)
    sys.exit(0)

if __name__ == "__main__":
    main()
