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

def getCpuData(period):
    cpuRrdFile = "/var/lib/rrd/cpu.rrd"
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
               XPORT:totalpct:totalpct" % (period, cpuRrdFile, cpuRrdFile, cpuRrdFile)

    rv = Utils.runCommand(command, output=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Utils.log("failed to create RRD file for cpu usages %s" % file)
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toxml()
    return rv["Stdout"]

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s <period>\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    period = sys.argv[1]
    print getCpuData(period)
    sys.exit(0)

if __name__ == "__main__":
    main()
