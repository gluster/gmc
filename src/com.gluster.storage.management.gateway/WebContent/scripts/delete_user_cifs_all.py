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
import Utils


cifsUserFile = "/opt/glustermg/etc/users.cifs"


def removeUser(userName):
    try:
        fp = open(cifsUserFile)
        content = fp.read()
        fp.close()
    except IOError, e:
        Utils.log("failed to read file %s: %s" % (cifsUserFile, str(e)))
        return False

    try:
        fp = open(cifsUserFile, "w")
        lines = content.strip().split()
        for line in lines:
            if line.split(":")[1] == userName:
                continue
            fp.write("%s\n" % line)
        fp.close()
    except IOError, e:
        Utils.log("failed to write file %s: %s" % (cifsUserFile, str(e)))
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
            sys.stderr.write("Failed to remove user\n")
            sys.exit(10)
    sys.exit(rv)


if __name__ == "__main__":
    main()
