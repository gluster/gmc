#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import re
import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
from XmlHandler import XDOM

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

def addLog(responseDom, logMessagesTag, loginfo):
    logMessageTag = responseDom.createTag("logMessage")
    logMessageTag.appendChild(responseDom.createTag("timestamp", loginfo[0] + " " + loginfo[1]))
    logMessageTag.appendChild(responseDom.createTag("severity", enumLogType(loginfo[2])))
    logMessageTag.appendChild(responseDom.createTag("message", loginfo[3]))
    logMessagesTag.appendChild(logMessageTag);
    return True
##--end of addLog()

def logSplit(log):
    loginfo = log.strip().split(None, 3)
    loginfo[0] = loginfo[0][1:]   #-- Remove '['
    loginfo[1] = loginfo[1][0:-1] #-- Remove ']'
    return loginfo
##--end of logSplit()

def getVolumeLog(logFilePath, tailCount):
    rs = XDOM()
    if not logFilePath:
        print >> sys.stderr, "No log file path given"
        sys.exit(-1);

    if not tailCount:
        print >> sys.stderr, "No tail count given"
        sys.exit(-1);

    pattern = '\[\d{4}-\d{2}-\d{2}\s{1}\d{2}:\d{2}:\d{2}.\d+\]\s{1}([MACEWNIDT]){1}\s+'
    if not os.path.exists(logFilePath):
        print >> sys.stderr, "volume log file [%s] not found!" % logFilePath
        sys.exit(-1);

    fp = open(logFilePath)
    lines = [line for line in fp if re.match(pattern, line)]
    fp.close()
    i = len(lines) - int(tailCount)
    if i < 0:
        i = 0
    logMessagesTag = rs.createTag("logMessages")
    rs.addTag(logMessagesTag)
    for log in lines[i:]:
        loginfo = logSplit(log)
        addLog(rs, logMessagesTag, loginfo)
    return rs.toxml()
##--end of getVolumeLog()

def main():
    if len(sys.argv) != 3:
        print >> sys.stderr, "usage: %s <Log File Path> <Line Count>" % sys.argv[0]
        sys.exit(-1)

    logFilePath = sys.argv[1]
    tailCount = sys.argv[2]
    print getVolumeLog(logFilePath, tailCount)
    sys.exit(0)

if __name__ == "__main__":
    main()
