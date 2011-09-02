#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Management Gateway.
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
import socket
import struct
import syslog
import subprocess
#import spwd
import time
#import uuid
import tempfile
import grp
import pwd

import Globals

RUN_COMMAND_ERROR = -1024
LOG_SYSLOG = 1
SYSLOG_REQUIRED = False
LOG_FILE_NAME = None
LOG_FILE_OBJ = None
logOpened = False


def isString(value):
    return (type(value) == type("") or type(value) == type(u""))


def getTempFileName():
    filedesc, filename = tempfile.mkstemp(prefix="GSP_")
    os.close(filedesc)
    return filename


def runCommandBG(command, stdinFileObj=None, stdoutFileObj=None, stderrFileObj=None,
                 shell=False, root=None):
    log("runCommandBG(): Trying to execute command [%s]" % command)

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
        rv['Stdout'] = open(stdoutFileName).read()
        rv['Stderr'] = open(stderrFileName).read()

    os.remove(stdinFileName)
    os.remove(stdoutFileName)
    os.remove(stderrFileName)

    log("runCommand(): execution status of command [%s] = [%s]" % (command, rv))

    if output:
        return rv
    return rv["Status"]


def runCommandFG(command, stdout=False, stderr=False,
                 shell=False, root=None):
    if stdout or stderr:
        output = True
    else:
        output = False
    return runCommand(command, output=output, shell=shell, root=root)


def IP2Number(ipString):
    try:
        return socket.htonl(struct.unpack("I", socket.inet_aton(ipString))[0])
    except socket.error:
        return None
    except TypeError:
        return None
    except struct.error:
        return None


def Number2IP(number):
    try:
        return socket.inet_ntoa(struct.pack("I", socket.ntohl(number)))
    except socket.error:
        return None
    except AttributeError:
        return None
    except ValueError:
        return None


def computeHostName(hostName):
    if not hostName:
        return False

    hostPrefix = ""
    for i in range(len(hostName), 0, -1):
        pos = i - 1
        if hostName[pos].isdigit():
            continue
        break
    hostPrefix = hostName[:pos+1]
    try:
        hostIndex = int(hostName[pos+1:]) 
    except ValueError:
        hostIndex = 0
    # TODO: Check the availablity of the (server) name
    return "%s%s" % (hostPrefix, hostIndex + 1)


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


def getDownloadStatus(fileName):
    try:
        lines = [line for line in open(fileName)
                 if "saved" in line or "%" in line]
    except IOError:
        return 0
    if not lines:
        return 0
    if "saved" in lines[-1]:
        return 100
    return lines[-1].split("%")[0].split()[-1]


def getMeminfo():
    """-> dict of data from meminfo (str:int).
    Values are in kilobytes.
    """
    import re
    re_parser = re.compile(r'^(?P<key>\S*):\s*(?P<value>\d*)\s*kB' )
    result = {}
    for line in open('/proc/meminfo'):
        match = re_parser.match(line)
        if not match:
            continue # skip lines that don't parse
        key, value = match.groups(['key', 'value'])
        result[key] = int(value)
    result['MemUsed'] = (result['MemTotal'] - result['MemFree'] - result['Buffers'] - result['Cached'])
    return result


def getCpuUsage():
    """-> dict of cpuid : (usertime, nicetime, systemtime, idletime)
    cpuid "cpu" means the total for all CPUs.
    cpuid "cpuN" means the value for CPU N.
    """
    wanted_records = [line for line in open('/proc/stat') if
                      line.startswith('cpu')]
    result = {}
    for cpuline in wanted_records:
        fields = cpuline.split()[:5]
        data = map(int, fields[1:])
        result[fields[0]] = tuple(data)
    return result

def _getCpuStatList():
    try:
        fp = open("/proc/stat")
        line = fp.readline()
        fp.close()
        return map(float, line.split()[1:5])
    except IOError, e:
        log("Failed to open /proc/stat: %s" % str(e))
    return None

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

def getLoadavg():
    try:
        loadavgstr = open('/proc/loadavg', 'r').readline().strip()
    except IOError:
        syslog.syslog(syslog.LOG_ERR, "failed to find cpu load")
        return None

    data = map(float, loadavgstr.split()[1:])
    # returns 1 minute load average
    return data[0]


def getInfinibandPortStatus():

    """ Check for availability of infiniband port 
    and return which port is active in a key pair value
    """

    # Check for existence of infiniband ports
    value = os.popen ("ls /sys/class/infiniband").readline().strip()

    if not value:
        return None

    portlist = os.popen ("echo /sys/class/infiniband/*/ports/*").readline().split()
    
    portkeys = {}
    
    for port in portlist:
        value = os.popen ("cat %s/state" % 
                          port.strip()).readline().split(':')[1].strip()
        portkeys[port.strip()] = value
        
    return portkeys
        

def getPasswordHash(userName):
    try:
        #return spwd.getspnam(userName).sp_pwd
        return "Not implimented"
    except KeyError:
        return None


def generateSignature():
    #return str(uuid.uuid4()) + ('--%f' % time.time())
    return ('--%f' % time.time())


def isUserExist(userName):
    try:
        grp.getgrnam(userName).gr_gid
        return True
    except KeyError:
        pass
    try:
        pwd.getpwnam(userName).pw_uid
        return True
    except KeyError:
        pass
    return False


def getPlatformVersion(fileName=Globals.GLUSTER_VERSION_FILE):
    versionInfo = {}
    versionInfo["Version"] = None
    versionInfo["Update"] = None
    try:
        lines = open(Globals.GLUSTER_VERSION_FILE).readlines()
        for line in open(fileName):
            line = line.strip()
            k = line[:line.index("=")]
            v = line[line.index("=") + 1:]
            if v[0] == "'" or v[0] == '"':
                v = v[1:]
            if v[-1] == "'" or v[-1] == '"':
                v = v[:-1]
            if k.upper() == "VERSION":
                versionInfo["Version"] = v
            if k.upper() == "UPDATE":
                versionInfo["Update"] = v
    except IOError, e:
        log("Failed to read file %s: %s" % (fileName, e))
    return versionInfo


def setPlatformVersion(versionInfo, fileName=Globals.GLUSTER_VERSION_FILE):
    if isString(versionInfo):
        tokens = versionInfo.strip().split(".")
        if len(tokens) < 2:
            log("Invalid version format %s. Expecting <MAJOR>.<MINOR>.<PATCHLEVEL>" % versionInfo)
            return False
        version = ".".join(tokens[:2])
        update = ".".join(tokens[2:])
        if not update:
            update = "0"
    else:
        version = versionInfo["Version"]
        update = versionInfo["Update"]
    try:
        fp = open(fileName, "w")
        fp.write("VERSION=%s\n" % version)
        fp.write("UPDATE=%s\n" % update)
        fp.close()
        return True
    except IOError, e:
        log("Failed to write file %s: %s" % (fileName, e))
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
        log("Failed to remove file %s: %s" % (fileName, e))
    return False


def isLiveMode():
    return os.path.exists(Globals.LIVE_MODE_FILE)

def convertKbToMb(kb):
    return kb / 1024.0


def getIPIndex(indexFile):
    try:
        fp = open(indexFile)
        line = fp.readline()
        fp.close()
        index = int(line)
    except IOError:
        index = 0
    except ValueError:
        index = False
    return index

def setIPIndex(index, indexFile):
    try:
        fp = open(indexFile, "w")
        fp.write(str(index))
        fp.close()
    except IOError:
        return False
    return True

def IP2Number(ipString):
    try:
        return socket.htonl(struct.unpack("I", socket.inet_aton(ipString))[0])
    except socket.error:
        return None
    except TypeError:
        return None
    except struct.error:
        return None

def Number2IP(number):
    try:
        return socket.inet_ntoa(struct.pack("I", socket.ntohl(number)))
    except socket.error:
        return None
    except AttributeError:
        return None
    except ValueError:
        return None

def hasEntryFoundInFile(searchString, dnsEntryFileName):
    try:
        addServerEntryList = open(dnsEntryFileName).read().split()
    except IOError:
        return None
    if searchString in addServerEntryList:
        return True
    return False


def computeIpAddress(ipAddress, startIp, endIp):
    startIpNumber = IP2Number(startIp)
    endIpNumber = IP2Number(endIp)
    if not ipAddress:
        return startIp
    nextIpNumber = IP2Number(ipAddress)
    while True:  
        nextIpNumber = nextIpNumber + 1
        ipAddress = Number2IP(nextIpNumber)
        rv = runCommandFG(["ping", "-qnc", "1", ipAddress])
        if type(rv) == type(True):
            return False
        if rv != 0:
            break

    if nextIpNumber >= startIpNumber and nextIpNumber <= endIpNumber:
        return ipAddress

    nextIpNumber = IP2Number(startIp)
    while True:
        ipAddress = Number2IP(nextIpNumber)
        nextIpNumber = nextIpNumber + 1
        rv = runCommandFG(["ping", "-qnc", "1", ipAddress])
        if type(rv) == type(True):
            return False
        if rv != 0:
            break

    if IP2Number(ipAddress) >= startIpNumber and IP2Number(ipAddress) <= endIpNumber:
        return ipAddress
    return False


def setHostNameAndIp(hostName, ipAddress, lastAddServerDetailFile):
    try:
        fp = open(lastAddServerDetailFile, "w")
        fp.write("HOSTNAME=" + hostName + "\n")
        fp.write("IPADDRESS=" + ipAddress);
        fp.close()
    except IOError:
        return False
    return True

def getPort():
    try:
        fd = open(Globals.PORT_FILE, "r")
        portString = fd.readline()
        fd.close()
        port = int(portString)
    except IOError:
        port = Globals.DEFAULT_PORT - 2
    except ValueError:
        port = Globals.DEFAULT_PORT - 2
    return port

def setPort(port):
    try:
        fd = open(Globals.PORT_FILE, "w")
        fd.write(str(port))
        fd.close()
    except IOError:
        return False
    return True


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


def getDhcpServerStatus():
    status = runCommandFG(["sudo", "service", "dnsmasq", " status"])
    if type(status) == type(True) or 0 != status:
        return False
    return True

def startDhcpServer():
    status = runCommandFG(["sudo", "service", "dnsmasq", " start"])
    if type(status) == type(True) or 0 != status:
        return False
    return True

def stopDhcpServer():
    status = runCommandFG(["sudo", "service", "dnsmasq", " stop"])
    if type(status) == type(True) or 0 != status:
        return False
    return True

def getStoragePoolInfo():
    startRange = None
    endRange = None
    try:
        for line in open(Globals.GLUSTER_SERVER_POOL_FILE):
            tokens = line.split("=")
            if tokens[0] == "STARTRANGE":
                startRange = tokens[1].strip()
            if tokens[0] == "ENDRANGE":
                endRange = tokens[1].strip()
    except IOError:
        log(syslog.LOG_ERR, "unable to read %s file" % Globals.GLUSTER_SERVER_POOL_FILE)
    return startRange, endRange

def configureDnsmasq(serverIpAddress, dhcpIpAddress):
    dnsmasqConfFile = Globals.GLUSTER_CONF_CONF_DIR + "/dnsmasq.conf"
    serverPortString = "68"
    try:
        for arg in open("/proc/cmdline").read().strip().split():
            token = arg.split("=")
            if token[0] == "dhcp":
                serverPortString = token[1]
                break
    except IOError:
        log(syslog.LOG_ERR, "Failed to read /proc/cmdline.  Continuing with default port 68")
    try:
        serverPort = int(serverPortString)
    except ValueError:
        log(syslog.LOG_ERR, "Invalid dhcp port '%s' in /proc/cmdline.  Continuing with default port 68" % serverPortString)
        serverPort = 68

    try:
        fp = open(dnsmasqConfFile, "w")
        fp.write("no-hosts\n")
        #fp.write("addn-hosts=%s\n" % Globals.GLUSTER_DNS_ENTRIES)
        fp.write("bind-interfaces\n")
        fp.write("except-interface=lo\n")
        fp.write("dhcp-range=%s,%s\n" % (dhcpIpAddress, dhcpIpAddress))
        fp.write("dhcp-lease-max=1\n")
        #fp.write("dhcp-option=option:router,%s\n" % serverIp)
        #fp.write("dhcp-option=option:ntp-server,%s\n" % serverIp)
        fp.write("dhcp-alternate-port=%s\n" % serverPort)
        fp.write("server=%s\n" % serverIpAddress)
        fp.write("dhcp-script=/usr/sbin/server-info\n")
        fp.close()
    except IOError:
        log(syslog.LOG_ERR, "unable to write dnsmasq configuration %s" % dnsmasqConfFile)
        return False
    status = runCommandFG(["sudo", "cp", "-f", Globals.GLUSTER_CONF_CONF_DIR + "/dnsmasq.conf", Globals.DNSMASQ_CONF_FILE])
    if type(status) == type(True) or 0 != status:
        log(syslog.LOG_ERR, "unable to copy dnsmasq configuration to " + Globals.DNSMASQ_CONF_FILE)
        return False
    return True

def configureDhcpServer(serverIpAddress, dhcpIpAddress):
    return configureDnsmasq(serverIpAddress, dhcpIpAddress)

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


def stripEmptyLines(content):
    ret = ""
    for line in content.split("\n"):
        if line.strip() != "":
            ret += line
    return ret


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
    try:
        fp = open(Globals.CIFS_USER_FILE)
        content = fp.read()
        fp.close()
    except IOError, e:
        log("failed to read file %s: %s" % (Globals.CIFS_USER_FILE, str(e)))
        return False

    for line in content.strip().split():
        tokens = line.split(":")
        if tokens[1] == userName:
            return int(tokens[0])
    return None
