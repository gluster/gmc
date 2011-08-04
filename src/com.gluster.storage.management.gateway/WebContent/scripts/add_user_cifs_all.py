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


defaultUid = 1024000
cifsUserFile = "/opt/glustermg/etc/users.cifs"


def getLastUid():
    if not os.path.exists(cifsUserFile):
        return defaultUid
    try:
        fp = open(cifsUserFile)
        content = fp.read()
        fp.close()
    except IOError, e:
        Utils.log("failed to read file %s: %s" % (cifsUserFile, str(e)))
        return False

    lines = content.strip().split()
    if not lines:
        return defaultUid
    return int(lines[-1].split(":")[0])


def setUid(uid, userName):
    try:
        fp = open(cifsUserFile, "a")
        fp.write("%s:%s\n" % (uid, userName))
        fp.close()
        return True
    except IOError, e:
        Utils.log("failed to write file %s: %s" % (cifsUserFile, str(e)))
        return False


def main():
    if len(sys.argv) < 4:
        sys.stderr.write("usage: %s SERVER_FILE USERNAME PASSWORD\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    serverFile = sys.argv[1]
    userName = sys.argv[2]
    password = sys.argv[3]

    uid = getLastUid()
    if not uid:
        sys.exit(10)

    uid += 1

    rv = Utils.runCommand("grun.py %s add_user_cifs.py %s %s %s" % (serverFile, uid, userName, password))
    if rv == 0:
        if not setUid(uid, userName):
            sys.exit(11)
    sys.exit(rv)


if __name__ == "__main__":
    main()
