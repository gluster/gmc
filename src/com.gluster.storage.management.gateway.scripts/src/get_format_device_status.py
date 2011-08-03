#!/usr/bin/python
#  Copyright (C) 2009,2010 Gluster, Inc. <http://www.gluster.com>
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
import time
import Utils
import DiskUtils
from XmlHandler import ResponseXml

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s DEVICE_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    device = DiskUtils.getDevice(sys.argv[1])

    deviceFormatLockFile = Utils.getDeviceFormatLockFile(device)
    deviceFormatStatusFile = Utils.getDeviceFormatStatusFile(device)
    deviceFormatOutputFile = Utils.getDeviceFormatOutputFile(device)

    time.sleep(1)
    if not os.path.exists(deviceFormatLockFile):
        if not os.path.exists(deviceFormatStatusFile):
            sys.stderr.write("Device format not initiated\n")
            sys.exit(1)

    if os.path.exists(deviceFormatStatusFile):
        try:
            fp = open(deviceFormatStatusFile)
            line = fp.read()
            fp.close()
            line = line.strip()

            Utils.removeFile(deviceFormatOutputFile)
            Utils.removeFile(deviceFormatStatusFile)

            responseDom = ResponseXml()
            responseDom.appendTagRoute("device", sys.argv[1])
            responseDom.appendTagRoute("completedBlocks", "0")
            responseDom.appendTagRoute("totalBlocks", "0")
            responseDom.appendTagRoute("message", line)
            if line.upper() == "COMPLETED":
                responseDom.appendTagRoute("formatStatus", "COMPLETED")
            else:
                responseDom.appendTagRoute("formatStatus", "NOT_RUNNING")
            print responseDom.toxml()
            sys.exit(0)
        except IOError, e:
            Utils.log("failed to read format status file %s: %s" % (deviceFormatStatusFile, str(e)))
            sys.stderr.write("%s\n" % str(e))
            sys.exit(-2)

    if not os.path.exists(deviceFormatOutputFile):
        responseDom = ResponseXml()
        responseDom.appendTagRoute("device", sys.argv[1])
        responseDom.appendTagRoute("completedBlocks", "0")
        responseDom.appendTagRoute("totalBlocks", "0")
        responseDom.appendTagRoute("message", None)
        responseDom.appendTagRoute("formatStatus", "IN_PROGRESS")
        print responseDom.toxml()
        sys.exit(0)

    try:
        fp = open(deviceFormatOutputFile)
        content = fp.read()
        fp.close()
    except IOError, e:
        Utils.log("failed to read format output file %s: %s" % (deviceFormatOutputFile, str(e)))
        responseDom = ResponseXml()
        responseDom.appendTagRoute("device", sys.argv[1])
        responseDom.appendTagRoute("completedBlocks", "0")
        responseDom.appendTagRoute("totalBlocks", "0")
        responseDom.appendTagRoute("message", None)
        responseDom.appendTagRoute("formatStatus", "IN_PROGRESS")
        print responseDom.toxml()
        sys.exit(0)

    lines = [line for line in content
             if "Writing inode tables" in line]
    if not lines:
        responseDom = ResponseXml()
        responseDom.appendTagRoute("device", sys.argv[1])
        responseDom.appendTagRoute("completedBlocks", "0")
        responseDom.appendTagRoute("totalBlocks", "0")
        if content:
            responseDom.appendTagRoute("message", content[-1])
        else:
            responseDom.appendTagRoute("message")
        responseDom.appendTagRoute("formatStatus", "IN_PROGRESS")
        print responseDom.toxml()
        sys.exit(0)

    tokens = [token for token in lines[-1].split("\x08") if token]
    if "done" in tokens[-1]:
        values = tokens[-2].split(':')[-1].strip().split('/')
    else:
        values = tokens[-1].split(':')[-1].strip().split('/')

    responseDom.appendTagRoute("device", sys.argv[1])
    responseDom.appendTagRoute("completedBlocks", values[0])
    responseDom.appendTagRoute("totalBlocks", values[1])
    responseDom.appendTagRoute("message", lines[-1])
    responseDom.appendTagRoute("formatStatus", "IN_PROGRESS")
    print responseDom.toxml()
    sys.exit(0)

if __name__ == "__main__":
    main()
