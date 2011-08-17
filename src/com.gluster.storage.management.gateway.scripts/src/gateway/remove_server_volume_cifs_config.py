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
import Globals
import Utils


def main():
    if len(sys.argv) < 3:
        sys.stderr.write("usage: %s SERVER_NAME VOLUME_FILE\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    serverName = sys.argv[1]
    volumeFile = sys.argv[2]

    try:
        fp = open(volumeFile)
        lines = fp.readlines()
        fp.close()
    except IOError, e:
        Utils.log("Failed to read volume file %s: %s" % (volumeFile, str(e)))
        sys.stderr.write("Failed to read volume file %s: %s\n" % (volumeFile, str(e)))
        sys.exit(1)

    volumeNameList = [line.strip() for line in lines]
    if not volumeNameList:
        sys.exit(0)

    try:
        fp = open(Globals.CIFS_VOLUME_FILE)
        content = fp.read()
        fp.close()
    except IOError, e:
        Utils.log("failed to read file %s: %s" % (Globals.CIFS_VOLUME_FILE, str(e)))
        sys.stderr.write("failed to read file %s: %s\n" % (Globals.CIFS_VOLUME_FILE, str(e)))
        sys.exit(2)

    cifsVolumeList = [line.split(":")[0] for line in content.split()]
    runningCifsVolumeList = set(cifsVolumeList).intersection(set(volumeNameList))

    if not runningCifsVolumeList:
        sys.exit(0)

    tempFileName = Utils.getTempFileName()
    try:
        fp = open(tempFileName, "w")
        fp.write("%s\n" % serverName)
    except IOError:
        Utils.log("Failed to write server name to file %s" % tempFileName)
        sys.stderr.write("Failed to write server name to file\n")
        sys.exit(3)

    status = True
    for volumeName in runningCifsVolumeList:
        if Utils.runCommand(["grun.py", tempFileName, "stop_volume_cifs.py", volumeName.strip()]) != 0:
            status = False
        if Utils.runCommand(["grun.py", tempFileName, "delete_volume_cifs.py", volumeName.strip()]) ! 0:
            status = False

    try:
        os.remove(tempFileName)
    except OSError, e:
        Utils.log("Failed to remove temporary file %s" % tempFileName)
        sys.stderr.write("Failed to remove temporary file %s\n" % tempFileName)
        pass

    if status:
        sys.exit(0)
    else:
        sys.exit(2)

if __name__ == "__main__":
    main()
