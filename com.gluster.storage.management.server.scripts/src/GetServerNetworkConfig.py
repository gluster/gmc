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
import re
from ServerUtils import *
from XmlHandler import *

from netconfpkg.NCHardwareList import getHardwareList
from netconfpkg.NCProfileList import getProfileList
from netconfpkg.NCDevice import Device
from GetNetDeviceList import getNetDeviceList

def getServerNetworkConfig():
    serverName = socket.gethostname()

    if not serverName:
        return ResponseXml.errorResponse("Unable to get server name!")

    ProfileList = getProfileList()
    DNS = None
    for profile in ProfileList:
        if profile.ProfileName == "default" and profile.Active:
            DNS = profile.DNS
        break

    if not serverName == DNS.Hostname:
        return ResponseXml.errorResponse("Unable to get server details")

    responseDom = ResponseXml()
    responseDom.appendTagRoute("status.code", "0"); 
    responseDom.appendTagRoute("status.message", "SUCCESS")
    serverTag = responseDom.appendTagRoute("server.name", serverName)
    networkInterfaces  = responseDom.appendTagRoute("server.networkInterfaces", None)

    #hardwareList = getHardwareList()
    #serverTag.appendChild(responseDom.createTag("domainname", getDomainName()))
    #serverTag.appendChild(responseDom.createTag("dns1", DNS.PrimaryDNS))
    #serverTag.appendChild(responseDom.createTag("dns2", DNS.SecondaryDNS))
    #serverTag.appendChild(responseDom.createTag("dns3", DNS.TertiaryDNS))

    deviceList = {}
    for device in getNetDeviceList():
        deviceList[device["device"]] = device
    for device in getHardwareList():
        try:
            macAddress = open("/sys/class/net/%s/address" % device.Name).read().strip()
        except IOError:
            continue
        interfaceTag = responseDom.createTag("networkInterface", device.Name)
        if deviceList[device.Name]:
            interfaceTag.appendChild(responseDom.createTag("ipAddress", deviceList[device.Name]["ipaddr"]))
            interfaceTag.appendChild(responseDom.createTag("netMask", deviceList[device.Name]["netmask"]))
            interfaceTag.appendChild(responseDom.createTag("defaultGateway", deviceList[device.Name]["gateway"]))
            interfaceTag.appendChild(responseDom.createTag("isPreferred", None))
        serverTag.appendChild(interfaceTag)
    responseDom.appendTag(serverTag)
    return responseDom
