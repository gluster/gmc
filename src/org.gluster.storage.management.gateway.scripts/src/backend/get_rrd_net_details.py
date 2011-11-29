#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway (GlusterMG).
#
#  GlusterMG is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 3 of the License,
#  or (at your option) any later version.
#
#  GlusterMG is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.
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

def main():
    if len(sys.argv) != 3:
        sys.stderr.write("usage: %s DEVICE DURATION\n" % os.path.basename(sys.argv[0]))
        sys.exit(-1)

    device = sys.argv[1]
    period = sys.argv[2]

    command = "rrdtool xport --start -%s \
               DEF:received=/var/lib/rrd/network-%s.rrd:received:AVERAGE \
               DEF:transmitted=/var/lib/rrd/network-%s.rrd:transmitted:AVERAGE \
               CDEF:total=received,transmitted,+ \
               XPORT:received:received \
               XPORT:transmitted:transmitted \
               XPORT:total:total" % (period, device, device)

    rv = Utils.runCommand(command, output=True, root=True)
    if rv["Status"] != 0:
        sys.stderr.write("Failed to get RRD information of device %s\n" % device)
        sys.exit(rv["Status"])

    print rv["Stdout"]
    sys.exit(0)

if __name__ == "__main__":
    main()
