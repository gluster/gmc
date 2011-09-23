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
    if len(sys.argv) < 2:
        sys.stderr.write("usage: %s VOLUME_NAME\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    volumeName = sys.argv[1]

    lines = Utils.readFile(Globals.CIFS_VOLUME_FILE, lines=True)
    for line in lines:
        if not line.strip():
            continue
        tokens = line.strip().split(":")
        if tokens[0] == volumeName:
            print "\n".join(tokens[1:])
            sys.exit(0)
    # given volume is not configured for cifs export
    sys.exit(0)


if __name__ == "__main__":
    main()
