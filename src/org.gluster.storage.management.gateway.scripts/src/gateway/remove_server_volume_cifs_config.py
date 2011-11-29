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
import Globals
import Utils


def main():
    if len(sys.argv) < 3:
        sys.stderr.write("usage: %s SERVER_NAME VOLUME_FILE\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    serverName = sys.argv[1]
    volumeFile = sys.argv[2]

    lines = Utils.readFile(volumeFile, lines=True)
    volumeNameList = [line.strip() for line in lines]
    if not volumeNameList:
        sys.exit(0)

    lines = Utils.readFile(Globals.CIFS_VOLUME_FILE, lines=True)
    cifsVolumeList = [line.strip().split(":")[0] for line in lines if line.strip()]
    runningCifsVolumeList = set(cifsVolumeList).intersection(set(volumeNameList))

    if not runningCifsVolumeList:
        sys.exit(0)

    tempFileName = Utils.getTempFileName()
    try:
        fp = open(tempFileName, "w")
        fp.write("%s\n" % serverName)
        fp.close()
    except IOError, e:
        Utils.log("Failed to write server name to file %s: %s" % (tempFileName, str(e)))
        sys.stderr.write("Failed to write server name to file %s: %s\n" % (tempFileName, str(e)))
        sys.exit(3)

    status = True
    for volumeName in runningCifsVolumeList:
        if Utils.grun(tempFileName, "stop_volume_cifs.py", [volumeName.strip()]) != 0:
            status = False
        if Utils.grun(tempFileName, "delete_volume_cifs.py", [volumeName.strip()]) != 0:
            status = False

    try:
        os.remove(tempFileName)
    except OSError, e:
        Utils.log("Failed to remove temporary file %s: %s" % (tempFileName, str(e)))
        sys.stderr.write("Failed to remove temporary file %s: %s\n" % (tempFileName, str(e)))
        pass

    if status:
        sys.exit(0)
    else:
        sys.exit(2)

if __name__ == "__main__":
    main()
