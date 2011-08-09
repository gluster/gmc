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

# Input command: get_rrd_memory_details.py 1hour
# OUTPUT as bellow:
# <?xml version="1.0" encoding="ISO-8859-1"?>
#
# <xport>
#   <meta>
#     <start>1310455500</start>
#     <step>300</step>
#     <end>1310459100</end>
#     <rows>13</rows>
#     <columns>5</columns>
#     <legend>
#       <entry>memoryUsed</entry>
#       <entry>memoryFree</entry>
#       <entry>memoryCache</entry>
#      <entry>memoryBuffer</entry>
#       <entry>totalMemory</entry>
#     </legend>
#   </meta>
#   <data>
#     <row><t>1310455500</t><v>1.9181091707e+06</v><v>1.5819754974e+06</v><v>1.2528146351e+06</v><v>1.2528146351e+06</v><v>3.5000846681e+06</v></row>
#     ---
#     ---
#   </data>
# </xport>

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import syslog
from XmlHandler import ResponseXml
import Utils

def getMemData(period):
    memRrdFile = "/var/lib/rrd/mem.rrd"
    rs = ResponseXml()
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
                 XPORT:total:totalMemory" % (period, memRrdFile, memRrdFile, memRrdFile, memRrdFile)

    rv = Utils.runCommand(command, output=True, root=True)
    if status["Status"] != 0:
        Utils.log("failed to create RRD file for memory usages %s" % file)
        rs.appendTagRoute("status.code", rv["Status"])
        rs.appendTagRoute("status.message", "Error: [%s] %s" % (Utils.stripEmptyLines(rv["Stderr"]), Utils.stripEmptyLines(rv["Stdout"])))
        return rs.toxml()
    return rv["Stdout"]

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s <period>\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    period = sys.argv[1]
    print getMemData(period)
    sys.exit(0)

if __name__ == "__main__":
    main()
