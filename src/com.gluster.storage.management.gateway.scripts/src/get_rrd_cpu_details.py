#!/usr/bin/python
#  Copyright (C) 2010 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#
#  Gluster Storage Platform is free software; you can redistribute it
#  and/or modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3 of
#  the License, or (at your option) any later version.
#
#  Gluster Storage Platform is distributed in the hope that it will be
#  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
#  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.
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
               CDEF:total=cpuuser,cpusystem,+ \
               XPORT:cpuuser:user \
               XPORT:cpusystem:system \
               XPORT:total:total" % (period, cpuRrdFile, cpuRrdFile)

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
