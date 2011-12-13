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
import re
import syslog
import subprocess
import time
import tempfile
import glob

import Globals

RUN_COMMAND_ERROR = -1024
LOG_SYSLOG = 1
SYSLOG_REQUIRED = False
LOG_FILE_NAME = None
LOG_FILE_OBJ = None
logOpened = False
sshCommandPrefix = "ssh -l root -q -i /opt/glustermg/keys/gluster.pem -o BatchMode=yes -o GSSAPIAuthentication=no -o PasswordAuthentication=no -o StrictHostKeyChecking=no".split()
try:
    commandPath = "/opt/glustermg/%s/backend" % os.environ['GMG_VERSION']
except KeyError, e:
    commandPath = "/opt/glustermg/@VERSION@/backend"

def log(priority, message=None):
    global logOpened
    if not logOpened:
        syslog.openlog(os.path.basename(sys.argv[0]))
        logOpened = True

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


def isString(value):
    return (type(value) == type("") or type(value) == type(u""))


def getTempFileName():
    filedesc, filename = tempfile.mkstemp(prefix="GSP_")
    os.close(filedesc)
    return filename


def readFile(fileName, lines=False):
    content = None
    try:
        fp = open(fileName)
        if lines:
            content = fp.readlines()
        else:
            content = fp.read()
        fp.close()
        return content
    except IOError, e:
        log("failed to read file %s: %s" % (fileName, str(e)))
    if lines:
        return []
    else:
        return ""


def writeFile(fileName, content):
    try:
        fp = open(fileName, "w")
        if isString(content):
            fp.write(content)
        elif type(content) == type([]):
            fp.writelines(content)
        fp.close()
        return True
    except IOError, e:
        log("failed to write file %s: %s" % (fileName, str(e)))
    return False


def removeFile(fileName, root=False):
    if root:
        if runCommand("rm %s" % fileName, root=True) == 0:
            return True
        return False
    try:
        os.remove(fileName)
        return True
    except OSError, e:
        log("Failed to remove file %s: %s" % (fileName, str(e)))
    return False


def runCommandBG(command, stdinFileObj=None, stdoutFileObj=None, stderrFileObj=None,
                 shell=False, root=None):
    if shell:
        if not isString(command):
            return None
    else:
        if isString(command):
            command = command.split()

    if root == True:
        if shell:
            command = "sudo " + command
        else:
            command = ['sudo'] + command
    elif isString(root):
        if shell:
            command = "sudo -u " + root + " " + command
        else:
            command = ['sudo', '-u', root] + command

    if not stdinFileObj:
        stdinFileObj=subprocess.PIPE
    if not stdoutFileObj:
        stdoutFileObj=subprocess.PIPE
    if not stderrFileObj:
        stderrFileObj=subprocess.PIPE

    try:
        process = subprocess.Popen(command,
                                   bufsize=-1,
                                   stdin=stdinFileObj,
                                   stdout=stdoutFileObj,
                                   stderr=stderrFileObj,
                                   shell=shell)
        return process
    except OSError, e:
        log("runCommandBG(): Failed to run command [%s]: %s" % (command, e))
    return None


def runCommand(command,
               input='', output=False,
               shell=False, root=None):
    rv = {}
    rv["Status"] = RUN_COMMAND_ERROR
    rv["Stdout"] = None
    rv["Stderr"] = None

    try:
        stdinFileName = getTempFileName()
        stdinFileObj = open(stdinFileName, "w")
        stdinFileObj.write(input)
        stdinFileObj.close()
        stdinFileObj = open(stdinFileName, "r")

        stdoutFileName = getTempFileName()
        stdoutFileObj = open(stdoutFileName, "w")

        stderrFileName = getTempFileName()
        stderrFileObj = open(stderrFileName, "w")
    except IOError, e:
        log("Failed to create temporary file for executing command [%s]: %s" % (command, e))
        if output:
            return rv
        return rv["Status"]

    stdoutContent = None
    stderrContent = None

    process = runCommandBG(command,
                           stdinFileObj=stdinFileObj,
                           stdoutFileObj=stdoutFileObj,
                           stderrFileObj=stderrFileObj,
                           shell=shell, root=root)
    if process:
        rv['Status'] = process.wait()
        rv['Stdout'] = readFile(stdoutFileName)
        rv['Stderr'] = readFile(stderrFileName)

    os.remove(stdinFileName)
    os.remove(stdoutFileName)
    os.remove(stderrFileName)

    if output:
        return rv
    return rv["Status"]


def daemonize():
    try: 
        pid = os.fork() 
        if pid > 0:
            # exit first parent
            sys.exit(0) 
    except OSError, e:
        #sys.stderr.write("fork #1 failed: %d (%s)\n" % (e.errno, e.strerror))
        return False
	
    # decouple from parent environment
    os.chdir("/") 
    os.setsid() 
    os.umask(0) 
	
    # do second fork
    try: 
        pid = os.fork() 
        if pid > 0:
            # exit from second parent
            sys.exit(0) 
    except OSError, e: 
        #sys.stderr.write("fork #2 failed: %d (%s)\n" % (e.errno, e.strerror))
        return False 
	
    # redirect standard file descriptors
    sys.stdout.flush()
    sys.stderr.flush()
    si = file("/dev/null", 'r')
    so = file("/dev/null", 'a+')
    se = file("/dev/null", 'a+', 0)
    os.dup2(si.fileno(), sys.stdin.fileno())
    os.dup2(so.fileno(), sys.stdout.fileno())
    os.dup2(se.fileno(), sys.stderr.fileno())
    return True


def getMeminfo():
    lines = readFile("/proc/meminfo", lines=True)
    re_parser = re.compile(r'^(?P<key>\S*):\s*(?P<value>\d*)\s*kB' )
    result = {}
    for line in lines:
        match = re_parser.match(line)
        if not match:
            continue # skip lines that don't parse
        key, value = match.groups(['key', 'value'])
        result[key] = int(value)
    result['MemUsed'] = (result['MemTotal'] - result['MemFree'] - result['Buffers'] - result['Cached'])
    return result


def _getCpuStatList():
    lines = readFile("/proc/stat", lines=True)
    if not lines:
        return None
    return map(float, lines[0].split()[1:5])


def getCpuUsageAvg():
    st1 = _getCpuStatList()
    #time1 = time.time()
    time.sleep(1)
    st2 = _getCpuStatList()
    #time2 = time.time()
    if not (st1 and st2):
        return None
    usageTime = (st2[0] - st1[0]) + (st2[1] - st1[1]) + (st2[2] - st1[2])
    try:
        return (100.0 * usageTime) / (usageTime + (st2[3] - st1[3]))
    except ZeroDivisionError, e:
        return 0


def convertKbToMb(kb):
    return kb / 1024.0


def getDeviceFormatStatusFile(device):
    return "/var/tmp/format_%s.status" % device.replace('/', '_')


def getDeviceFormatLockFile(device):
    return "/var/lock/format_%s.lock" % device.replace('/', '_')


def getDeviceFormatOutputFile(device):
    return "/var/tmp/format_%s.out" % device.replace('/', '_')


def getGlusterVersion():
    rv = runCommand("/usr/sbin/gluster --version", output=True)
    if rv["Stderr"]:
        return None
    if rv["Status"] != 0:
        return None
    if not rv["Stdout"]:
        return None
    return rv["Stdout"].strip().split()[1]


def getCifsUserUid(userName):
    lines = readFile(Globals.CIFS_USER_FILE, lines=True)
    for line in lines:
        if not line.strip():
            continue
        tokens = line.strip().split(":")
        if tokens[1] == userName:
            return int(tokens[0])
    return None

def grun(serverFile, command, argumentList=[]):
    commandList = ["%s/%s" % (commandPath, command)] + argumentList
    serverNameList = readFile(serverFile, lines=True)
    if not serverNameList:
        return 1
    status = True
    for serverName in serverNameList:
        rv = runCommand(sshCommandPrefix + [serverName.strip()] + commandList, output=True)
        if rv["Status"] != 0:
            sys.stderr.write("%s: %s\n" % (serverName.strip(), rv["Status"]))
            sys.stderr.write("Stdout:\n%s\n" % rv["Stdout"])
            sys.stderr.write("Stderr:\n%s\n" % rv["Stderr"])
            sys.stderr.write("---\n")
            status = False

    if status:
        return 0
    else:
        return 2

def getFileSystemType():
    return [os.path.basename(i).split('.')[1] for i in glob.glob("/sbin/mkfs.*")]
