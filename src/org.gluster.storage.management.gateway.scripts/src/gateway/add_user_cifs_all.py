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


def getUid(userName):
    lines = Utils.readFile(Globals.CIFS_USER_FILE, lines=True)
    for line in lines:
        tokens = line.strip().split(":")
        if tokens[1] == userName:
            return int(tokens[0])
    return None


def getLastUid():
    lines = Utils.readFile(Globals.CIFS_USER_FILE, lines=True)
    if not lines:
        return Globals.DEFAULT_UID
    return int([line.strip().split(':')[0] for line in lines if line.strip()][-1])


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
            sys.stderr.write("Unable to read file %s\n" % Globals.CIFS_USER_FILE)
            sys.exit(10)
        uid += 1
    else:
        existingUser = True

    print (serverFile, uid, userName, password)
    rv = Utils.grun(serverFile, "add_user_cifs.py", ["%s" % uid, userName, password])
    if existingUser:
        sys.exit(rv)

    if rv == 0:
        if not setUid(uid, userName):
            sys.stderr.write("Failed to add the user\n")
            sys.exit(11)
    sys.exit(rv)


if __name__ == "__main__":
    main()
