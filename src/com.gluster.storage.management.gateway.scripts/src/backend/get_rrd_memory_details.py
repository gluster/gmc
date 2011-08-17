#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
#

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
import Utils

MEMORY_RRD_FILE = "/var/lib/rrd/mem.rrd"

def main():
    if len(sys.argv) != 2:
        sys.stderr.write("usage: %s <PERIOD>\n" % os.path.basename(sys.argv[0]))
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
