#!/usr/bin/python
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
import Common
import DiskUtils
from ServerUtils import *
from Protocol import *
from NetworkUtils import *
from Disk import *
from XmlHandler import ResponseXml
from optparse import OptionParser


def getServerDetails(listall):

    serverName = socket.gethostname()
    meminfo = getMeminfo()
    cpu = 100 * float(getLoadavg())
    nameServerList, domain, searchDomain = readResolvConfFile()
    if not domain:
        domain = [None]

    responseDom = ResponseXml()
    serverTag = responseDom.appendTagRoute("server")
    serverTag.appendChild(responseDom.createTag("name", serverName))
    serverTag.appendChild(responseDom.createTag("domainname", domain[0]))

    for dns in nameServerList:
        serverTag.appendChild(responseDom.createTag("dns%s" % str(nameServerList.index(dns) +1) , dns))

    #TODO: probe and retrieve timezone, ntp-server details and update the tags

    deviceList = {}
    interfaces = responseDom.createTag("networkInterfaces", None)
    for device in getNetDeviceList():
        if device["model"] in ['LOCAL', 'IPV6-IN-IPV4']:
            continue
        deviceList[device["device"]] = device
        try:
            macAddress = open("/sys/class/net/%s/address" % device["device"]).read().strip()
        except IOError:
            continue
        interfaceTag = responseDom.createTag("networkInterface", None)
        interfaceTag.appendChild(responseDom.createTag("name",  device["device"]))
        interfaceTag.appendChild(responseDom.createTag("hwAddr",macAddress))
        interfaceTag.appendChild(responseDom.createTag("speed", device["speed"]))
        interfaceTag.appendChild(responseDom.createTag("model", device["model"]))
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


    # refreshing hal data
    DiskUtils.refreshHal()

    diskObj = Disk()
    disks = diskObj.getMountableDiskList()

    if disks is None:
        print "No disk found!"
        syslog.syslog(syslog.LOG_ERR, "Error finding disk information of server:%s" % serverName)
        return None

    serverTag.appendChild(responseDom.createTag("cpuUsage", str(cpu)))
    serverTag.appendChild(responseDom.createTag("totalMemory", str(convertKbToMb(meminfo['MemTotal']))))
    serverTag.appendChild(responseDom.createTag("memoryInUse", str(convertKbToMb(meminfo['MemUsed']))))
    serverTag.appendChild(responseDom.createTag("status", "ONLINE"))
    serverTag.appendChild(responseDom.createTag("uuid", None))

    totalDiskSpace = 0
    diskSpaceInUse = 0
    diskTag = responseDom.createTag("disks")
    for disk in disks:
        if not listall:
            if not disk['mount_point'].startswith("/export/"):
                continue
        if disk['interface'] in ['usb', 'mmc']:
            continue
        partitionTag = responseDom.createTag("disk", None)
        partitionTag.appendChild(responseDom.createTag("name", os.path.basename(disk['device'])))
        partitionTag.appendChild(responseDom.createTag("mountPoint", disk['mount_point']))
        partitionTag.appendChild(responseDom.createTag("serverName", serverName))
        partitionTag.appendChild(responseDom.createTag("description", disk['description']))
        total, used, free = 0, 0, 0
        if disk['size']:
            total, used, free = DiskUtils.getDiskSizeInfo(disk['device'])
        if total:
            partitionTag.appendChild(responseDom.createTag("space", str(total)))
            totalDiskSpace += total
        else:
            partitionTag.appendChild(responseDom.createTag("space", "NA"))
        if used:
            partitionTag.appendChild(responseDom.createTag("spaceInUse", str(used)))
            diskSpaceInUse += used
            partitionTag.appendChild(responseDom.createTag("status", "AVAILABLE"))
        else:
            partitionTag.appendChild(responseDom.createTag("spaceInUse", "NA"))
            partitionTag.appendChild(responseDom.createTag("status", "UNINITIALIZED"))
        diskTag.appendChild(partitionTag)
    serverTag.appendChild(diskTag)
    serverTag.appendChild(responseDom.createTag("totalDiskSpace", str(totalDiskSpace)))
    serverTag.appendChild(responseDom.createTag("diskSpaceInUse", str(diskSpaceInUse)))
    return serverTag

def main():
    ME = os.path.basename(sys.argv[0])
    parser = OptionParser(version="%s %s" % (ME, Globals.GLUSTER_PLATFORM_VERSION))

    parser.add_option("-N", "--only-data-disks",
                      action="store_false", dest="listall", default=True,
                      help="List only data disks")

    (options, args) = parser.parse_args()
    responseXml = getServerDetails(options.listall)
    if responseXml:
        print responseXml.toxml()

    sys.exit(0)

if __name__ == "__main__":
    main()
