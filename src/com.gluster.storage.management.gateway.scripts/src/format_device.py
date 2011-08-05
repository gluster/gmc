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
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import Globals
import Utils
import DiskUtils
from optparse import OptionParser


def main():
    if Utils.runCommand("wget -t 1 -T 1 -q -O /dev/null %s" % Globals.AWS_WEB_SERVICE_URL) == 0:
        sys.stderr.write("format device unsupported")
        sys.exit(1)

    parser = OptionParser()
    parser.add_option("-t", "--type", action="store", type="string", dest="fstype")
    (options, args) = parser.parse_args()

    if len(args) != 1:
        sys.stderr.write("usage: %s [-t FSTYPE] DEVICE_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    device = DiskUtils.getDevice(args[0])
    deviceFormatLockFile = Utils.getDeviceFormatLockFile(device)
    deviceFormatStatusFile = Utils.getDeviceFormatStatusFile(device)
    deviceFormatOutputFile = Utils.getDeviceFormatOutputFile(device)

    if DiskUtils.isDataDiskPartitionFormatted(device):
        sys.stderr.write("Device already formatted\n")
        sys.exit(2)

    if os.path.exists(deviceFormatStatusFile):
        Utils.log("format status file %s exists" % deviceFormatStatusFile)
        try:
            fp = open(deviceFormatStatusFile)
            line = fp.read()
            fp.close()
            if line.strip().upper() == "COMPLETED":
                sys.stderr.write("Device already formatted\n")
                sys.exit(3)
            else:
                sys.stderr.write("Device format already running\n")
                sys.exit(4)
        except IOError, e:
            Utils.log("failed to read format status file %s: %s" % (deviceFormatStatusFile, str(e)))
            sys.stderr.write("%s\n" % str(e))
            sys.exit(-2)

    if os.path.exists(deviceFormatLockFile):
        Utils.log("lock file %s exists" % deviceFormatLockFile)
        sys.stderr.write("Device format already running\n")
        sys.exit(5)

    if options.fstype:
        command = ["%s/gluster_provision_block_wrapper.py" % p1, "-t", "%s" % (options.fstype), "%s" % (device)]
    else:
        command = ["%s/gluster_provision_block_wrapper.py" % p1, "%s" % (device)]

    try:
        pid = os.fork()
    except OSError, e:
        Utils.log("failed to fork a child process: %s" % str(e))
        sys.exit(6)
    if pid == 0:
        os.execv(command[0], command)
    sys.exit(0)


if __name__ == "__main__":
    main()
