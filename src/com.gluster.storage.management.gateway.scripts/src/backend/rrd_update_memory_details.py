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

def createMemData(file, step):
    rs = ResponseXml()
    command = ["rrdtool", "create", file, "--step=%s" % step,
               "DS:memused:ABSOLUTE:600:0:U",
               "DS:memfree:ABSOLUTE:600:0:U",
               "DS:memcache:ABSOLUTE:600:0:U",
               "DS:swapused:ABSOLUTE:600:0:U",
               "DS:swapfree:ABSOLUTE:600:0:U",
               "RRA:AVERAGE:0.5:1:576",
               "RRA:AVERAGE:0.5:6:672",
               "RRA:AVERAGE:0.5:24:732",
               "RRA:AVERAGE:0.5:144:1460"]

    rv = Utils.runCommand(command, output=True, root=True)
    message = Utils.stripEmptyLines(rv["Stdout"])
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Utils.log("failed to create RRD file for memory usages %s" % file)
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toxml()
    return None

def updateMemData(file):
    rs = ResponseXml()
    command = ["free", "-b", "-o"]
    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Utils.log("failed to retrieve memory details")
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toxml()

    message = rv["Stdout"].split()
    command = ["rrdtool", "update", file, "-t", "memused:memfree:memcache:swapused:swapfree",
               "N:%s:%s:%s:%s:%s" % (message[8], message[9], message[12], message[14], message[15])]
    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Stderr"]:
        error = Utils.stripEmptyLines(rv["Stderr"])
        message += "Error: [%s]" % (error)
        Utils.log(syslog.LOG_ERR, "failed to update memory usage into rrd file %s" % file)
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", message)
        return rs.toxml()
    return None


def main():
    #if len(sys.argv) != 2:
    #    print >> sys.stderr, "usage: %s <step>" % sys.argv[0]
    #    sys.exit(-1)
    #step = sys.argv[1]

    memRrdFile = "mem.rrd"
    if not os.path.exists(memRrdFile):
        status = createMemData(memRrdFile, 100)
        if status:
            print status
    status = updateMemData(memRrdFile)
    if status:
        print status
    sys.exit(0)

if __name__ == "__main__":
    main()
