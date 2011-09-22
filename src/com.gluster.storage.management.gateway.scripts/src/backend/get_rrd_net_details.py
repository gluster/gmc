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
    if len(sys.argv) != 3:
        sys.stderr.write("usage: %s DEVICE DURATION\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    device = sys.argv[1]
    period = sys.argv[2]

    command = "rrdtool xport --start -%s \
               DEF:received=/var/lib/rrd/network-%s.rrd:received:AVERAGE \
               DEF:transmitted=/var/lib/rrd/network-%s.rrd:transmitted:AVERAGE \
               CDEF:total=received,transmitted,+ \
               XPORT:received:received \
               XPORT:transmitted:transmitted \
               XPORT:total:total" % (period, device, device)

    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Status"] != 0:
        sys.stderr.write("Failed to get RRD information of device %s\n" % device)
        sys.exit(rv["Status"])

    print rv["Stdout"]
    sys.exit(0)

if __name__ == "__main__":
    main()
