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
import VolumeUtils

def main():
    if len(sys.argv) <= 2:
        sys.stderr.write("usage: %s VOLUME_NAME USER1 USER2 ...\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeName = sys.argv[1]
    userList = sys.argv[2:]

    if not VolumeUtils.writeVolumeCifsConfiguration(volumeName, userList):
        sys.stderr.write("Unable to write cifs configuration\n")
        sys.exit(1)
    if Utils.runCommand("service smb reload") != 0:
        Utils.log("Failed to reload smb service")
        sys.stderr.write("Failed to reload smb service\n")
        sys.exit(2)
    sys.exit(0)


if __name__ == "__main__":
    main()
