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

MEMORY_RRD_FILE = "/var/lib/rrd/mem.rrd"

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s DURATION\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    period = sys.argv[1]

    command = "rrdtool xport --start -%s \
                 DEF:free=%s:memfree:AVERAGE \
                 DEF:used=%s:memused:AVERAGE \
                 DEF:cache=%s:memcache:AVERAGE \
                 DEF:buffer=%s:membuffer:AVERAGE \
                 CDEF:total1=used,free,+ \
                 CDEF:used1=used,buffer,cache,-,- \
                 CDEF:total=total1,used1,+ \
                 XPORT:used:memoryUsed \
                 XPORT:free:memoryFree \
                 XPORT:cache:memoryCache \
                 XPORT:buffer:memoryBuffer \
                 XPORT:total:totalMemory" % (period, MEMORY_RRD_FILE, MEMORY_RRD_FILE, MEMORY_RRD_FILE, MEMORY_RRD_FILE)

    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Status"] != 0:
        sys.stderr.write("Failed to get RRD data of memory usage\n")
        sys.exit(rv["Status"])

    print rv["Stdout"]
    sys.exit(0)

if __name__ == "__main__":
    main()
