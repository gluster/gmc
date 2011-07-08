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
import subprocess
import Utils
import DiskUtils
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

    ## try:
    ##     process = subprocess.Popen(command,
    ##                                stdout=fptr,
    ##                                stderr=subprocess.PIPE,
    ##                                stdin=subprocess.PIPE,
    ##                                close_fds=True)
    ##     status = process.wait()
    ## except OSError:
    ##     os.unlink(deviceFormatOutputFile)
    ##     Utils.log(syslog.LOG_ERR, "formatting disk command failed. command: %s" % str(command))
    ##     writeStatus(deviceFormatStatusFile, "Formatting disk command failed\n")
    ##     removeLockFile()
    ##     sys.exit(-5)

    if status != 0:
        Utils.removeFile(deviceFormatOutputFile)
        Utils.removeFile(deviceFormatLockFile)
        writeStatus(deviceFormatStatusFile, "Device format failed\n")
        sys.exit(6)

    if Utils.runCommand("/usr/bin/lshal") != 0:
        Utils.log("failed running /usr/bin/lshal")
    writeStatus(deviceFormatStatusFile, "Completed\n")
    Utils.removeFile(deviceFormatOutputFile)
    Utils.removeFile(deviceFormatLockFile)
    sys.exit(0)

if __name__ == "__main__":
    main()
