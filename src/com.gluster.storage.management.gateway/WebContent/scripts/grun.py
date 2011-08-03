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


def main():
    sshCommandPrefix = "ssh -q -o BatchMode=yes -o GSSAPIAuthentication=no -o PasswordAuthentication=no -o StrictHostKeyChecking=no".split()

    if len(sys.argv) < 3:
        sys.stderr.write("usage: %s SERVER_FILE COMMAND [ARGUMENTS]\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)
    serverFile = sys.argv[1]
    try:
        command = ["/opt/glustermg/%s/backend/%s" % (os.environ['GMG_VERSION'], sys.argv[2])]
    except KeyError, e:
        command = ["/opt/glustermg/1.0.0/backend/%s" % sys.argv[2]]
    command += sys.argv[3:]

    try:
        fp = open(serverFile)
        serverNameList = fp.readlines()
        fp.close()
    except IOError, e:
        Utils.log("Failed to read server file %s: %s\n" % (serverFile, str(e)))
        sys.exit(1)

    for serverName in serverNameList:
        rv = Utils.runCommand(sshCommandPrefix + [serverName.strip()] + command)
        print rv

    sys.exit(0)


if __name__ == "__main__":
    main()
