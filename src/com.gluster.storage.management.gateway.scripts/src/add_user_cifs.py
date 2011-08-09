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
import grp
import pwd
import Globals
import Utils

def main():
    if len(sys.argv) < 4:
        sys.stderr.write("usage: %s UID USERNAME PASSWORD\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    try:
        uid = int(sys.argv[1])
    except ValueError, e:
        sys.stderr.write("invalid uid %s\n" % sys.argv[1])
        sys.exit(-2)
    userName = sys.argv[2]
    password = sys.argv[3]

    try:
        groupInfo = grp.getgrnam(userName)
        if uid != groupInfo.gr_gid:
            Utils.log("group %s exists with different gid %s\n" % (userName, groupInfo.gr_gid))
            sys.exit(1)
    except KeyError, e:
        if Utils.runCommand("groupadd -g %s %s" % (uid, userName)) != 0:
            Utils.log("failed to add group %s gid %s\n" % (username, uid))
            sys.exit(2)
    try:
        userInfo = pwd.getpwnam(userName)
        if uid != userInfo.pw_uid:
            Utils.log("user %s exists with different uid %s\n" % (userName, userInfo.pw_uid))
            sys.exit(3)
    except KeyError, e:
        command = ["useradd", "-c", Globals.VOLUME_USER_DESCRIPTION, "-M", "-d", "/", "-s", "/sbin/nologin", "-u", str(uid), "-g", str(uid), userName]
        if Utils.runCommand(command) != 0:
            Utils.log("failed to add user %s uid %s\n" % (userName, uid))
            sys.exit(4)

    if Utils.runCommand("smbpasswd -s -a %s" % userName,
                  input="%s\n%s\n" % (password, password)) != 0:
        Utils.log("failed to set smbpassword of user %s\n" %  userName)
        sys.exit(5)
    sys.exit(0)


if __name__ == "__main__":
    main()
