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
    if len(sys.argv) < 2:
        sys.stderr.write("usage: %s SERVER_FILE\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    serverFile = sys.argv[1]

    rv = Utils.grun(serverFile, "setup_cifs_config.py")
    sys.exit(rv)


if __name__ == "__main__":
    main()
