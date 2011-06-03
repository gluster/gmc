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

def createMemData(file, step):
    rs = ResponseXml()
    command = ["rrdtool", "create", file, "--step=%s" % step,
               "DS:user:COUNTER:600:0:U",
               "DS:system:COUNTER:600:0:U",
               "DS:idle:COUNTER:600:0:U",
               "RRA:AVERAGE:0.5:1:576",
               "RRA:AVERAGE:0.5:6:672",
               "RRA:AVERAGE:0.5:24:732",
               "RRA:AVERAGE:0.5:144:1460"]

    rv = Utils.runCommandFG(command, stdout=True, root=True)
    message = Common.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        error = Common.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Common.log(syslog.LOG_ERR, "failed to create RRD file for cpu usages %s" % file)
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toxml()
    return None

def updateMemData(file):
    rs = ResponseXml()
    user = None
    system = None
    idle = None
    for line in open("/proc/stat").readlines():
        if line.startswith("cpu"):
            cpudetails = line.split()
            if "cpu" == cpudetails[0]:
               user = cpudetails[1]
               system = cpudetails[3]
               idle = cpudetails[4]
               break
    
    if None == user:
        Common.log(syslog.LOG_ERR, "failed to fetch cpu details from /proc/stat")
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "failed to fetch cpu details")
        return rs.toxml()

    command = ["rrdtool", "update", file, "-t", "user:system:idle",
               "N:%s:%s:%s" % (user, system, idle)]
    rv = Utils.runCommandFG(command, stdout=True, root=True)
    if rv["Stderr"]:
        error = Common.stripEmptyLines(rv["Stderr"])
        message = "Error: [%s]" % (error)
        Common.log(syslog.LOG_ERR, "failed to update cpu usage into rrd file %s" % file)
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toxml()
    return None


def main():
    cpuRrdFile = "/var/lib/rrd/cpu.rrd"
    if not os.path.exists(cpuRrdFile):
        status = createMemData(cpuRrdFile, 100)
        if status:
            print status
    status = updateMemData(cpuRrdFile)
    if status:
        print status
    sys.exit(0)

if __name__ == "__main__":
    main()
