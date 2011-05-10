#  Copyright (C) 2009 Gluster, Inc. <http://www.gluster.com>
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

import sys
import syslog
import socket
import Globals
import Commands
import re
from ServerUtils import *
from Protocol import *
from NetworkUtils import *
from Disk import *
from XmlHandler import ResponseXml

def getDiskSizeInfo(partition):
    # get values from df output
    total = None
    used = None
    free = None
    commandList = ['df', '-kl', '-t', 'ext3', '-t', 'ext4']
    commandOutput = ""
    try:
        process = subprocess.Popen(commandList, 
                                   stdout=subprocess.PIPE,
                                   stdin=subprocess.PIPE,
                                   stderr=subprocess.PIPE,
                                   close_fds=True)
        status = process.wait()
        if status == 0:
            commandOutput = process.communicate()
    except OSError:
        return None,None,None
    
    for line in commandOutput[0].split("\n"):
        tokens = line.split()
        if len(tokens) < 4:
            continue
        if tokens[0] == partition:
            total = int(tokens[1]) / 1024.0
            used = int(tokens[2]) / 1024.0
            free = int(tokens[3]) / 1024.0
            break

    if total:
        return total, used, free
    
    # get total size from parted output
    for i in range(len(partition), 0, -1):
        pos = i - 1
        if not partition[pos].isdigit():
            break
    disk = partition[:pos+1]
    number = int(partition[pos+1:])
    
    commandList = ['parted', '-ms', disk, 'unit', 'kb', 'print']
    commandOutput = ""
    try:
        process = subprocess.Popen(commandList, 
                                   stdout=subprocess.PIPE,
                                   stdin=subprocess.PIPE,
                                   stderr=subprocess.PIPE,
                                   close_fds=True)
        status = process.wait()
        if status == 0:
            commandOutput = process.communicate()
    except OSError:
        return None,None,None
    
    lines = commandOutput[0].split(";\n")
    if len(lines) < 3:
        return None,None,None
    
    for line in lines[2:]:
        tokens = line.split(':')
        if len(tokens) < 4:
            continue
        if tokens[0] == str(number):
            total = int(tokens[3].split('kB')[0]) / 1024.0
            break
    
    return total, used, free

def getServerDetails():
    serverName = socket.gethostname()
    responseDom = ResponseXml()
    #responseDom.appendTagRoute("status.code", "0")
    #responseDom.appendTagRoute("status.message", "success")
    serverTag = responseDom.appendTagRoute("server")
    serverTag.appendChild(responseDom.createTag("name", serverName))

    nameServerList, domain, searchDomain = readResolvConfFile()
    if domain:
        domainName = domain[0]
    else:
        domainName = None
    serverTag.appendChild(responseDom.createTag("domainname", domainName))
    i = 1
    for dns in nameServerList:
        serverTag.appendChild(responseDom.createTag("dns%s" % i, dns))
        i += 1
    #TODO: probe and retrieve timezone, ntp-server, preferred-network details and update the tags

    deviceList = {}
    for device in getNetDeviceList():
        deviceList[device["device"]] = device
        try:
            macAddress = open("/sys/class/net/%s/address" % device["device"]).read().strip()
        except IOError:
            continue
        interfaces = responseDom.createTag("networkInterfaces", None)
        interfaceTag = responseDom.createTag("networkInterface", None)
        interfaceTag.appendChild(responseDom.createTag("name",      device["device"]))
        interfaceTag.appendChild(responseDom.createTag("hwaddr",      macAddress))
        if deviceList[device["device"]]:
            if deviceList[device["device"]]["onboot"]:
                interfaceTag.appendChild(responseDom.createTag("onboot", "yes"))
            else:
                interfaceTag.appendChild(responseDom.createTag("onBoot", "no"))
            interfaceTag.appendChild(responseDom.createTag("bootProto", deviceList[device["device"]]["bootproto"]))
            interfaceTag.appendChild(responseDom.createTag("ipAddress",    deviceList[device["device"]]["ipaddr"]))
            interfaceTag.appendChild(responseDom.createTag("netMask",   deviceList[device["device"]]["netmask"]))
            interfaceTag.appendChild(responseDom.createTag("defaultGateway",   deviceList[device["device"]]["gateway"]))
            if deviceList[device["device"]]["mode"]:
                interfaceTag.appendChild(responseDom.createTag("mode",   deviceList[device["device"]]["mode"]))
            if deviceList[device["device"]]["master"]:
                interfaceTag.appendChild(responseDom.createTag("bonding", "yes"))
                spliter = re.compile(r'[\D]')
                interfaceTag.appendChild(responseDom.createTag("bondid", spliter.split(device["master"])[-1]))            
        else:
            interfaceTag.appendChild(responseDom.createTag("onBoot",    "no"))
            interfaceTag.appendChild(responseDom.createTag("bootProto", "none"))
        interfaces.appendChild(interfaceTag)
    serverTag.appendChild(interfaces)
    responseDom.appendTag(serverTag)
    serverTag.appendChild(responseDom.createTag("numOfCPUs", int(os.sysconf('SC_NPROCESSORS_ONLN'))))

    try:
        meminfo = getMeminfo()
        mem_total = meminfo['MemTotal']
        mem_free = meminfo['MemFree']
        mem_used = (mem_total - mem_free)
        value = "%.2f" % (1.0 * mem_used / mem_total)
        mem_percent = 100 * float(value)
        cpu = 100 * float(getLoadavg())

    except IOError:
        print "Error"
        responseDom.appendTagRoute("server.name", serverName)
        syslog.syslog(syslog.LOG_ERR, "Error finding memory information of server:%s" % serverName)
        return None
        
    diskObj = Disk()
    ## disks = diskObj.getDiskList()
    disks = diskObj.getMountableDiskList()

    if disks is None:
        print "No disk found!"
        syslog.syslog(syslog.LOG_ERR, "Error finding disk information of server:%s" % serverName)
        return None

    serverTag.appendChild(responseDom.createTag("cpuUsage", str(cpu)))
    #serverTag.appendChild(responseDom.createTag("totalMemory", str(mem_percent)))
    serverTag.appendChild(responseDom.createTag("totalMemory", str(mem_total)))
    serverTag.appendChild(responseDom.createTag("memoryInUse", str(mem_used)))
    serverTag.appendChild(responseDom.createTag("status", "ONLINE"))
    serverTag.appendChild(responseDom.createTag("uuid", None))

    totalDiskSpace = 0
    diskSpaceInUse = 0
    diskTag = responseDom.createTag("disks")
    for disk in disks:
        if disk['interface'] in ['usb', 'mmc']:
            continue
        partitionTag = responseDom.createTag("disk", None)
        partitionTag.appendChild(responseDom.createTag("name", os.path.basename(disk['device'])))
        partitionTag.appendChild(responseDom.createTag("serverName", serverName))
        partitionTag.appendChild(responseDom.createTag("description", disk['description']))
        total, used, free = getDiskSizeInfo(disk['device'])
        if total:
            partitionTag.appendChild(responseDom.createTag("space", str(total)))
            totalDiskSpace += total
        if used:
            partitionTag.appendChild(responseDom.createTag("spaceInUse", str(used)))
            diskSpaceInUse += used
            partitionTag.appendChild(responseDom.createTag("status", "READY"))
        diskTag.appendChild(partitionTag)
    serverTag.appendChild(diskTag)
    serverTag.appendChild(responseDom.createTag("totalDiskSpace", str(totalDiskSpace)))
    serverTag.appendChild(responseDom.createTag("diskSpaceInUse", str(diskSpaceInUse)))
    return serverTag

def test():
    print getServerDetails().toxml()
