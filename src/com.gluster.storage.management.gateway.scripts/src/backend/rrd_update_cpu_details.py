#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
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

    rv = Utils.runCommand(command, output=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Utils.log("failed to create RRD file for cpu usages %s" % file)
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
        Utils.log("failed to fetch cpu details from /proc/stat")
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "failed to fetch cpu details")
        return rs.toxml()

    command = ["rrdtool", "update", file, "-t", "user:system:idle",
               "N:%s:%s:%s" % (user, system, idle)]
    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message = "Error: [%s]" % (error)
        Utils.log("failed to update cpu usage into rrd file %s" % file)
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
