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
        line = Utils.readFile(deviceFormatStatusFile)
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

    content = Utils.readFile(deviceFormatOutputFile, lines=True)
    if not content:
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
        responseDom.appendTagRoute("message", content[-1])
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
