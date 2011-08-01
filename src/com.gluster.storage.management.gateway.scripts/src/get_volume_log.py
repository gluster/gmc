#  Copyright (C) 2009,2010 Gluster, Inc. <http://www.gluster.com>
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

import Globals
import syslog
import Utils
from VolumeUtils import *
from XmlHandler import ResponseXml


def enumLogType(logCode):
    if "M" == logCode.upper():
        return "EMERGENCY"
    elif "A" == logCode.upper():
        return "ALERT"
    elif "C" == logCode.upper():
        return "CRITICAL"
    elif "E" == logCode.upper():
        return "ERROR"
    elif "W" == logCode.upper():
        return "WARNING"
    elif "N" == logCode.upper():
        return "NOTICE"
    elif "I" == logCode.upper():
        return "INFO"
    elif "D" == logCode.upper():
        return "DEBUG"
    elif "T" == logCode.upper():
        return "TRACE"
    else:
        return "UNKNOWN"
##--end of enumLogType()


def addLog(responseDom, logMessageTag, loginfo):
    logTag = responseDom.createTag("log", None)
    logTag.appendChild(responseDom.createTag("date", loginfo[0]))
    logTag.appendChild(responseDom.createTag("time", loginfo[1]))
    logTag.appendChild(responseDom.createTag("type", enumLogType(loginfo[2])))
    logTag.appendChild(responseDom.createTag("message", loginfo[3]))
    logMessageTag.appendChild(logTag)
    return True
##--end of addLog()


def logSplit(log):
    loginfo = log.strip().split(None, 3)
    loginfo[0] = loginfo[0][1:]   #-- Remove '['
    loginfo[1] = loginfo[1][0:-1] #-- Remove ']'
    return loginfo
##--end of logSplit()


def getVolumeLog(volumeName, tailCount):
    rs = ResponseXml()
    if not volumeName:
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "No volume name given")
        return rs.toprettyxml()

    if not tailCount:
        rs.appendTagRoute("status.code", "-1")
        rs.appendTagRoute("status.message", "No tail count given")
        return rs.toprettyxml()

    thisServerName = getCurrentServerName()
    if not thisServerName:
        rs.appendTagRoute("status.code", "-2")
        rs.appendTagRoute("status.message", "Failed to get current server name")
        return rs.toprettyxml()

    volumeDom = XDOM()
    partitionList = getPartitionListByServerName(volumeDom, thisServerName)
    if not partitionList:
        rs.appendTagRoute("status.code", "-3")
        rs.appendTagRoute("status.message", "Failed to get server partition details")
        return rs.toprettyxml()

    pattern = '\[\d{4}-\d{2}-\d{2}\s{1}\d{2}:\d{2}:\d{2}.\d+\]\s{1}([MACEWNIDT]){1}\s+'
    logMessagesTag = rs.createTag("response.logMessages")
    for partitionName in partitionList:
        logMessageTag = rs.createTag("logMessage")
        logMessageTag.appendChild("disk", "%s:%s" % (thisServerName, partitionName))

        logDirectory = "%s/%s/%s/log" % (Globals.GLUSTER_LUN_DIR, partitionList[partitionName], volumeUuid)
        logFileName = "%s/%s-%s-%s-exports-brick1.log" % (logDirectory,
                                                          Globals.GLUSTER_LUN_DIR[1:],
                                                          partitionList[partitionName],
                                                          volumeUuid)
        if not os.path.exists(logFileName):
            Utils.log("volume log file not found %s" % logFileName)
            continue
        fp = open(logFileName)
        lines = [line for line in fp if re.match(pattern, line)]
        fp.close()
        i = len(lines) - int(tailCount)
        if i < 0:
            i = 0
        for log in lines[i:]:
            loginfo = logSplit(log)
            addLog(rs, logMessageTag, loginfo)
        logMessagesTag.appendChild(logMessageTag)
    return rs.toprettyxml()
##--end of getVolumeLog()

def main():
    if len(sys.argv) != 3:
        print >> sys.stderr, "usage: %s <disk name> <volume name>" % sys.argv[0]
        sys.exit(-1)

    volumeName = sys.argv[1]
    tailCount = sys.argv[2]
    print getVolumeLog(volumeName, tailCount)
    sys.exit(0)

if __name__ == "__main__":
    main()
