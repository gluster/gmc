#!/usr/bin/python
#  Copyright (C) 2011 Gluster, Inc. <http://www.gluster.com>
#  This file is part of Gluster Storage Platform.
#

import os
import sys
p1 = os.path.abspath(os.path.dirname(sys.argv[0]))
p2 = "%s/common" % os.path.dirname(p1)
if not p1 in sys.path:
    sys.path.append(p1)
if not p2 in sys.path:
    sys.path.append(p2)
import dbus
import socket
import re
import Utils
import DiskUtils
from NetworkUtils import *
from Disk import *
from XmlHandler import ResponseXml
from optparse import OptionParser


def getServerDetails(listall):
    serverName = socket.getfqdn()
    meminfo = getMeminfo()
    cpu = getCpuUsageAvg()
    nameServerList, domain, searchDomain = readResolvConfFile()
    if not domain:
        domain = [None]

    responseDom = ResponseXml()
    serverTag = responseDom.appendTagRoute("server")
    serverTag.appendChild(responseDom.createTag("name", serverName))
    serverTag.appendChild(responseDom.createTag("domainname", domain[0]))
    if Utils.runCommand("pidof glusterd") == 0:
        serverTag.appendChild(responseDom.createTag("status", "ONLINE"))
    else:
        serverTag.appendChild(responseDom.createTag("status", "OFFLINE"))
    serverTag.appendChild(responseDom.createTag("glusterFsVersion", Utils.getGlusterVersion()))
    serverTag.appendChild(responseDom.createTag("cpuUsage", str(cpu)))
    serverTag.appendChild(responseDom.createTag("totalMemory", str(convertKbToMb(meminfo['MemTotal']))))
    serverTag.appendChild(responseDom.createTag("memoryInUse", str(convertKbToMb(meminfo['MemUsed']))))
    serverTag.appendChild(responseDom.createTag("uuid", None))

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

    try:
        diskDom = DiskUtils.getDiskDom()
    except dbus.dbus_bindings.DBusException, e:
        sys.stderr.write("%s. Please check if HAL services are running\n" % str(e))
        Utils.log("failed to get disk details :%s" % str(e))
        sys.exit(1)
    if not diskDom:
        sys.stderr.write("No disk found!")
        Utils.log("Failed to get disk details")
        sys.exit(2)

    serverTag.appendChild(diskDom.getElementsByTagRoute("disks")[0])
    return serverTag

def main():
    parser = OptionParser()
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
