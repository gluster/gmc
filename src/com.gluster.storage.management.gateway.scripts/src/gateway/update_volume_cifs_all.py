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


def updateVolumeCifsConf(volumeName, userList):
    lines = Utils.readFile(Globals.CIFS_VOLUME_FILE, lines=True)
    try:
        fp = open(Globals.CIFS_VOLUME_FILE, "w")
        for line in lines:
            if not line.strip():
                continue
            if line.strip().split(":")[0] == volumeName:
                fp.write("%s:%s\n" % (volumeName, ":".join(userList)))
            else:
                fp.write("%s\n" % line.strip())
        fp.close()
    except IOError, e:
        Utils.log("failed to write file %s: %s" % (Globals.CIFS_VOLUME_FILE, str(e)))
        return False
    return True


def main():
    if len(sys.argv) < 4:
        sys.stderr.write("usage: %s SERVER_FILE VOLUME_NAME USER1 USER2 ...\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    serverFile = sys.argv[1]
    volumeName = sys.argv[2]
    userList = sys.argv[3:]

    missingUserList = []
    for userName in userList:
        if not Utils.getCifsUserUid(userName):
            missingUserList.append(userName)

    if missingUserList:
        sys.stderr.write("User %s does not exists\n" % missingUserList)
        sys.exit(1)


    rv = Utils.runCommand(["grun.py", serverFile, "update_volume_cifs.py", volumeName] + userList)
    if rv == 0:
        if not updateVolumeCifsConf(volumeName, userList):
            sys.stderr.write("Failed to update volume %s and user-list %s in cifs volume configuration\n" % (volumeName, userList))
            sys.exit(11)
    sys.exit(rv)


if __name__ == "__main__":
    main()
