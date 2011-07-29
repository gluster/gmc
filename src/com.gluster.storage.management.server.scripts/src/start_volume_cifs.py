#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
import Globals
import Utils
import VolumeUtils

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s VOLUME_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeName = sys.argv[1]

    volumeMountDirName = "%s/%s" % (Globals.REEXPORT_DIR, volumeName)
    cifsDirName = "%s/%s" % (Globals.CIFS_EXPORT_DIR, volumeName)

    if Utils.runCommand("mount -t glusterfs 127.0.0.1:%s %s" % (volumeName, volumeMountDirName)) != 0:
        Utils.log("Failed to mount volume %s" % (volumeName))
        sys.exit(1)
    if Utils.runCommand("ln -fTs %s %s" % (volumeMountDirName, cifsDirName)) != 0:
        Utils.log("Failed to create reexport link %s" % cifsDirName)
        sys.exit(2)
    if Utils.runCommand("chcon -t samba_share_t %s -h" % cifsDirName) != 0:
        Utils.log("Failed to change security context for the link %s" % cifsDirName)
        sys.exit(2)
    if not VolumeUtils.includeVolume(volumeName):
        Utils.log("Failed to include volume for CIFS reexport")
        sys.exit(3)
    if Utils.runCommand("service smb reload") != 0:
        Utils.log("Failed to reload smb service")
        sys.exit(4)
    sys.exit(0)


if __name__ == "__main__":
    main()
