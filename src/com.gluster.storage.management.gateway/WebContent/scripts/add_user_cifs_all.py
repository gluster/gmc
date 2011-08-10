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


def getUid(userName):
    try:
        fp = open(Globals.CIFS_USER_FILE)
        content = fp.read()
        fp.close()
    except IOError, e:
        Utils.log("failed to read file %s: %s" % (Globals.CIFS_USER_FILE, str(e)))
        return False

    for line in content.strip().split():
        tokens = line.split(":")
        if tokens[1] == userName:
            return int(tokens[0])
    return None


def getLastUid():
    if not os.path.exists(Globals.CIFS_USER_FILE):
        return Globals.DEFAULTUID
    try:
        fp = open(Globals.CIFS_USER_FILE)
        content = fp.read()
        fp.close()
    except IOError, e:
        Utils.log("failed to read file %s: %s" % (Globals.CIFS_USER_FILE, str(e)))
        return False

    lines = content.strip().split()
    if not lines:
        return Globals.DEFAULTUID
    return int(lines[-1].split(":")[0])


def setUid(uid, userName):
    try:
        fp = open(Globals.CIFS_USER_FILE, "a")
        fp.write("%s:%s\n" % (uid, userName))
        fp.close()
        return True
    except IOError, e:
        Utils.log("failed to write file %s: %s" % (Globals.CIFS_USER_FILE, str(e)))
        return False


def main():
    if len(sys.argv) < 4:
        sys.stderr.write("usage: %s SERVER_FILE USERNAME PASSWORD\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    serverFile = sys.argv[1]
    userName = sys.argv[2]
    password = sys.argv[3]

    existingUser = False
    uid = getUid(userName)
    if not uid:
        uid = getLastUid()
        if not uid:
            sys.exit(10)
        uid += 1
    else:
        existingUser = True

    rv = Utils.runCommand("grun.py %s add_user_cifs.py %s %s %s" % (serverFile, uid, userName, password))
    if existingUser:
        sys.exit(rv)

    if rv == 0:
        if not setUid(uid, userName):
            sys.stderr.write("Failed to add the user\n")
            sys.exit(11)
    sys.exit(rv)


if __name__ == "__main__":
    main()
