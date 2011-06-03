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
import syslog
from XmlHandler import ResponseXml
import Utils
import Common

def getCpuData(period):
    cpuRrdFile = "/var/lib/rrd/cpu.rrd"
    rs = ResponseXml()
    command = ["rrdtool", "xport", "--start", "-1%s" % period,
               "DEF:cpuuser=%s:user:AVERAGE" % cpuRrdFile,
               "DEF:cpusystem=%s:system:AVERAGE" % cpuRrdFile,
               "DEF:cpuidle=%s:idle:AVERAGE" % cpuRrdFile,
               "XPORT:cpuuser:'user'",
               "XPORT:cpusystem:'system'",
               "XPORT:cpuidle:'idle'"]

    rv = Utils.runCommandFG(command, stdout=True, root=True)
    message = Common.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        error = Common.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Common.log(syslog.LOG_ERR, "failed to create RRD file for cpu usages %s" % file)
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toxml()
    return rv["Stdout"]

def main():
    if len(sys.argv) != 2:
        print >> sys.stderr, "usage: %s <period>" % sys.argv[0]
        sys.exit(-1)

    period = sys.argv[1]
    print getCpuData(period)
    sys.exit(0)

if __name__ == "__main__":
    main()
