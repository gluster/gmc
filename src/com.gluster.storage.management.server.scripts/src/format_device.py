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
import Utils
import DiskUtils
from optparse import OptionParser


def main():
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
        sys.exit(1)

    if os.path.exists(deviceFormatStatusFile):
        Utils.log("format status file %s exists" % deviceFormatStatusFile)
        try:
            fp = open(deviceFormatStatusFile)
            line = fp.read()
            fp.close()
            if line.strip().upper() == "COMPLETED":
                sys.stderr.write("Device already formatted\n")
                sys.exit(1)
            else:
                sys.stderr.write("Device format already running\n")
                sys.exit(2)
        except IOError, e:
            Utils.log("failed to read format status file %s: %s" % (deviceFormatStatusFile, str(e)))
            sys.stderr.write("%s\n" % str(e))
            sys.exit(-2)

    if os.path.exists(deviceFormatLockFile):
        Utils.log("lock file %s exists" % deviceFormatLockFile)
        sys.stderr.write("Device format already running\n")
        sys.exit(2)

    if options.fstype:
        process = Utils.runCommandBG("gluster_provision_block_wrapper.py -t %s %s" % (options.fstype, device), root=True)
    else:
        process = Utils.runCommandBG("gluster_provision_block_wrapper.py %s" % device, root=True)
    if process:
        sys.exit(0)

    sys.stderr.write("Device format failed\n")
    sys.exit(3)


if __name__ == "__main__":
    main()
