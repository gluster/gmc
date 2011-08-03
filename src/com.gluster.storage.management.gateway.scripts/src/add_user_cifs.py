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
    if len(sys.argv) < 4:
        sys.stderr.write("usage: %s UID USERNAME PASSWORD\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    uid = sys.argv[1]
    userName = sys.argv[2]
    password = sys.argv[3]

    if Utils.runCommand("groupadd -g %s %s" % (uid, userName)) != 0:
        Utils.log("failed to add group gid:%s, name:%s\n" % (uid, userName))
        sys.exit(1)

    command = ["useradd", "-c", Globals.VOLUME_USER_DESCRIPTION, "-M", "-d", "/", "-s", "/sbin/nologin", "-u", uid, "-g", uid, userName]
    if Utils.runCommand(command) != 0:
        Utils.log("failed to add user uid:%s, name:%s\n" % (uid, userName))
        sys.exit(2)

    if Utils.runCommand("smbpasswd -s -a %s" % userName,
                  input="%s\n%s\n" % (password, password)) != 0:
        Utils.log("failed to set smbpassword of user uid:%s, name:%s\n" % (uid, userName))
        sys.exit(3)
    sys.exit(0)


if __name__ == "__main__":
    main()
