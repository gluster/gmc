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


def removeUser(userName):
    lines = Utils.readFile(Globals.CIFS_USER_FILE, lines=True)
    try:
        fp = open(Globals.CIFS_USER_FILE, "w")
        for line in lines:
            if not line.strip():
                continue
            if line.split(":")[1] == userName:
                continue
            fp.write("%s\n" % line)
        fp.close()
    except IOError, e:
        Utils.log("failed to write file %s: %s" % (Globals.CIFS_USER_FILE, str(e)))
        return False
    return True


def main():
    if len(sys.argv) < 3:
        sys.stderr.write("usage: %s SERVER_LIST USERNAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    serverList = sys.argv[1]
    userName = sys.argv[2]

    rv = Utils.runCommand("grun.py %s delete_user_cifs.py %s" % (serverList, userName))
    if rv == 0:
        if not removeUser(userName):
            Utils.log("Failed to remove the user:%s on gateway server\n" % userName)
            sys.exit(0)
    sys.exit(rv)


if __name__ == "__main__":
    main()
