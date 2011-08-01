#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

import os
import sys
import Utils

def main():
    if len(sys.argv) < 2:
        sys.stderr.write("usage: %s USERNAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    userName = sys.argv[1]

    if Utils.runCommand("userdel %s" % userName) != 0:
        Utils.log("failed to remove user name:%s\n" % userName)
        sys.exit(1)
    sys.exit(0)


if __name__ == "__main__":
    main()
