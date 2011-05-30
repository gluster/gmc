#  Copyright (c) 2009 Gluster, Inc. <http://www.gluster.com>
#  This file is part of GlusterSP.
#
#  GlusterSP is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published
#  by the Free Software Foundation; either version 3 of the License,
#  or (at your option) any later version.
#
#  GlusterSP is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#  General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see
#  <http://www.gnu.org/licenses/>.

import sys
import syslog

def log(priority, message=None):
    if type(priority) == type(""):
        logPriority = syslog.LOG_INFO
        logMessage = priority
    else:
        logPriority = priority
        logMessage = message
    if not logMessage:
        return
    #if Globals.DEBUG:
    #    sys.stderr.write(logMessage)
    else:
        syslog.syslog(logPriority, logMessage)
    return


def stripEmptyLines(content):
    ret = ""
    for line in content.split("\n"):
        if line.strip() != "":
            ret += line
    return ret

