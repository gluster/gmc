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
from XmlHandler import ResponseXml
import Utils

CPU_RRD_FILE = "/var/lib/rrd/cpu.rrd"

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s <PERIOD>\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    period = sys.argv[1]

    rs = ResponseXml()
    command = "rrdtool xport --start -%s \
               DEF:cpuuser=%s:user:AVERAGE \
               DEF:cpusystem=%s:system:AVERAGE \
               DEF:cpuidle=%s:idle:AVERAGE \
               CDEF:total=cpuuser,cpusystem,cpuidle,+,+ \
               CDEF:userpct=100,cpuuser,total,/,* \
               CDEF:systempct=100,cpusystem,total,/,* \
               CDEF:idlepct=100,cpuidle,total,/,* \
               CDEF:totalpct=userpct,systempct,+ \
               XPORT:userpct:userpct \
               XPORT:systempct:systempct \
               XPORT:totalpct:totalpct" % (period, CPU_RRD_FILE, CPU_RRD_FILE, CPU_RRD_FILE)

    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Status"] != 0:
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", "Failed to get RRD data of CPU")
        print rs.toxml()
    print rv["Stdout"]
    sys.exit(0)

if __name__ == "__main__":
    main()
